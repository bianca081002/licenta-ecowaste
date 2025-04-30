from motor.motor_asyncio import AsyncIOMotorDatabase
from src.db.manager import mongo_manager

def get_mongo_session() -> AsyncIOMotorDatabase:
    return mongo_manager.get_db()
