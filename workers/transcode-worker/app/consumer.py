import json
import threading
from rabbitmq import channel, connection 
from processor import process_transcode, abort_job_process
from rabbitmq_sender import send_done, send_failed

channel.queue_declare(queue='transcode.queue', durable=True)
channel.exchange_declare(exchange='jobs.control.exchange', exchange_type='fanout')

result = channel.queue_declare(queue='', exclusive=True)
control_queue_name = result.method.queue
channel.queue_bind(exchange='jobs.control.exchange', queue=control_queue_name)

def job_worker_thread(job, delivery_tag):
    job_id = job["jobId"]
    try:
        result_key = process_transcode(job)
        if result_key:
            send_done(job_id, result_key)
            print(f"Job {job_id} completed.")
    except Exception as e:
        print("ERROR:", e)
        send_failed(job_id)
    finally:
        connection.add_callback_threadsafe(lambda: channel.basic_ack(delivery_tag))

def job_callback(ch, method, properties, body):
    job = json.loads(body)
    t = threading.Thread(target=job_worker_thread, args=(job, method.delivery_tag))
    t.start()

def control_callback(ch, method, properties, body):
    raw_id = body.decode().strip()
    job_id = raw_id.replace('"', '') 
    
    print(f"Control signal received: ABORT {job_id}")
    abort_job_process(job_id)

def start_consumer():
    channel.basic_consume(queue=control_queue_name, on_message_callback=control_callback, auto_ack=True)
    channel.basic_consume(queue='transcode.queue', on_message_callback=job_callback)

    print("Transcode worker started (listening for jobs and abort signals)...")
    channel.start_consuming()