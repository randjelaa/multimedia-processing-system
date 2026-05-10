import os
import time
import ffmpeg

from minio_client import client
from config import BUCKET

from rabbitmq_sender import (
    send_progress
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

    send_progress(
        job["jobId"],
        10
    )

    print("Downloading from MinIO...")

    client.fget_object(
        BUCKET,
        object_key,
        input_path
    )

    time.sleep(1)

    send_progress(
        job["jobId"],
        35
    )

    print("Extracting thumbnail...")

    (
        ffmpeg
        .input(input_path, ss=1)
        .output(output_path, vframes=1)
        .run(overwrite_output=True)
    )

    time.sleep(1)

    send_progress(
        job["jobId"],
        75
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

    send_progress(
        job["jobId"],
        95
    )

    os.remove(input_path)
    os.remove(output_path)

    return processed_key