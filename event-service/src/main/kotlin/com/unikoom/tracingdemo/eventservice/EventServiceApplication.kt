package com.unikoom.tracingdemo.eventservice

import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@SpringBootApplication
class EventServiceApplication {
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
    runApplication<EventServiceApplication>(*args)
}

/**
 * CRUD
 */
@RequestMapping("events")
@RestController
class EventsController(
    private val cityClient: WebClient,
) {
    val log = KotlinLogging.logger { }

    @GetMapping
    suspend fun list(): List<Any> {
        log.debug { "Get all events" }
        // get cities for events
        val cities = cityClient.get().uri("/cities").retrieve().awaitBodyOrNull<Any>() ?: emptyList<Any>()
        delay(1000)
        return emptyList()
    }

    @GetMapping("{id}")
    suspend fun get(@PathVariable id: String): Map<String, Any> {
        log.debug { "Get event $id" }
        // get city for event
        val cities = cityClient.get().uri("/cities").retrieve().awaitBodyOrNull<Any>() ?: emptyList<Any>()
        delay(1000)
        return mapOf()
    }
}
