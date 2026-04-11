from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
from sqlalchemy import Column, Integer, String
from database import Base, engine, get_db, SessionLocal

# 1. Define what a "Category" looks like in the Postgres database
class DBCategory(Base):
    __tablename__ = "categories"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)

# 2. Tell SQLAlchemy to actually create this table in Postgres
Base.metadata.create_all(bind=engine)

app = FastAPI(title="EventPlan API")

# 3. A quick helper function to "seed" the database with initial data
def seed_database():
    db = SessionLocal()
    # Only add these if the table is completely empty
    if db.query(DBCategory).count() == 0:
        initial_categories = ["Venues", "DJs", "Cocktail Bars", "Photographers", "Dessert Bars", "Security"]
        for cat_name in initial_categories:
            db.add(DBCategory(name=cat_name))
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
