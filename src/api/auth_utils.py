from datetime import datetime
from typing import Annotated

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from pydantic import BaseModel, EmailStr

from src.config.jwt_settings import SECRET_KEY, ALGORITHM, ACCESS_TOKEN_EXPIRE_MINUTES


oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/login")

class TokenData(BaseModel):
    id: str
    email: EmailStr

def verify_token(token: Annotated[str, Depends(oauth2_scheme)]) -> TokenData:
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")
        email: str = payload.get("email")
        if user_id is None or email is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token invalid",
            )
        return TokenData(id=user_id, email=email)

    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token invalid sau expirat",
        )

def require_admin(token: Annotated[str, Depends(oauth2_scheme)]) -> TokenData:
    token_data = verify_token(token)
    if not getattr(token_data, "email", None):
        raise HTTPException(status_code=403, detail="Acces interzis")

    # Dacă vrei să incluzi și rolul de admin din token:
    payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    if not payload.get("is_admin", False):
        raise HTTPException(status_code=403, detail="Doar administratorii au acces")
    
    return token_data