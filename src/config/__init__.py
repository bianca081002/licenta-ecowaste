from pathlib import Path
from typing import ClassVar
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    BASE_DIR: ClassVar[Path] = Path(__file__).resolve().parent.parent.parent

    # Mongo
    MONGO_MASTER_DATABASE_USER: str
    MONGO_MASTER_DATABASE_PASSWORD: str
    MONGO_DATABASE_NAME: str
    MONGO_DATABASE_URI: str

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        extra = "ignore"


settings = Settings()
