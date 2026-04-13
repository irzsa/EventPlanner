from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session, relationship  
from sqlalchemy import Column, Integer, String, Float, ForeignKey, Date
from database import Base, engine, get_db, SessionLocal
from datetime import date, timedelta
from pydantic import BaseModel

class DBUser(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    password = Column(String) # We will use plain text for now to keep it simple!
    role = Column(String) # Will be either "client" or "vendor"

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

# 2. Tell SQLAlchemy to actually create this table in Postgres
Base.metadata.create_all(bind=engine)

app = FastAPI(title="EventPlan API")

# 3. A quick helper function to "seed" the database with initial data
def seed_database():
    db = SessionLocal()
    # Check if users are empty
    if db.query(DBUser).count() == 0:
        # 1. Create Test Users
        test_client = DBUser(username="client1", password="client1", role="client")
        test_vendor = DBUser(username="dj_snake", password="vendor1", role="vendor")
        db.add_all([test_client, test_vendor])
        db.commit() # Save to get their IDs

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
