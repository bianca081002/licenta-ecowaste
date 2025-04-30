from pymongo import ReturnDocument
from motor.motor_asyncio import AsyncIOMotorDatabase
from typing import Generic, List, Optional, TypeVar, Any, Dict
import uuid
from src.models.base import MongoBaseModel
from src.repository.base import BaseRepository
from src.config import ImproperlyConfigured
from pydantic import BaseSettings


T = TypeVar('T', bound=MongoBaseModel)

class MongoRepository(BaseRepository[T], Generic[T]):
    model: type[T] = MongoBaseModel

    def __init__(self, db: AsyncIOMotorDatabase):
        if not hasattr(self.model, "Settings") or not hasattr(self.model.Settings, "name"):
            raise ImproperlyConfigured(
                "Document should define a Settings configuration class with the name of the collection."
            )

        self._collection = db[self.model.Settings.name]

    async def get_by_id(self, _id: uuid.UUID) -> Optional[T]:
        instance = await self._collection.find_one({"_id": _id})
        if not instance:
            return None
        return self.model.from_mongo(instance)
    
    async def get(self, **filter_options: Dict[str, Any]) -> Optional[T]:
        instance = await self._collection.find_one(filter_options)
        return self.model.from_mongo(instance)

    async def create(self, data: Dict[str, Any], **kwargs) -> T:
        model = self.model(**data)
        instance = await self._collection.insert_one(model.to_mongo())
        result = await self._collection.find_one({"_id": instance.inserted_id})
        return self.model.from_mongo(result)

    async def get_or_create(self, defaults: Optional[Dict[str, Any]] = None, **kwargs):

        defaults = defaults or {}

        result = await self.filter(**kwargs)

        if result:
            return result[0]

        create_data = {**kwargs, **defaults}

        new_instance = await self.create(create_data)
        return new_instance

    async def bulk_create(self, data_list: List[Dict[str, Any]]) -> List[T]:
        if not data_list:
            return []
        models = [self.model(**item) for item in data_list]

        mongo_docs = [model.model_dump() for model in models]

        for doc in mongo_docs:
            if 'id' in doc:
                doc['_id'] = doc.pop('id')

            for key, value in doc.items():
                if isinstance(value, uuid.UUID):
                    doc[key] = str(value)

        result = await self._collection.insert_many(mongo_docs)

        for i, model in enumerate(models):
            if hasattr(model, 'id') and not model.id:
                model.id = result.inserted_ids[i]

        return models

    async def filter(
            self,
            projection: Dict[str, Any] = None,
            sort_by: List[tuple] = None,
            **filter_options
    ) -> List[T]:
        result = []

        cursor = self._collection.find(filter_options, projection=projection)

        if sort_by:
            cursor.sort(sort_by)

        async for document in cursor:
            result.append(self.model.from_mongo(document))

        return result
            
    async def list(self) -> List[T]:
        cursor = self._collection.find({})
        results = await cursor.to_list(length=None)
        return [self.model.from_mongo(result) for result in results]

    async def update(self, _id: uuid.UUID, data: Dict[str, Any], **kwargs) -> Optional[T]:
        instance = await self._collection.find_one_and_update(
            {"_id": _id},
            {"$set": data},
            upsert=False,
            return_document=ReturnDocument.AFTER
        )
        return self.model.from_mongo(instance) if instance else None
    
    async def delete_many(self, **filter_options) -> bool:
        if not filter_options:
            raise ValueError("At least one filter must be provided for deletion")

        result = self._collection.delete_many(filter_options)
        return result.deleted_count > 0

    async def delete(self, _id: uuid.UUID) -> bool:
        result = await self._collection.delete_one({"_id": _id})
        return result.deleted_count > 0
