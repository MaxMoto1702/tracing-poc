package com.unikoom.tracingdemo.placeservice

import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.sleuth.annotation.NewSpan
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@SpringBootApplication
class PlaceServiceApplication {
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
    runApplication<PlaceServiceApplication>(*args)
}

/**
 * CRUD
 */
@RequestMapping("places")
@RestController
class PlaceController(
    private val cityClient: WebClient,
    private val eventClient: WebClient,
    private val cityService: CityService,
) {
    val log = KotlinLogging.logger { }

    @GetMapping
    suspend fun list(): List<Any> {
        log.debug { "Get all places" }
        // get cities for places
//        val cities = cityClient.get().uri("/cities").retrieve().awaitBodyOrNull<Any>() ?: emptyList<Any>()
        val cities = cityService.getAll()
        delay(1000)
        return emptyList()
    }

    @GetMapping("{id}")
    suspend fun get(@PathVariable id: String): Map<String, Any> {
        log.debug { "Get place $id" }
        // get city for place
//        val cities = cityClient.get().uri("/cities").retrieve().awaitBodyOrNull<Any>() ?: emptyList<Any>()
        val cities = cityService.getAll()
        // get events for place
        val events = eventClient.get().uri("/events").retrieve().awaitBodyOrNull<Any>() ?: emptyList<Any>()
        delay(1000)
        return mapOf()
    }
}

interface CityService {
    suspend fun getAll(): Any
}

@Service
class CityServiceImpl(
    private val cityClient: WebClient,
) : CityService {
    val log = KotlinLogging.logger { }

//    @NewSpan
    override suspend fun getAll(): Any {
        return cityClient.get().uri("/cities").retrieve().awaitBodyOrNull<Any>() ?: emptyList<Any>()
    }

}
