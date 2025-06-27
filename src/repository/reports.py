from src.repository.base import MongoRepository
from src.models.reports import Reports

class ReportsRepository(MongoRepository[Reports]):
    pass