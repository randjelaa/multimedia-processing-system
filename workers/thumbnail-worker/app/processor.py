import os
import time
import subprocess
from minio_client import client
from config import BUCKET
from rabbitmq_sender import send_progress

DOWNLOAD_DIR = "C:/temp/downloads"
PROCESSED_DIR = "C:/temp/processed"

active_processes = {}

def process_thumbnail(job):
    job_id = job["jobId"]
    os.makedirs(DOWNLOAD_DIR, exist_ok=True)
    os.makedirs(PROCESSED_DIR, exist_ok=True)

    object_key = job["objectKey"]
    filename = object_key.split("/")[-1]
    input_path = os.path.join(DOWNLOAD_DIR, filename)
    thumbnail_name = f"{filename}.jpg"
    output_path = os.path.join(PROCESSED_DIR, thumbnail_name)

    try:
        send_progress(job_id, 10)
        print(f"[{job_id}] Downloading from MinIO...")
        client.fget_object(BUCKET, object_key, input_path)

        send_progress(job_id, 35)
        print(f"[{job_id}] Extracting thumbnail...")

        command = [
            "ffmpeg", "-y", "-ss", "00:00:01", "-i", input_path,
            "-vframes", "1", "-q:v", "2", output_path
        ]
        
        process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        active_processes[job_id] = process
        
        process.wait()

        if job_id not in active_processes:
            print(f"[{job_id}] Thumbnail extraction aborted.")
            return None

        if process.returncode != 0:
            raise Exception("FFmpeg thumbnail extraction failed")

        send_progress(job_id, 75)
        processed_key = f"processed/{thumbnail_name}"

        print(f"[{job_id}] Uploading thumbnail...")
        client.fput_object(BUCKET, processed_key, output_path)

        send_progress(job_id, 95)
        return processed_key

    finally:
        if job_id in active_processes:
            del active_processes[job_id]
        if os.path.exists(input_path): os.remove(input_path)
        if os.path.exists(output_path): os.remove(output_path)

def abort_job_process(job_id):
    if job_id in active_processes:
        print(f"!!! Aborting process for job {job_id} !!!")
        process = active_processes[job_id]
        process.terminate()
        del active_processes[job_id]
        return True
    return False