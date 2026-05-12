import os
import subprocess
import re
import signal  
from minio_client import client
from config import BUCKET
from rabbitmq_sender import send_progress

DOWNLOAD_DIR = "C:/temp/downloads"
PROCESSED_DIR = "C:/temp/processed"

active_processes = {}

def get_video_duration(input_path):
    command = ["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", input_path]
    result = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return float(result.stdout.strip())

def process_transcode(job):
    job_id = job["jobId"]
    
    os.makedirs(DOWNLOAD_DIR, exist_ok=True)
    os.makedirs(PROCESSED_DIR, exist_ok=True)

    object_key = job["objectKey"]
    filename = object_key.split("/")[-1]
    
    input_path = os.path.join(DOWNLOAD_DIR, filename)
    transcoded_name = f"720p-{filename}"
    output_path = os.path.join(PROCESSED_DIR, transcoded_name)

    client.fget_object(BUCKET, object_key, input_path)
    duration = get_video_duration(input_path)
    send_progress(job_id, 0)

    command = ["ffmpeg", "-i", input_path, "-vf", "scale=1280:720", "-y", output_path]
    process = subprocess.Popen(command, stderr=subprocess.PIPE, stdout=subprocess.PIPE, text=True, universal_newlines=True)
    active_processes[job_id] = process
    print(f"Started FFmpeg for job {job_id}")

    try:
        last_progress = -1
        while True:
            line = process.stderr.readline()
            if not line: break

            match = re.search(r"time=(\d+):(\d+):(\d+\.\d+)", line)
            if match:
                current_time = int(match.group(1)) * 3600 + int(match.group(2)) * 60 + float(match.group(3))
                progress = int((current_time / duration) * 100)
                if progress > last_progress:
                    last_progress = progress
                    send_progress(job_id, progress)

        process.wait()

        if process.returncode != 0:
            if job_id not in active_processes:
                print(f"Job {job_id} was aborted during execution.")
                return None
            raise Exception("FFmpeg failed")

        processed_key = f"processed/{transcoded_name}"
        client.fput_object(BUCKET, processed_key, output_path)
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