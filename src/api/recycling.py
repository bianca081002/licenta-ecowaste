from fastapi import APIRouter, HTTPException, UploadFile
from src.ocr.service import svc

router = APIRouter("/recycling")

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
async def classify(file: UploadFile = File(...)):
    if not file.content_type.startswith("image/"):
        raise HTTPException(400, "Only image uploads are supported")
    data = await file.read()
    bin_name = svc.get_classification(data)
    return {"bin": bin_name}
# TODO: use reports repository to increase the conter based on classification return