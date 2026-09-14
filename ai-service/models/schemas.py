from pydantic import BaseModel, HttpUrl
from typing import List, Optional


class ReviewRequest(BaseModel):
    repoUrl: str


class ReviewResponse(BaseModel):
    repo_name: str
    review: str
    files: List[str]
    chunk_count: int


class HealthResponse(BaseModel):
    status: str
    service: str