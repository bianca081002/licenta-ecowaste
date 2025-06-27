import datetime
from fastapi import APIRouter, Depends, HTTPException
from motor.motor_asyncio import AsyncIOMotorDatabase
from src.db import get_mongo_session
from src.api.auth_utils import require_admin
from bson import ObjectId
from pydantic import BaseModel

router = APIRouter(prefix="/admin", tags=["admin"])

@router.get("/users")
async def get_all_users(
    admin = Depends(require_admin),
    db: AsyncIOMotorDatabase = Depends(get_mongo_session)
):
    users = await db["users"].find().to_list(length=100)

    return [
        {
            "id": str(u["_id"]),
            "username": u.get("username"),
            "email": u.get("email"),
            "is_admin": u.get("is_admin", False)
        }
        for u in users
    ]

@router.delete("/users/{user_id}")
async def delete_user(
    user_id: str,
    admin = Depends(require_admin),
    db: AsyncIOMotorDatabase = Depends(get_mongo_session)
):
    try:
        object_id = ObjectId(user_id)
    except:
        raise HTTPException(status_code=400, detail="ID invalid")

    result = await db["users"].delete_one({"_id": object_id})

    if result.deleted_count == 0:
        raise HTTPException(status_code=404, detail="Utilizatorul nu a fost găsit")

    return {"message": f"Utilizatorul cu ID {user_id} a fost șters"}

@router.post("/reset-score/{user_id}")
async def reset_user_score(
    user_id: str,
    admin = Depends(require_admin),
    db: AsyncIOMotorDatabase = Depends(get_mongo_session)
):
    try:
        object_id = ObjectId(user_id)
    except:
        raise HTTPException(status_code=400, detail="ID invalid")

    result = await db["users"].update_one(
        {"_id": object_id},
        {"$set": {"scans": 0}}
    )

    if result.matched_count == 0:
        raise HTTPException(status_code=404, detail="Utilizatorul nu a fost găsit")

    return {"message": f"Scorul utilizatorului cu ID {user_id} a fost resetat la 0"}



class ScoreUpdate(BaseModel):
    scans: int  # noul scor
@router.post("/update-score/{user_id}")
async def update_user_score(
    user_id: str,
    data: ScoreUpdate,
    admin = Depends(require_admin),
    db: AsyncIOMotorDatabase = Depends(get_mongo_session)
):
    try:
        object_id = ObjectId(user_id)
    except:
        raise HTTPException(status_code=400, detail="ID invalid")

    # Verificăm dacă utilizatorul există
    user = await db["users"].find_one({"_id": object_id})
    if not user:
        raise HTTPException(status_code=404, detail="Utilizatorul nu a fost găsit")

    # Ștergem toate scanările vechi din colecția reports
    await db["reports"].delete_many({"user_id": user_id})

    # Cream înregistrări noi (fictive) corespunzătoare noului scor
    fake_reports = [{
        "user_id": user_id,
        "category": "manual",
        "timestamp": datetime.datetime.utcnow() 
    } for _ in range(data.scans)]

    if fake_reports:
        await db["reports"].insert_many(fake_reports)

    return {"message": f"Scorul utilizatorului a fost setat la {data.scans} scanări"}