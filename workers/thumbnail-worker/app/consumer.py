import json
import threading
from rabbitmq import channel, connection
from processor import process_thumbnail, abort_job_process
from rabbitmq_sender import send_done, send_failed

channel.queue_declare(queue='thumbnail.queue', durable=True)
channel.queue_declare(queue='jobs.results.queue', durable=True)

channel.exchange_declare(exchange='jobs.control.exchange', exchange_type='fanout')
result = channel.queue_declare(queue='', exclusive=True)
control_queue_name = result.method.queue
channel.queue_bind(exchange='jobs.control.exchange', queue=control_queue_name)

def job_worker_thread(job, delivery_tag):
    job_id = job["jobId"]
    try:
        result_key = process_thumbnail(job)
        if result_key:
            send_done(job_id, result_key)
            print(f"DONE: Thumbnail for {job_id}")
    except Exception as e:
        print(f"ERROR in thread: {e}")
        send_failed(job_id)
    finally:
        connection.add_callback_threadsafe(lambda: channel.basic_ack(delivery_tag))

def job_callback(ch, method, properties, body):
    job = json.loads(body)
    print(f"New job received: {job['jobId']}")
    t = threading.Thread(target=job_worker_thread, args=(job, method.delivery_tag))
    t.start()

def control_callback(ch, method, properties, body):
    job_id = body.decode().strip().replace('"', '')
    print(f"Abort signal for: {job_id}")
    abort_job_process(job_id)

def start_consumer():
    channel.basic_consume(queue=control_queue_name, on_message_callback=control_callback, auto_ack=True)
    channel.basic_consume(queue='thumbnail.queue', on_message_callback=job_callback)

    print("Thumbnail worker is running and responsive...")
    channel.start_consuming()

if __name__ == "__main__":
    start_consumer()