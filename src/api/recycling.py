from fastapi import APIRouter, HTTPException, UploadFile, File, Form
from datetime import datetime
from src.ocr.service import svc
from src.db.manager import mongo_manager

router = APIRouter(prefix="/recycling")


@router.get("/recycling-centers")
async def get_recycling_centers():
    return [
        {
            "name": "RETIM - Punct de colectare prin aport voluntar",
            "lat": 45.744750660520886,
            "lng": 21.26720740281725,
            "url": "https://retim.ro/",
            "types": ["biodegradabile", "voluminoase", "constructii", "echipamente electrice", "polistiren"],
            "schedule": {
                "monday": "09:00-19:00",
                "tuesday": "09:00-19:00",
                "wednesday": "09:00-19:00",
                "thursday": "09:00-19:00",
                "friday": "09:00-19:00",
                "saturday": "08:00-16:00",
                "sunday": "Închis"
            }
        },
        {
            "name": "VRANCART SA - Punct colectare Timișoara",
            "lat": 45.7213195580428,
            "lng": 21.194566784560482,
            "url": "https://vrancart.ro/produse-si-servicii/colectare-si-reciclare-deseuri/",
            "types": ["hartie", "carton", "ambalaje"],
            "schedule": {
                "monday": "08:00-16:00",
                "tuesday": "08:00-16:00",
                "wednesday": "08:00-16:00",
                "thursday": "08:00-16:00",
                "friday": "08:00-16:00",
                "saturday": "Închis",
                "sunday": "Închis"
            }
        },
        {
            "name": "SC VIELE 2005 SRL",
            "lat": 45.74128117362353,
            "lng": 21.272836278042394,
            "url": "https://www.google.ro/search?q=SC+VIELE+2005+SRL+Recenzii",
            "types": ["plastic", "hartie", "deseuri industriale"],
            "schedule": {
                "monday": "08:00-16:00",
                "tuesday": "08:00-16:00",
                "wednesday": "08:00-16:00",
                "thursday": "08:00-16:00",
                "friday": "08:00-16:00",
                "saturday": "Închis",
                "sunday": "Închis"
            }
        },
        {
            "name": "Hamburger Recycling România",
            "lat": 45.774637638890475,
            "lng": 21.199254606722317,
            "url": "https://www.hamburger-recycling.com/ro/",
            "types": ["hartie", "sticla", "metal", "ambalaje"],
            "schedule": {
                "monday": "08:00-16:00",
                "tuesday": "08:00-16:00",
                "wednesday": "08:00-16:00",
                "thursday": "08:00-16:00",
                "friday": "08:00-16:00",
                "saturday": "Închis",
                "sunday": "Închis"
            }
        },
        {
            "name": "RETIM ECOLOGIC SERVICE SA",
            "lat": 45.75964536130877,
            "lng": 21.22072965090633,
            "url": "https://retim.ro/",
            "types": ["metal", "plastic", "sticla", "voluminoase"],
            "schedule": {
                "monday": "08:00-16:00",
                "tuesday": "08:00-16:00",
                "wednesday": "08:00-16:00",
                "thursday": "08:00-16:00",
                "friday": "08:00-16:00",
                "saturday": "Închis",
                "sunday": "Închis"
            }
        }
    ]


@router.post("/classify/")
async def classify(
    file: UploadFile = File(...),
    user_id: str = Form(...)
):
    if not file.content_type.startswith("image/"):
        raise HTTPException(400, "Only image uploads are supported")

    data = await file.read()
    bin_name = svc.get_classification(data)

    db = mongo_manager.get_db()
    await db["reports"].insert_one({
        "user_id": user_id,
        "category": bin_name,
        "timestamp": datetime.utcnow()
    })

    return {"bin": bin_name}


@router.get("/reports/{user_id}")
async def get_user_report(user_id: str):
    db = mongo_manager.get_db()
    pipeline = [
        {"$match": {"user_id": user_id}},
        {"$group": {"_id": "$category", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}}
    ]
    result = await db["reports"].aggregate(pipeline).to_list(length=100)
    return result


@router.get("/community-reports")
async def get_community_reports():
    db = mongo_manager.get_db()

    pipeline = [
        {
            "$group": {
                "_id": "$user_id",
                "scanCount": {"$sum": 1}
            }
        },
        {
            "$sort": {"scanCount": -1}
        }
    ]

    result = await db["reports"].aggregate(pipeline).to_list(length=100)

    users = await db["users"].find().to_list(length=1000)
    user_dict = {str(user["_id"]): user.get("username", "Anonim") for user in users}

    response = [
        {
            "user_id": r["_id"],
            "username": user_dict.get(r["_id"], "Anonim"),
            "scanCount": r["scanCount"]
        }
        for r in result
    ]

    return response
