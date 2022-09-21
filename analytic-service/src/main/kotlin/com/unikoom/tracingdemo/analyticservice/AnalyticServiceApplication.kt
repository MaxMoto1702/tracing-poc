package com.unikoom.tracingdemo.analyticservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class AnalyticServiceApplication {
    @Bean
    fun analyticClient(): WebClient {
        return WebClient.create("http://localhost:8080/")
    }

    @Bean
    fun cityClient(): WebClient {
        return WebClient.create("http://localhost:8081/")
    }

    @Bean
    fun eventClient(): WebClient {
        return WebClient.create("http://localhost:8082/")
    }

    @Bean
    fun placeClient(): WebClient {
        return WebClient.create("http://localhost:8083/")
    }

    @Bean
    fun routeClient(): WebClient {
        return WebClient.create("http://localhost:8084/")
    }

    @Bean
    fun tripBuilderClient(): WebClient {
        return WebClient.create("http://localhost:8085/")
    }

    @Bean
    fun tripClient(): WebClient {
        return WebClient.create("http://localhost:8086/")
    }
}

fun main(args: Array<String>) {
    runApplication<AnalyticServiceApplication>(*args)
}
