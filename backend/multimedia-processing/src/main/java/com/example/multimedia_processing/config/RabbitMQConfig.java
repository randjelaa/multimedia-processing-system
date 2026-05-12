package com.example.multimedia_processing.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.thumbnail}")
    private String thumbnailQueueName;

    @Value("${rabbitmq.queue.audio}")
    private String audioQueueName;

    @Value("${rabbitmq.queue.transcode}")
    private String transcodeQueueName;

    @Value("${rabbitmq.queue.results}")
    private String resultsQueueName;

    @Bean
    public Queue thumbnailQueue() {
        return new Queue(thumbnailQueueName);
    }

    @Bean
    public Queue audioQueue() {
        return new Queue(audioQueueName);
    }

    @Bean
    public Queue transcodeQueue() {
        return new Queue(transcodeQueueName);
    }

    @Bean
    public Queue resultsQueue() {
        return new Queue(resultsQueueName);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}