package com.example.multimedia_processing.messaging;

import com.example.multimedia_processing.dto.JobMessage;
import com.example.multimedia_processing.entity.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.thumbnail}")
    private String thumbnailQueue;

    @Value("${rabbitmq.queue.audio}")
    private String audioQueue;

    @Value("${rabbitmq.queue.transcode}")
    private String transcodeQueue;

    @Value("${rabbitmq.exchange.control}")
    private String controlExchange;

    public void sendJob(JobMessage message, JobType type) {

        String queueName = switch (type) {
            case THUMBNAIL -> thumbnailQueue;
            case AUDIO -> audioQueue;
            case TRANSCODE -> transcodeQueue;
        };

        rabbitTemplate.convertAndSend(
                queueName,
                message
        );
    }

    public void sendAbortSignal(String jobId) {

        rabbitTemplate.convertAndSend(
                controlExchange,
                "",
                jobId
        );
    }
}