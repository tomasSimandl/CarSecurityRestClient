package com.example.tomas.carsecurity.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

/**
 * Configuration of RestTemplate for sending http requests.
 */
@Configuration
class RestTemplateConfig {

    /**
     * Basic RestTemplate for sending http requests.
     */
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
