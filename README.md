# Multimedia Processing Distributed System

Distributed microservice-based system for asynchronous multimedia processing.

Users can upload multimedia files, choose a processing type, and track job execution status in real time.  
The system uses a Spring Boot orchestrator, Angular frontend, RabbitMQ messaging, MinIO object storage, and multiple Python processing microservices.

---

# Features

- JWT authentication
- Multimedia file upload
- Asynchronous processing using RabbitMQ
- Distributed processing microservices
- S3-compatible object storage using MinIO
- Job status tracking
- Real-time progress tracking
- Thumbnail extraction
- Audio extraction
- Video transcoding
- Abort / cancel processing support
- Docker Compose deployment
- Nginx reverse proxy

---

# Architecture

The system follows a distributed microservice architecture.

Spring Boot acts as the central orchestrator responsible for:
- authentication
- upload management
- job lifecycle management
- RabbitMQ communication
- status tracking
- MinIO integration
- progress updates
- abort coordination

Processing is delegated to independent Python worker services.  
Each worker listens to its own RabbitMQ queue and performs a specific multimedia operation.

The frontend communicates with the backend through an Nginx reverse proxy.  
Angular and Spring Boot containers communicate internally through the Docker network and are not directly exposed outside the infrastructure.

---

# System Architecture

```text
                         +-------------+
                         |    Nginx    |
                         +-------------+
                                |
                                |
                       +------------------+
                       |     Angular      |
                       +------------------+
                                |
                                |
                       +------------------+
                       |  Spring Boot API |
                       +------------------+
                         |      |       |
                         |      |       |
                    MySQL   RabbitMQ   MinIO
                                |         |
   ----------------------------------------------------------------
   |                         |                            |
+-------------+      +--------------+         +------------------+
| Thumbnail   |      | Audio Worker |         | Transcode Worker |
| Worker      |      |              |         |                  |
+-------------+      +--------------+         +------------------+
```

---

# Microservices

## Spring Boot Orchestrator

Responsible for:
- authentication
- upload management
- job lifecycle management
- RabbitMQ communication
- status tracking
- callback handling
- progress updates
- abort coordination

---

## Thumbnail Worker

Python microservice responsible for:
- extracting representative thumbnails from videos using FFmpeg

Queue:
- `thumbnail.queue`

---

## Audio Worker

Python microservice responsible for:
- extracting audio tracks from video files using FFmpeg

Queue:
- `audio.queue`

---

## Transcode Worker

Python microservice responsible for:
- transcoding videos to lower resolutions using FFmpeg

Queue:
- `transcode.queue`

---

# Infrastructure Components

## RabbitMQ

Used for asynchronous communication between the orchestrator and processing services.

Queues:
- `thumbnail.queue`
- `audio.queue`
- `transcode.queue`
- `jobs.results.queue`

Exchanges:
- `jobs.control.exchange`

The abort exchange is used to broadcast abort requests to all workers.  
The worker currently processing the requested job stops the processing task and reports the job as aborted.

---

## MinIO

S3-compatible object storage used for:
- uploaded multimedia files
- processed results

---

## MySQL

Stores:
- users
- jobs
- job statuses
- progress information
- metadata

---

## Nginx

Acts as a reverse proxy for the Angular frontend.

Only the Nginx container is exposed publicly.  
Angular and Spring Boot containers communicate internally through the Docker network.

---

# System Workflow

1. User uploads a multimedia file
2. Spring Boot uploads the file to MinIO
3. A processing job is created in MySQL
4. Spring Boot sends a message to RabbitMQ
5. The corresponding worker consumes the message
6. Worker downloads the file from MinIO
7. Worker processes the file using FFmpeg
8. Worker periodically sends progress updates
9. Processed result is uploaded back to MinIO
10. Worker sends callback result through RabbitMQ
11. Spring Boot updates the job status
12. Angular frontend polls job statuses every 2 seconds

---

# Abort Workflow

1. User clicks cancel/abort on the frontend
2. Spring Boot publishes an abort event to `abort.exchange`
3. All workers receive the abort event
4. The worker currently processing the target job interrupts processing
5. Worker reports the job as `ABORTED`
6. Spring Boot updates the database
7. Frontend displays the updated status

---

# Job Statuses

- `PENDING`
- `PROCESSING`
- `DONE`
- `FAILED`
- `ABORTED`

---

# Progress Tracking

Workers periodically send progress updates through `jobs.results.queue`.

Spring Boot updates the progress value in the database, while the Angular frontend retrieves updated job information using polling every 2 seconds.

---

# Processing Services

## Thumbnail Extraction

Extracts representative video thumbnails.

Example:
```text
video.mp4 -> video.jpg
```

---

## Audio Extraction

Extracts audio from video files.

Example:
```text
video.mp4 -> video.mp3
```

---

## Video Transcoding

Converts videos to lower resolutions.

Example:
```text
1080p -> 720p
```

---

# Technologies

## Backend
- Spring Boot
- Spring Security
- JWT Authentication
- Spring AMQP
- JPA / Hibernate

---

## Frontend
- Angular 17
- TypeScript

---

## Workers
- Python
- FFmpeg
- pika
- MinIO SDK

---

## Infrastructure
- Docker
- Docker Compose
- RabbitMQ
- MinIO
- MySQL
- Nginx

---

# Project Structure

```text
project-root/
│
├── backend/
├── frontend/
├── workers/
│   ├── thumbnail-worker/
│   ├── audio-worker/
│   └── transcode-worker/
│
└── docker-compose.yml
```

---

# Running the Application

## Start all services

```bash
docker compose up --build
```

---

# Access URLs

## Frontend

http://localhost

---

## RabbitMQ Management

http://localhost:15672

Default credentials:
```text
guest
guest
```

---

## MinIO Console

http://localhost:9001

Default credentials:
```text
admin
password
```

---

# Environment Variables

Example infrastructure configuration:

```env
RABBITMQ_HOST=rabbitmq

MINIO_HOST=minio:9000
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=password

MINIO_BUCKET=uploads
```

---

# Future Improvements

- WebSocket-based live updates
- Multiple transcoding resolutions
- OCR processing service
- Horizontal worker scaling
- Kubernetes deployment
- Distributed orchestration
