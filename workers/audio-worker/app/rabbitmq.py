import pika

from config import RABBITMQ_HOST

connection = pika.BlockingConnection(
    pika.ConnectionParameters(
        host=RABBITMQ_HOST
    )
)

channel = connection.channel()