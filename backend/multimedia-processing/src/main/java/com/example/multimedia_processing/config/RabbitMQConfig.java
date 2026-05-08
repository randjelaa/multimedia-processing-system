package com.example.multimedia_processing.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String JOBS_QUEUE =
            "jobs.queue";

    public static final String RESULTS_QUEUE =
            "jobs.results.queue";

    @Bean
    public Queue jobsQueue() {

        return new Queue(JOBS_QUEUE);
    }

    @Bean
    public Queue resultsQueue() {

        return new Queue(RESULTS_QUEUE);
    }

    @Bean
    public JacksonJsonMessageConverter
    messageConverter() {

        return new JacksonJsonMessageConverter();
    }
}