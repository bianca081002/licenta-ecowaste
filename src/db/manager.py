import logging

from os import name
from typing import Optional

from motor.motor_asyncio import AsyncIOMotorClient
from src.config import settings

logger = logging.getLogger(name)

class MongoDatabaseConnector:
    _instance = None

    def init(self):
        self._client: Optional[AsyncIOMotorClient] = None

    def new(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super().new(cls)
        return cls._instance

    def init(self) -> None:
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

    async def close(self) -> None:
        if self._client:
            self._client.close()
            logger.info("Disconnected from MongoDB")

    def get_db(self):
        if self._client is None:
            raise RuntimeError("MongoDB client is not initialized")
        return self._client[settings.MONGO_DATABASE_NAME]

    async def connected(self) -> bool:
        try:
            self._client.admin.command('ping')
        except Exception:
            return False
        return True

mongo_manager = MongoDatabaseConnector()