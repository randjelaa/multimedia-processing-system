import os
import ffmpeg

from minio_client import client
from config import BUCKET

BASE_DIR = os.path.dirname(
    os.path.abspath(__file__)
)

DOWNLOAD_DIR = "C:/temp/downloads"
PROCESSED_DIR = "C:/temp/processed"


def process_thumbnail(job):

    os.makedirs(
        DOWNLOAD_DIR,
        exist_ok=True
    )

    os.makedirs(
        PROCESSED_DIR,
        exist_ok=True
    )

    object_key = job["objectKey"]

    filename = object_key.split("/")[-1]

    input_path = os.path.join(
        DOWNLOAD_DIR,
        filename
    )

    thumbnail_name = f"{filename}.jpg"

    output_path = os.path.join(
        PROCESSED_DIR,
        thumbnail_name
    )

    print("DOWNLOAD DIR:", DOWNLOAD_DIR)
    print("INPUT PATH:", input_path)

    print("Downloading from MinIO...")

    client.fget_object(
        BUCKET,
        object_key,
        input_path
    )

    print("Extracting thumbnail...")

    (
        ffmpeg
        .input(input_path, ss=1)
        .output(output_path, vframes=1)
        .run(overwrite_output=True)
    )

    processed_key = (
        f"processed/{thumbnail_name}"
    )

    print("Uploading thumbnail...")

    client.fput_object(
        BUCKET,
        processed_key,
        output_path
    )

    os.remove(input_path)
    os.remove(output_path)

    return processed_key