import json

from processor import process_transcode
from config import RABBITMQ_HOST
from rabbitmq_sender import (
    send_done,
    send_failed
)
from rabbitmq import channel

channel.queue_declare(
    queue='transcode.queue',
    durable=True
)

channel.queue_declare(
    queue='jobs.results.queue',
    durable=True
)


def callback(
        ch,
        method,
        properties,
        body
):

    job = json.loads(body)

    print("Received job:", job)

    try:

        result_key = \
            process_transcode(job)

        send_done(
            job["jobId"],
            result_key
        )

        print("Transcode done.")

    except Exception as e:

        print("ERROR:", e)

        send_failed(
            job["jobId"]
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