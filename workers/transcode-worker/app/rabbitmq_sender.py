import json

from rabbitmq import channel

def send_progress(
        job_id,
        progress
):

    message = {

        "jobId": job_id,

        "status": "PROCESSING",

        "progressPercentage": progress
    }

    channel.basic_publish(

        exchange='',

        routing_key=
        'jobs.results.queue',

        body=json.dumps(message)
    )


def send_done(
        job_id,
        result_key
):

    message = {

        "jobId": job_id,

        "status": "DONE",

        "resultFileKey": result_key,

        "progressPercentage": 100
    }

    channel.basic_publish(

        exchange='',

        routing_key=
        'jobs.results.queue',

        body=json.dumps(message)
    )


def send_failed(job_id):

    message = {

        "jobId": job_id,

        "status": "FAILED",

        "progressPercentage": 0
    }

    channel.basic_publish(

        exchange='',

        routing_key=
        'jobs.results.queue',

        body=json.dumps(message)
    )