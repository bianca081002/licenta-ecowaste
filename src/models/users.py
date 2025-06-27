from pydantic import BaseModel, EmailStr

class UserResponse(BaseModel):
    username: str
    email: EmailStr
    is_admin: bool = False
