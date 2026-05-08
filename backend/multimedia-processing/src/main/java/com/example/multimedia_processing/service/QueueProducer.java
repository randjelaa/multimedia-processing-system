package com.example.multimedia_processing.service;

import com.example.multimedia_processing.config.RabbitMQConfig;
import com.example.multimedia_processing.dto.JobMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendJob(JobMessage message) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.JOBS_QUEUE,
                message
        );
    }
}