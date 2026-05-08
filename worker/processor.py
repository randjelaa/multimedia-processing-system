import os
import shutil

from minio_client import client, BUCKET

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

DOWNLOAD_DIR = os.path.join(BASE_DIR, "downloads")
PROCESSED_DIR = os.path.join(BASE_DIR, "processed")

os.makedirs(DOWNLOAD_DIR, exist_ok=True)
os.makedirs(PROCESSED_DIR, exist_ok=True)


def process_job(job):

    object_key = job["objectKey"]

    filename = object_key.split("/")[-1]

    local_input = os.path.join(DOWNLOAD_DIR, filename)

    processed_name = f"processed-{filename}"

    local_output = os.path.join(PROCESSED_DIR, processed_name)

    # download from MinIO
    client.fget_object(
        BUCKET,
        object_key,
        local_input
    )

    # fake processing (copy)
    shutil.copy(
        local_input,
        local_output
    )

    # upload processed file
    processed_object_key = f"processed/{processed_name}"

    client.fput_object(
        BUCKET,
        processed_object_key,
        local_output
    )

    return processed_object_key