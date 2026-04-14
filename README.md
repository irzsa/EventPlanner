# EventPlan Marketplace
* Irzsa Iulian

## Project Title
EventPlan: The Complete Event Vendor Marketplace

## Description
EventPlan is a comprehensive, dual-role marketplace application that connects clients planning events with premium vendors (DJs, Venues, Photographers, and Catering). Clients can discover recommended vendors, search by specific criteria, view high-quality portfolios, and book available dates directly through the app.

## Features
* **Role-Based Routing:** Secure login and registration that directs users to distinct Client or Vendor dashboards based on their account type.
* **Dynamic Discovery Feed:** A visually rich feed featuring edge-to-edge vendor cards using modern minimalist UI guidelines.
* **Real-Time Search Engine:** "Search-as-you-type" functionality that instantly filters vendors by Name or Location, dynamically hiding the discovery feed to display results.
* **Booking Calendar:** A detailed vendor profile screen equipped with a smart calendar that checks availability via the backend API to prevent double-booking.
* **Premium UI/UX:** Circular profile images, custom Material cards, and high-resolution Unsplash photography loaded asynchronously to ensure smooth scrolling.
* **Secure Backend:** Communicates with a custom Python REST API, featuring bcrypt password hashing for data security.

## Screenshots
<img width="417" height="929" alt="Dashboard" src="https://github.com/user-attachments/assets/c945106a-fbea-46b6-a356-55cbe000c6b8" />
<img width="417" height="927" alt="Booking" src="https://github.com/user-attachments/assets/94a4d485-271a-42d7-889f-6aad07e0225b" />
<img width="416" height="928" alt="Dynamic search" src="https://github.com/user-attachments/assets/c2fd7bd0-e23a-4691-b0e0-e0aa06057d68" />

## Technologies Used
* **Kotlin** & **Android SDK** (Frontend)
* **Retrofit** (REST API Network Calls)
* **Glide** (Asynchronous Image Loading)
* **Android Material Design Components** (UI/UX)
* **Python / FastAPI** (Backend Server)
* **PostgreSQL / SQLite via SQLAlchemy** (Database)
* **Docker** (Containerization for the backend)

## How to Run
1. Clone the repository to your local machine.
2. Open the project folder using **Android Studio**.
3. **Start the Backend:** Navigate to the backend folder and run `docker compose up --build` to spin up the Python API and Database.
4. Run the application on an Android emulator or physical device.
