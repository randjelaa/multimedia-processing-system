import json
import pika
from config import RABBITMQ_HOST


QUEUE = "jobs.results.queue"


def _publish(message: dict):
    connection = None
    try:
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(host=RABBITMQ_HOST)
        )

        channel = connection.channel()

        channel.queue_declare(queue=QUEUE, durable=True)

        channel.basic_publish(
            exchange='',
            routing_key=QUEUE,
            body=json.dumps(message),
            properties=pika.BasicProperties(
                delivery_mode=2  # make message persistent
            )
        )

    finally:
        if connection and connection.is_open:
            connection.close()


def send_progress(job_id, progress):
    message = {
        "jobId": job_id,
        "status": "PROCESSING",
        "progressPercentage": progress
    }
    _publish(message)


def send_done(job_id, result_key):
    message = {
        "jobId": job_id,
        "status": "DONE",
        "resultFileKey": result_key,
        "progressPercentage": 100
    }
    _publish(message)


def send_failed(job_id):
    message = {
        "jobId": job_id,
        "status": "FAILED",
        "progressPercentage": 0
    }
    _publish(message)