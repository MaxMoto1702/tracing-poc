package com.unikoom.tracingdemo.routeservice

import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@SpringBootApplication
class RouteServiceApplication {
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
    runApplication<RouteServiceApplication>(*args)
}

@RequestMapping("durations")
@RestController
class DurationController(
    private val placeClient: WebClient,
) {
    val log = KotlinLogging.logger { }

    @PostMapping("matrix")
    suspend fun matrix(): List<List<Any>> {
        log.debug { "Get duration matrix" }
        // get places
        val places = placeClient.get().uri("/places").retrieve().awaitBodyOrNull<Any>() ?: emptyList<Any>()
        delay(1000)
        return emptyList()
    }
}

@RequestMapping("routes")
@RestController
class RouteController(
    private val placeClient: WebClient,
) {
    val log = KotlinLogging.logger { }

    @PostMapping
    suspend fun build(): List<Any> {
        log.debug { "Get routes" }
        // get places
        val places = placeClient.get().uri("/places").retrieve().awaitBodyOrNull<Any>() ?: emptyList<Any>()
        delay(1000)
        return emptyList()
    }
}
