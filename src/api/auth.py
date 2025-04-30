from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, EmailStr
from motor.motor_asyncio import AsyncIOMotorDatabase
from src.db import get_mongo_session
from jose import JWTError, jwt
from passlib.context import CryptContext
from datetime import datetime, timedelta
from src.api.auth_utils import verify_token, TokenData
from src.config.jwt_settings import SECRET_KEY, ALGORITHM, ACCESS_TOKEN_EXPIRE_MINUTES


router = APIRouter(prefix="/auth", tags=["auth"])


# Context pentru hash parola
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# ---------------------------
# MODELE
# ---------------------------

class UserCreate(BaseModel):
    username: str
    email: EmailStr
    password: str

class UserLogin(BaseModel):
    email: EmailStr
    password: str

class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"

# ---------------------------
# FUNCTII UTILE
# ---------------------------

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)

def create_access_token(data: dict, expires_delta: timedelta = None):
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

# ---------------------------
# ENDPOINT REGISTER
# ---------------------------

@router.post("/register")
async def register(user: UserCreate, db: AsyncIOMotorDatabase = Depends(get_mongo_session)):
    print("✅ Received from Android:", user.dict())

    existing_user = await db["users"].find_one({"email": user.email})
    if existing_user:
        raise HTTPException(status_code=400, detail="Email already registered.")

    hashed_password = pwd_context.hash(user.password)
    user_data = {
        "username": user.username,
        "email": user.email,
        "password": hashed_password
    }

    await db["users"].insert_one(user_data)
    return {"message": f"User {user.username} registered!"}

# ---------------------------
# ENDPOINT LOGIN
# ---------------------------

@router.post("/login", response_model=TokenResponse)
async def login(user: UserLogin, db: AsyncIOMotorDatabase = Depends(get_mongo_session)):
    db_user = await db["users"].find_one({"email": user.email})
    if not db_user or not verify_password(user.password, db_user["password"]):
        raise HTTPException(status_code=401, detail="Invalid email or password")

    token_data = {
        "sub": str(db_user["_id"]),
        "email": db_user["email"]
    }

    token = create_access_token(token_data)
    return TokenResponse(access_token=token)

@router.get("/me")
async def get_me(token_data: TokenData = Depends(verify_token)):
    return {"email": token_data.email}

@router.get("/verify-token", response_model=TokenData)
async def verify_token_route(token_data: TokenData = Depends(verify_token)):
    return token_data
