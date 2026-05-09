package com.example.multimedia_processing.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String THUMBNAIL_QUEUE =
            "thumbnail.queue";

    public static final String AUDIO_QUEUE =
            "audio.queue";

    public static final String TRANSCODE_QUEUE =
            "transcode.queue";

    public static final String RESULTS_QUEUE =
            "jobs.results.queue";

    @Bean
    public Queue thumbnailQueue() {

        return new Queue(THUMBNAIL_QUEUE);
    }

    @Bean
    public Queue audioQueue() {

        return new Queue(AUDIO_QUEUE);
    }

    @Bean
    public Queue transcodeQueue() {

        return new Queue(TRANSCODE_QUEUE);
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