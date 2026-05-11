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
    command = [
        "ffprobe", "-v", "error", "-show_entries", "format=duration",
        "-of", "default=noprint_wrappers=1:nokey=1", input_path
    ]
    result = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return float(result.stdout.strip())

def process_audio(job):
    job_id = job["jobId"]
    os.makedirs(DOWNLOAD_DIR, exist_ok=True)
    os.makedirs(PROCESSED_DIR, exist_ok=True)

    object_key = job["objectKey"]
    filename = object_key.split("/")[-1]
    input_path = os.path.join(DOWNLOAD_DIR, filename)
    audio_name = f"{filename}.mp3"
    output_path = os.path.join(PROCESSED_DIR, audio_name)

    try:
        print(f"[{job_id}] Downloading from MinIO...")
        client.fget_object(BUCKET, object_key, input_path)

        duration = get_video_duration(input_path)
        send_progress(job_id, 0)

        command = [
            "ffmpeg", "-i", input_path, "-vn", "-acodec", "libmp3lame", "-y", output_path
        ]

        process = subprocess.Popen(
            command, stderr=subprocess.PIPE, stdout=subprocess.PIPE, text=True, universal_newlines=True
        )
        active_processes[job_id] = process

        last_progress = -1
        while True:
            line = process.stderr.readline()
            if not line:
                break

            match = re.search(r"time=(\d+):(\d+):(\d+\.\d+)", line)
            if match:
                current_time = int(match.group(1)) * 3600 + int(match.group(2)) * 60 + float(match.group(3))
                progress = int((current_time / duration) * 100)
                if progress > last_progress:
                    last_progress = progress
                    send_progress(job_id, progress)

        process.wait()

        if job_id not in active_processes:
            print(f"[{job_id}] Audio extraction was aborted.")
            return None

        if process.returncode != 0:
            raise Exception("FFmpeg audio extraction failed")

        processed_key = f"processed/{audio_name}"
        print(f"[{job_id}] Uploading audio...")
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
        print(f"Aborting audio process for job: {job_id}")
        process.terminate()
        del active_processes[job_id]
        return True
    return False