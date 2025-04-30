from src.repository.base import MongoRepository
from src.models.users import User


class UserRepository(MongoRepository[User]):
    pass
