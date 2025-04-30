from fastapi import APIRouter, Depends
from db import get_mongo_session
from repository.users import UserRepository
from typing import List
from src.models.users import UserResponse
from src.db.manager import mongo_manager
from motor.motor_asyncio import AsyncIOMotorDatabase




router = APIRouter(prefix="/users")


@router.get("", response_model=List[UserResponse])
async def list_users(session: AsyncIOMotorDatabase = Depends(get_mongo_session)):
    """List all users (paginated)"""
    user_repository = UserRepository(session)
    return await user_repository.list()