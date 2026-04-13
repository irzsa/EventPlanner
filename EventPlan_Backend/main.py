from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session, relationship  
from sqlalchemy import Column, Integer, String, Float, ForeignKey, Date
from database import Base, engine, get_db, SessionLocal
from datetime import date, timedelta
from pydantic import BaseModel

# 1. Define what a "Category" looks like in the Postgres database
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
    
    # The Foreign Key: This links the vendor to a specific category ID
    category_id = Column(Integer, ForeignKey("categories.id"))
    
    # This tells SQLAlchemy who the "parent" category is
    category = relationship("DBCategory", back_populates="vendors")

    bookings = relationship("DBBooking", back_populates="vendor")

class DBBooking(Base):
    __tablename__ = "bookings"
    id = Column(Integer, primary_key=True, index=True)
    
    # The specific date the vendor is booked
    booked_date = Column(Date, index=True) 
    
    # Links the booking to a specific vendor
    vendor_id = Column(Integer, ForeignKey("vendors.id"))
    vendor = relationship("DBVendor", back_populates="bookings")

class BookingCreate(BaseModel):
    vendor_id: int
    booked_date: date

# 2. Tell SQLAlchemy to actually create this table in Postgres
Base.metadata.create_all(bind=engine)

app = FastAPI(title="EventPlan API")

# 3. A quick helper function to "seed" the database with initial data
def seed_database():
    db = SessionLocal()
    if db.query(DBCategory).count() == 0:
        
        # 1. Create Categories
        venues_cat = DBCategory(name="Venues")
        djs_cat = DBCategory(name="DJs")
        db.add_all([venues_cat, djs_cat, DBCategory(name="Cocktail Bars"), DBCategory(name="Photographers")])
        db.commit() 
        
        # 2. Create Fake Vendors (Now with locations!)
        dj_snake = DBVendor(name="DJ Snake-Eyes", description="Top 40 and EDM.", price_per_hour=100.0, location="Downtown Club", category_id=djs_cat.id)
        
        fake_vendors = [
            DBVendor(name="The Grand Ballroom", description="A beautiful 500-person hall.", price_per_hour=500.0, location="Northside", category_id=venues_cat.id),
            DBVendor(name="Rustic Barn Retreat", description="Outdoor wedding venue.", price_per_hour=350.0, location="Countryside", category_id=venues_cat.id),
            dj_snake, # Using the variable we just created so we can attach bookings to him
            DBVendor(name="Vinyl Vintage", description="Classic 80s and 90s hits.", price_per_hour=80.0, location="West End", category_id=djs_cat.id)
        ]
        db.add_all(fake_vendors)
        db.commit()

        # 3. NEW: Create Fake Bookings for DJ Snake-Eyes
        today = date.today()
        fake_bookings = [
            DBBooking(booked_date=today + timedelta(days=2), vendor_id=dj_snake.id), # Booked in 2 days
            DBBooking(booked_date=today + timedelta(days=5), vendor_id=dj_snake.id), # Booked in 5 days
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
        booked_date=booking.booked_date
    )
    db.add(new_db_booking)
    db.commit()
    db.refresh(new_db_booking) # This grabs the new unique ID Postgres just generated
    
    return {"message": "Booking successful!", "booking_id": new_db_booking.id}
