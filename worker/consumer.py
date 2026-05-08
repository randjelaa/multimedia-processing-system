import pika
import json

from processor import process_job

connection = pika.BlockingConnection(

    pika.ConnectionParameters(
        host='localhost'
    )
)

channel = connection.channel()

channel.queue_declare(
    queue='jobs.queue',
    durable=True
)


def callback(ch, method, properties, body):

    job = json.loads(body)

    print("Received job:", job)

    try:

        result_key = process_job(job)

        print("Processing done")

        result_message = {

            "jobId": job["jobId"],

            "status": "DONE",

            "resultFileKey": result_key
        }

        channel.basic_publish(
            exchange='',
            routing_key='jobs.results.queue',
            body=json.dumps(result_message)
        )

    except Exception as e:

        print("Error:", e)

        failed_message = {

            "jobId": job["jobId"],

            "status": "FAILED",

            "resultFileKey": None
        }

        channel.basic_publish(
            exchange='',
            routing_key='jobs.results.queue',
            body=json.dumps(failed_message)
        )

    ch.basic_ack(
        delivery_tag=method.delivery_tag
    )


def start_consumer():

    print("Worker started...")

    channel.basic_consume(
        queue='jobs.queue',
        on_message_callback=callback
    )

    channel.start_consuming()