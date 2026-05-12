import os
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

    client.fget_object(BUCKET, object_key, input_path)
    send_progress(job_id, 20)

    command = [
        "ffmpeg", "-y", "-ss", "00:00:01", "-i", input_path,
        "-vframes", "1", "-q:v", "2", output_path
    ]

    process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    active_processes[job_id] = process
    
    try:
        process.wait()

        if process.returncode != 0:
            if job_id not in active_processes:
                print(f"Job {job_id} was aborted during execution.")
                return None
            raise Exception("FFmpeg failed")

        send_progress(job_id, 80)
        processed_key = f"processed/{thumbnail_name}"
        client.fput_object(BUCKET, processed_key, output_path)
        
        send_progress(job_id, 100)
        return processed_key

    finally:
        if job_id in active_processes:
            del active_processes[job_id]
        if os.path.exists(input_path): os.remove(input_path)
        if os.path.exists(output_path): os.remove(output_path)

def abort_job_process(job_id):
    if job_id in active_processes:
        process = active_processes[job_id]
        print(f"Aborting FFmpeg process for job: {job_id}")
        process.terminate() 
        del active_processes[job_id]
        return True
    return False