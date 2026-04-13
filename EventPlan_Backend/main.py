from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session, relationship  
from sqlalchemy import Column, Integer, String, Float, ForeignKey, Date
from database import Base, engine, get_db, SessionLocal
from datetime import date, timedelta
from pydantic import BaseModel
from typing import Optional

class DBUser(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    password = Column(String) # We will use plain text for now to keep it simple!
    role = Column(String) # Will be either "client" or "vendor"
    full_name = Column(String, nullable=True)
    phone_number = Column(String, nullable=True)

    # Links to what this user owns/does
    owned_vendors = relationship("DBVendor", back_populates="owner")
    bookings = relationship("DBBooking", back_populates="client")

class DBCategory(Base):
    __tablename__ = "categories"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)
    
    # This tells SQLAlchemy that one category can have many vendors
    vendors = relationship("DBVendor", back_populates="category")

class DBVendor(Base):
    __tablename__ = "vendors"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    description = Column(String)
    price_per_hour = Column(Float)
    location = Column(String) 
    
    category_id = Column(Integer, ForeignKey("categories.id"))
    category = relationship("DBCategory", back_populates="vendors")
    
    bookings = relationship("DBBooking", back_populates="vendor")

    # NEW: Links this vendor profile to the User who created it
    owner_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    owner = relationship("DBUser", back_populates="owned_vendors")

class DBBooking(Base):
    __tablename__ = "bookings"
    id = Column(Integer, primary_key=True, index=True)
    booked_date = Column(Date, index=True) 
    
    vendor_id = Column(Integer, ForeignKey("vendors.id"))
    vendor = relationship("DBVendor", back_populates="bookings")

    # NEW: Links this booking to the specific Client who made it
    client_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    client = relationship("DBUser", back_populates="bookings")

class BookingCreate(BaseModel):
    vendor_id: int
    client_id: int
    booked_date: date

class UserLogin(BaseModel):
    username: str
    password: str

class UserUpdate(BaseModel):
    full_name: str
    phone_number: str

class VendorUpdate(BaseModel):
    name: str
    description: str
    price_per_hour: float
    location: str

# 2. Tell SQLAlchemy to actually create this table in Postgres
Base.metadata.create_all(bind=engine)

app = FastAPI(title="EventPlan API")

# 3. A quick helper function to "seed" the database with initial data
def seed_database():
    db = SessionLocal()
    # Check if users are empty
    if db.query(DBUser).count() == 0:
        # 1. Create Test Users
        test_client = DBUser(username="client1", password="password123", role="client", full_name="John Doe", phone_number="555-0199")
        test_vendor = DBUser(username="dj_snake", password="password123", role="vendor", full_name="Sam Smith", phone_number="555-0888")
        db.add_all([test_client, test_vendor])
        db.commit()

        # 2. Create Categories
        venues_cat = DBCategory(name="Venues")
        djs_cat = DBCategory(name="DJs")
        db.add_all([venues_cat, djs_cat, DBCategory(name="Cocktail Bars"), DBCategory(name="Photographers")])
        db.commit() 
        
        # 3. Create Fake Vendors
        dj_snake = DBVendor(name="DJ Snake-Eyes", description="Top 40 and EDM.", price_per_hour=100.0, location="Downtown Club", category_id=djs_cat.id, owner_id=test_vendor.id) # LINKED TO THE VENDOR USER!
        
        fake_vendors = [
            DBVendor(name="The Grand Ballroom", description="A beautiful 500-person hall.", price_per_hour=500.0, location="Northside", category_id=venues_cat.id),
            DBVendor(name="Rustic Barn Retreat", description="Outdoor wedding venue.", price_per_hour=350.0, location="Countryside", category_id=venues_cat.id),
            dj_snake, 
            DBVendor(name="Vinyl Vintage", description="Classic 80s and 90s hits.", price_per_hour=80.0, location="West End", category_id=djs_cat.id)
        ]
        db.add_all(fake_vendors)
        db.commit()

        # 4. Create Fake Bookings
        today = date.today()
        fake_bookings = [
            # Booked by the test client!
            DBBooking(booked_date=today + timedelta(days=2), vendor_id=dj_snake.id, client_id=test_client.id), 
            DBBooking(booked_date=today + timedelta(days=5), vendor_id=dj_snake.id, client_id=test_client.id), 
        ]
        db.add_all(fake_bookings)
        db.commit()

    db.close()
# Run the seed function when the file loads
seed_database()

@app.get("/")
def read_root():
    return {"message": "Welcome to the EventPlan API!"}

# 4. The updated endpoint! It now asks Postgres for the data instead of using a hardcoded list.
@app.get("/categories")
def get_categories(db: Session = Depends(get_db)):
    # Query the database for all categories
    categories = db.query(DBCategory).all()
    return categories

@app.get("/categories/{category_id}/vendors")
def get_vendors_for_category(category_id: int, db: Session = Depends(get_db)):
    vendors = db.query(DBVendor).filter(DBVendor.category_id == category_id).all()
    return vendors

# Get all booked dates for a specific vendor
@app.get("/vendors/{vendor_id}/booked-dates")
def get_vendor_booked_dates(vendor_id: int, db: Session = Depends(get_db)):
    # Query the bookings table for this specific vendor
    bookings = db.query(DBBooking).filter(DBBooking.vendor_id == vendor_id).all()
    
    # Extract just the dates and turn them into text strings (YYYY-MM-DD)
    booked_dates_list = [booking.booked_date.isoformat() for booking in bookings]
    
    return booked_dates_list

# Create a new booking
@app.post("/bookings")
def create_booking(booking: BookingCreate, db: Session = Depends(get_db)):
    
    # 1. Double-check that the vendor actually exists before booking!
    vendor = db.query(DBVendor).filter(DBVendor.id == booking.vendor_id).first()
    if not vendor:
        raise HTTPException(status_code=404, detail="Vendor not found")

    # 2. Check if the date is ALREADY booked for this specific vendor
    existing_booking = db.query(DBBooking).filter(
        DBBooking.vendor_id == booking.vendor_id,
        DBBooking.booked_date == booking.booked_date
    ).first()
    
    if existing_booking:
        raise HTTPException(status_code=400, detail="This date is already booked!")

    # 3. If everything is safe, create the new booking in Postgres
    new_db_booking = DBBooking(
        vendor_id=booking.vendor_id,
        client_id=booking.client_id,
        booked_date=booking.booked_date
    )
    db.add(new_db_booking)
    db.commit()
    db.refresh(new_db_booking) # This grabs the new unique ID Postgres just generated
    
    return {"message": "Booking successful!", "booking_id": new_db_booking.id}

@app.post("/login")
def login(user_data: UserLogin, db: Session = Depends(get_db)):
    # Look for the user in the database
    user = db.query(DBUser).filter(
        DBUser.username == user_data.username, 
        DBUser.password == user_data.password
    ).first()
    
    # If they don't exist, kick them out
    if not user:
        raise HTTPException(status_code=401, detail="Invalid username or password")
    
    # If they do exist, return their ID and Role so Android can save it!
    return {
        "message": "Login successful!", 
        "user_id": user.id, 
        "role": user.role
    }
# Get a client's itinerary
@app.get("/clients/{client_id}/bookings")
def get_client_bookings(client_id: int, db: Session = Depends(get_db)):
    # Find all bookings belonging to this client
    bookings = db.query(DBBooking).filter(DBBooking.client_id == client_id).all()
    
    # We will format the response into a clean list so Android can easily read it
    itinerary = []
    for b in bookings:
        itinerary.append({
            "booking_id": b.id,
            "booked_date": b.booked_date.isoformat(),
            "vendor_name": b.vendor.name,
            "location": b.vendor.location
        })
        
    return itinerary

# Get a Vendor's Master Dashboard
@app.get("/users/{user_id}/vendor-dashboard")
def get_vendor_dashboard(user_id: int, db: Session = Depends(get_db)):
    # 1. Find the vendor profile owned by this user
    vendor = db.query(DBVendor).filter(DBVendor.owner_id == user_id).first()
    
    if not vendor:
        raise HTTPException(status_code=404, detail="Vendor profile not found for this user")

    # 2. Grab all their bookings and format them
    bookings_list = []
    for b in vendor.bookings:
        bookings_list.append({
            "booking_id": b.id,
            "booked_date": b.booked_date.isoformat(),
            # Because we linked DBUser, we can grab the client's username!
            "client_username": b.client.username if b.client else "Unknown Client"
        })

    # 3. Send it all back as one neat package
    return {
        "vendor_id": vendor.id,
        "vendor_name": vendor.name,
        "price_per_hour": vendor.price_per_hour,
        "location": vendor.location,
        "bookings": bookings_list
    }

# Update User Profile (Name and Phone)
@app.put("/users/{user_id}")
def update_user_profile(user_id: int, profile_data: UserUpdate, db: Session = Depends(get_db)):
    # 1. Find the user
    user = db.query(DBUser).filter(DBUser.id == user_id).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
        
    # 2. Update their data
    user.full_name = profile_data.full_name
    user.phone_number = profile_data.phone_number
    
    # 3. Save to Postgres
    db.commit()
    
    return {"message": "Profile updated successfully!"}

# Update Vendor Business Details
@app.put("/vendors/{vendor_id}")
def update_vendor_details(vendor_id: int, vendor_data: VendorUpdate, db: Session = Depends(get_db)):
    # 1. Find the vendor profile
    vendor = db.query(DBVendor).filter(DBVendor.id == vendor_id).first()
    
    if not vendor:
        raise HTTPException(status_code=404, detail="Vendor not found")
        
    # 2. Update their business data
    vendor.name = vendor_data.name
    vendor.description = vendor_data.description
    vendor.price_per_hour = vendor_data.price_per_hour
    vendor.location = vendor_data.location
    
    # 3. Save to Postgres
    db.commit()
    
    return {"message": "Business details updated successfully!"}

# Cancel a booking
@app.delete("/bookings/{booking_id}")
def cancel_booking(booking_id: int, db: Session = Depends(get_db)):
    booking = db.query(DBBooking).filter(DBBooking.id == booking_id).first()
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")
    
    db.delete(booking)
    db.commit()
    return {"message": "Booking cancelled successfully"}

# Search for Vendors by Name and/or Location
@app.get("/search/vendors")
def search_vendors(name: Optional[str] = None, location: Optional[str] = None, db: Session = Depends(get_db)):
    # 1. Start with a query for ALL vendors
    query = db.query(DBVendor)
    
    # 2. If they typed a name, filter the query (ilike means case-insensitive!)
    if name:
        query = query.filter(DBVendor.name.ilike(f"%{name}%"))
        
    # 3. If they typed a location, filter it further
    if location:
        query = query.filter(DBVendor.location.ilike(f"%{location}%"))
        
    # 4. Execute the query
    results = query.all()
    
    # 5. Format the results for Android
    formatted_results = []
    for v in results:
        formatted_results.append({
            "vendor_id": v.id,
            "vendor_name": v.name,
            "description": v.description,
            "location": v.location,
            "price_per_hour": v.price_per_hour
        })
        
    return formatted_results
