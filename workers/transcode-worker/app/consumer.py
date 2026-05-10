import json
import pika

from processor import process_transcode
from config import RABBITMQ_HOST

connection = pika.BlockingConnection(

    pika.ConnectionParameters(
        host=RABBITMQ_HOST
    )
)

channel = connection.channel()

channel.queue_declare(
    queue='transcode.queue',
    durable=True
)

channel.queue_declare(
    queue='jobs.results.queue',
    durable=True
)


def callback(ch, method, properties, body):

    job = json.loads(body)

    print("Received job:", job)

    print("QUEUE JOB:", job)

    try:

        result_key = \
            process_transcode(job)

        result_message = {

            "jobId": job["jobId"],

            "status": "DONE",

            "resultFileKey": result_key
        }

        channel.basic_publish(

            exchange='',

            routing_key=
            'jobs.results.queue',

            body=json.dumps(
                result_message
            )
        )

        print("Transcode done.")

    except Exception as e:

        print("ERROR:", e)

        failed_message = {

            "jobId": job["jobId"],

            "status": "FAILED",

            "resultFileKey": None
        }

        channel.basic_publish(

            exchange='',

            routing_key=
            'jobs.results.queue',

            body=json.dumps(
                failed_message
            )
        )

    ch.basic_ack(
        delivery_tag=method.delivery_tag
    )


def start_consumer():

    print(
        "Transcode worker started..."
    )

    channel.basic_consume(

        queue='transcode.queue',

        on_message_callback=callback
    )

    channel.start_consuming()