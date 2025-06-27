import logging
from typing import Optional
from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase
from src.config import settings

logger = logging.getLogger(__name__)

class MongoDatabaseConnector:
    _instance = None

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super(MongoDatabaseConnector, cls).__new__(cls)
        return cls._instance

    def __init__(self):
        self._client: Optional[AsyncIOMotorClient] = None

    def init(self) -> None:
        if self._client is not None:
            return
        try:
            self._client = AsyncIOMotorClient(
                settings.MONGO_DATABASE_URI,
                uuidRepresentation="standard",
                serverSelectionTimeoutMS=60000
            )
            logger.info("Connected to MongoDB")
        except Exception as e:
            logger.error("Failed to connect to MongoDB: %s", e)
            raise

    def get_db(self) -> AsyncIOMotorDatabase:
        if self._client is None:
            raise RuntimeError("MongoDB client is not initialized")
        return self._client[settings.MONGO_DATABASE_NAME]

    async def close(self) -> None:
        if self._client:
            self._client.close()
            logger.info("Disconnected from MongoDB")

    async def connected(self) -> bool:
        try:
            await self._client.admin.command("ping")
        except Exception:
            return False
        return True

mongo_manager = MongoDatabaseConnector()
