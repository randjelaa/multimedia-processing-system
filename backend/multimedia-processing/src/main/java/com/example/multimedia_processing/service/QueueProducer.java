package com.example.multimedia_processing.service;

import com.example.multimedia_processing.dto.JobMessage;
import com.example.multimedia_processing.entity.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendJob(
            JobMessage message,
            JobType type
    ) {

        String queueName = switch (type) {

            case THUMBNAIL -> "thumbnail.queue";

            case AUDIO -> "audio.queue";

            case TRANSCODE -> "transcode.queue";
        };

        rabbitTemplate.convertAndSend(
                queueName,
                message
        );
    }
}