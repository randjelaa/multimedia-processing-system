import os
from dotenv import load_dotenv

load_dotenv()

RABBITMQ_HOST = os.getenv(
    "RABBITMQ_HOST",
    "localhost"
)

MINIO_HOST = os.getenv(
    "MINIO_HOST",
    "localhost:9000"
)

MINIO_ACCESS_KEY = os.getenv(
    "MINIO_ACCESS_KEY",
    "admin"
)

MINIO_SECRET_KEY = os.getenv(
    "MINIO_SECRET_KEY",
    "password"
)

BUCKET = os.getenv(
    "MINIO_BUCKET",
    "uploads"
)