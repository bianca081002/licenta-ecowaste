
# 📘 EcoWaste – Project README

**EcoWaste** is a mobile and backend-powered platform designed to promote responsible waste disposal through AI-powered waste classification, user activity tracking, and a gamified community leaderboard. The Android app allows users to scan waste using their phone’s camera, receive classification feedback via an AI model, and earn points based on eco-friendly actions. The FastAPI backend manages authentication, user data, reports, and provides admin control functionalities, all integrated with MongoDB.


## 📁 Repository and Deliverables

The repository contains:
- The **complete source code** for both the mobile application (frontend) and the backend server,
- Configuration files, scripts, and documentation necessary for compilation and deployment,


## 📦 Project Contents

This project is composed of two main components:

1. **Android Mobile Application** – written in Java using Android Studio.
2. **Backend Server API** – written in Python using the FastAPI framework, connected to a MongoDB database and supporting AI-powered image classification using Transformers.

## 🧰 Requirements and Tools

To compile and run this project, the following tools must be installed:

### ✅ General Requirements

| Tool            | Purpose                               |
|-----------------|----------------------------------------|
| `Git`           | Version control and cloning the repo   |
| `Docker`        | Containerization (backend + database)  |
| `Docker Compose`| Multi-service Docker orchestration     |
| `Android Studio`| Develop and run the Android app        |

## 🧪 Backend API (FastAPI + MongoDB + Docker)

The backend is fully containerized and can be run locally using Docker Compose.

### 📂 Step-by-step Backend Setup (with Docker)

#### 1. Clone the repository

```bash
git clone https://github.com/bianca081002/licenta-ecowaste.git
cd ecowaste
```

#### 2. Configure environment variables

Create a `.env` file:

```bash
cp .env.example .env
```

Fill in required variables:
```env
MONGO_MASTER_DATABASE_USER=your_mongo_user
MONGO_MASTER_DATABASE_PASSWORD=your_mongo_password
MONGO_DATABASE_NAME=licenta
MONGO_DATABASE_URI=mongodb://your_user:your_pass@localhost:27017/licenta

JWT_SECRET_KEY=your_jwt_secret_key
ACCESS_TOKEN_EXPIRE_MINUTES=60
```

#### 3. Build and run the backend + MongoDB database

```bash
docker-compose up --build
```

#### 4. Access API documentation

Visit the automatically generated Swagger UI:
```
http://localhost:8111/docs
```

## 📦 Backend Dependencies (Python)

Dependencies are defined in `pyproject.toml` and locked in `uv.lock`.

📚 Main dependencies include:

- `fastapi` – Web framework
- `uvicorn` – ASGI server
- `motor` – Async MongoDB client
- `pydantic`, `pydantic-settings` – Data validation
- `python-jose`, `passlib` – JWT authentication & password hashing
- `transformers`, `pillow` – AI classification
- `dotenv`, `python-multipart` – Env & file upload handling

To manually install them:
```bash
pip install uv
uv sync
```

To start the backend manually (non-Docker):
```bash
uvicorn src.main:fastapi_app --host 127.0.0.1 --port 8000 --reload
```

## 📱 Android Application (Java + Android Studio)

The mobile application allows users to scan waste, submit reports, and view their profile and leaderboard.

### 🧭 Step-by-step Android App Setup

#### 1. Open Android Studio
Launch Android Studio and choose **"Open an existing project"**.

#### 2. Select the project directory
Choose the folder where you cloned the EcoWaste repository.

#### 3. Wait for Gradle build
The project will sync and download required dependencies.

#### 4. Set SDK path (if needed)
Edit `local.properties` and add:
```
sdk.dir=C:/Users/ady_m/AppData/Local/Android/Sdk
```

#### 5. Connect a device or emulator
Start an emulator or connect your Android phone via USB.

#### 6. Run the app
Click the green **Run ▶** button or use:

```
Shift + F10
```

## 🚀 Main Features

### Backend:
- ✅ User registration & login with JWT
- ✅ AI-based waste image classification
- ✅ MongoDB database integration
- ✅ Admin account & user moderation

### Mobile App:
- 📷 Waste scan with camera
- 📍 Map with recycling centers
- 🧑 Profile and leaderboard
- 📢 Community reporting
- 🔐 Admin dashboard

## 🌐 API Endpoints Summary

| Method | Endpoint                | Description             |
|--------|-------------------------|-------------------------|
| POST   | `/auth/register`        | Register new user       |
| POST   | `/auth/login`           | User login              |
| GET    | `/auth/me`              | Get current user info   |
| GET    | `/auth/verify-token`    | Verify access token     |
| DELETE | `/auth/delete-account`  | Delete own account      |
| POST   | `/scan/`                | Submit image for scan   |
| GET    | `/users/leaderboard`    | View top contributors   |

## 🔒 Security

- Uses **JWT (JSON Web Tokens)** for secure API access.
- Passwords are hashed using `bcrypt`.
- Tokens are verified on each request using dependency injection.

## 🧠 Artificial Intelligence

- Waste classification is powered by a pre-trained HuggingFace Transformer model.
- Images are processed with Pillow.
- Classification runs locally (no cloud access needed during runtime).

## 📌 Final Notes

- The backend server runs on `localhost:8111`
- The MongoDB database is accessible internally via Docker
- The Android app is fully functional and tested on multiple devices
- Repository access is private and academic-only

## 📎 Summary of Essential Commands

```bash
# Backend (Docker)
docker-compose up --build

# Backend (Manual)
pip install uv
uv sync
uvicorn src.main:fastapi_app --host 127.0.0.1 --port 8000 --reload

# Android App
Open Android Studio > Open project > Run
```

# EcoWaste – An intelligent mobile application for waste management

GitHub Repository: https://github.com/bianca081002/licenta-ecowaste  
GitLab Repository (UPT): 

## Overview

EcoWaste is an integrated academic application designed to promote responsible recycling and environmental awareness through artificial intelligence, mobile engagement, and gamified community interactions. The system consists of two components:

- A native Android mobile application that allows users to scan waste using the phone camera and receive classification feedback based on AI processing.
- A Python-based backend server built with FastAPI, providing API endpoints for authentication, waste classification, user management, community ranking, and reporting functionalities.

The application uses a pretrained Transformer-based model to classify scanned images of waste into predefined categories (e.g., plastic, paper, cardboard). Results are stored in a MongoDB database, and authenticated users are rewarded with points based on their eco-friendly actions.

## Project Structure

```
licenta-ecowaste/
├── android/                  # Android mobile application (Java)
├── src/                      # FastAPI backend source code
├── test_images/              # Sample images used for classification testing
├── docker-compose.yml        # Docker configuration (API + MongoDB)
├── Dockerfile                # Backend container definition
├── .env.example              # Template for environment variables
├── pyproject.toml            # Backend dependencies
├── uv.lock                   # Locked dependency versions
└── README.md                 # Project documentation
```

## Step-by-Step Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/bianca081002/licenta-ecowaste.git
cd licenta-ecowaste
```

### 2. Configure Environment Variables

Create a `.env` file in the project root and populate it based on the `.env.example` template:

#### `.env` 

```env
MONGO_MASTER_DATABASE_USER=your_mongo_user
MONGO_MASTER_DATABASE_PASSWORD=your_mongo_password
MONGO_DATABASE_NAME=licenta
MONGO_DATABASE_URI=mongodb://your_user:your_pass@localhost:27017/licenta

JWT_SECRET_KEY=your_jwt_secret_key
ACCESS_TOKEN_EXPIRE_MINUTES=60
```

### 3. Build and Run the Backend and Database (Docker)

To start the backend and MongoDB services locally:

```bash
docker-compose up --build
```

Once running, the API will be available at:
```
http://localhost:8111/docs
```

### 4. Open and Run the Android Application

1. Open **Android Studio**
2. Select **"Open an existing project"** and choose the `android/` directory inside the cloned repository
3. Wait for Gradle sync to complete
4. If needed, edit `local.properties` and set:

```
sdk.dir=C:/Users/ady_m/AppData/Local/Android/Sdk
```

5. Connect an Android device or emulator
6. Run the application via **Shift + F10** or the Run button

## Backend Features and Technologies

- FastAPI – high-performance asynchronous API framework
- MongoDB – NoSQL database for storing user data and reports
- Transformers (HuggingFace) – pretrained AI model for waste classification
- Pillow – image preprocessing
- python-jose, passlib – secure JWT-based authentication
- Docker + Docker Compose – isolated environment for reproducible deployment

## Mobile App Features

- Waste image scanning via native Android camera
- Real-time classification and point assignment
- Community leaderboard and user profile
- Interactive map with recycling centers
- Admin-only dashboard for account management

## API Endpoints Overview

## API Endpoints Overview

| Method | Endpoint                          | Description                                      |
|--------|-----------------------------------|--------------------------------------------------|
| POST   | /auth/register                    | Register a new user                              |
| POST   | /auth/login                       | User login (JWT issued)                          |
| GET    | /auth/me                          | Retrieve current user data                       |
| GET    | /auth/verify-token                | Verify JWT token validity                        |
| DELETE | /auth/delete-account              | Delete own user account                          |
| GET    | /users                            | List all users (repository)                      |
| GET    | /recycling/reports/{user_id}      | View user-specific report by category            |
| GET    | /recycling/community-reports      | View top users (leaderboard)                     |
| GET    | /recycling/recycling-centers      | List available recycling centers (static)        |
| POST   | /recycling/classify/              | Submit image for AI waste classification         |
| GET    | /status/health                    | Health check for backend/database                |
| GET    | /admin/users                      | [Admin] Get all users                            |
| DELETE | /admin/users/{user_id}            | [Admin] Delete specific user                     |
| POST   | /admin/reset-score/{user_id}      | [Admin] Reset scan score for user                |
| POST   | /admin/update-score/{user_id}     | [Admin] Set manual scan score + regenerate logs  |



## Security and Authentication

- Secure password hashing with bcrypt
- Stateless JWT authentication for all endpoints
- Role-based access control for administrative actions
- Environment-based secrets for token and database credentials

## Final Notes

- The backend runs on http://localhost:8111
- MongoDB is automatically deployed via Docker and accessible within the internal network
- All dependencies are version-locked via uv.lock
- This project is developed as part of a bachelor’s thesis at the Politehnica University of Timișoara

