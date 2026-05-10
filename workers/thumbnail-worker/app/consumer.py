import json
import pika

from processor import process_thumbnail

from config import RABBITMQ_HOST

from rabbitmq_sender import (
    send_done,
    send_failed
)

from rabbitmq import channel

channel.queue_declare(
    queue='thumbnail.queue',
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
            process_thumbnail(job)

        send_done(
            job["jobId"],
            result_key
        )

        print("Thumbnail done.")

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
        "Thumbnail worker started..."
    )

    channel.basic_consume(

        queue='thumbnail.queue',

        on_message_callback=callback
    )

    channel.start_consuming()