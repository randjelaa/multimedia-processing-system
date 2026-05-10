import os
import subprocess
import re

from minio_client import client
from config import BUCKET

from rabbitmq_sender import (
    send_progress
)

DOWNLOAD_DIR = "C:/temp/downloads"
PROCESSED_DIR = "C:/temp/processed"


def get_video_duration(input_path):

    command = [

        "ffprobe",

        "-v", "error",

        "-show_entries",
        "format=duration",

        "-of",
        "default=noprint_wrappers=1:nokey=1",

        input_path
    ]

    result = subprocess.run(

        command,

        stdout=subprocess.PIPE,

        stderr=subprocess.PIPE,

        text=True
    )

    return float(result.stdout.strip())


def process_audio(job):

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

    audio_name = f"{filename}.mp3"

    output_path = os.path.join(
        PROCESSED_DIR,
        audio_name
    )

    print("Downloading from MinIO...")

    client.fget_object(
        BUCKET,
        object_key,
        input_path
    )

    duration = get_video_duration(
        input_path
    )

    print(
        f"Video duration: {duration}"
    )

    send_progress(
        job["jobId"],
        0
    )

    command = [

        "ffmpeg",

        "-i",
        input_path,

        "-vn",

        "-acodec",
        "mp3",

        "-y",

        output_path
    ]

    process = subprocess.Popen(

        command,

        stderr=subprocess.PIPE,

        stdout=subprocess.PIPE,

        text=True
    )

    last_progress = -1

    while True:

        line = process.stderr.readline()

        if not line:
            break

        print(line.strip())

        match = re.search(
            r"time=(\d+):(\d+):(\d+\.\d+)",
            line
        )

        if match:

            hours = int(match.group(1))
            minutes = int(match.group(2))
            seconds = float(match.group(3))

            current_time = (
                    hours * 3600
                    + minutes * 60
                    + seconds
            )

            progress = int(
                (current_time / duration)
                * 100
            )

            if progress > last_progress:

                last_progress = progress

                print(
                    f"Progress: {progress}%"
                )

                send_progress(
                    job["jobId"],
                    progress
                )

    process.wait()

    if process.returncode != 0:

        raise Exception(
            "FFmpeg audio extraction failed"
        )

    processed_key = (
        f"processed/{audio_name}"
    )

    print("Uploading audio...")

    client.fput_object(

        BUCKET,

        processed_key,

        output_path
    )

    os.remove(input_path)
    os.remove(output_path)

    return processed_key