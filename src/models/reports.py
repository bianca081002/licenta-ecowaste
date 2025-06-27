import uuid
from pydantic import BaseModel


class Reports(BaseModel):
    user_id: uuid.UUID
    plastic: int | None = None
    glass: int | None = None
