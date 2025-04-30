from src.models.base import MongoBaseModel


class User(MongoBaseModel):
    username: str
    email: str
    is_admin: bool = False
    