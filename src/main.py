from contextlib import asynccontextmanager
from fastapi import FastAPI
from src.db.manager import mongo_manager
from fastapi.openapi.utils import get_openapi
from fastapi.middleware.cors import CORSMiddleware
from fastapi.routing import APIRouter,HTTPException
from src.api.status import router as status_router
from src.api.auth import router as auth_router
from src.api.recycling import router as recycling_router
from src.api.admin import router as admin_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    mongo_manager.init()

    try:
        yield
    finally:
        await mongo_manager.close()
        pass
fastapi_app = FastAPI(
    title="Licenta API Server",
    root_path="/api",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)
fastapi_app.include_router(auth_router)
fastapi_app.include_router(recycling_router)


def custom_openapi():
    if fastapi_app.openapi_schema:
        return fastapi_app.openapi_schema

    openapi_schema = get_openapi(
        title="API Server",
        version="1.0.0",
        description="API with JWT authentication",
        routes=fastapi_app.routes,
    )

    if "components" not in openapi_schema:
        openapi_schema["components"] = {}

    openapi_schema["components"]["securitySchemes"] = {
        "Bearer": {
            "type": "http",
            "scheme": "bearer",
            "bearerFormat": "JWT"
        }
    }

    openapi_schema["security"] = [{"Bearer": []}]

    fastapi_app.openapi_schema = openapi_schema
    return fastapi_app.openapi_schema


fastapi_app.openapi = custom_openapi

app = CORSMiddleware(
    app=fastapi_app,
    allow_origins=[''],
    allow_credentials=True,
    allow_methods=[""],
    allow_headers=["*"],
)

router = APIRouter()
router.include_router(status_router)
fastapi_app.include_router(auth_router, prefix="/api/auth")
fastapi_app.include_router(admin_router)



@router.post("/feedback/")
async def feedback(image_name: str, correct_label: str):
    # Salvăm feedback-ul într-un fișier sau DB
    try:
        with open("feedback_log.txt", "a") as feedback_file:
            feedback_file.write(f"{image_name}, {correct_label}\n")
        return {"message": "Feedback received"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))