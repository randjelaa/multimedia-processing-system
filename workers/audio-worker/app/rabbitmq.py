import time
import pika

from config import RABBITMQ_HOST

while True:
    try:
        print("Connecting to RabbitMQ...")

        connection = pika.BlockingConnection(
            pika.ConnectionParameters(
                host=RABBITMQ_HOST
            )
        )

        print("Connected to RabbitMQ")
        break

    except pika.exceptions.AMQPConnectionError:
        print("RabbitMQ not ready, retrying in 5 seconds...")
        time.sleep(5)

channel = connection.channel()