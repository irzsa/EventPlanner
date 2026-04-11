from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base

# The connection string to your Postgres Docker container
SQLALCHEMY_DATABASE_URL = "postgresql://admin:secretpassword@db:5432/eventplan_db"

# Create the database engine
engine = create_engine(SQLALCHEMY_DATABASE_URL)

# Create a session maker to talk to the database
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base class for our database models
Base = declarative_base()

# A helper function we will use in main.py to get a database session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
