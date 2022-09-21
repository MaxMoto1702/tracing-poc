package com.unikoom.tracingdemo.cityservice

import brave.baggage.BaggageField
import brave.baggage.CorrelationScopeConfig
import brave.context.slf4j.MDCScopeDecorator
import brave.propagation.CurrentTraceContext
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

@SpringBootApplication
class CityServiceApplication {
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

    @Bean
    fun tripIdField(): BaggageField {
        return BaggageField.create("trip-id")
    }

    @Bean
    fun mdcScopeDecorator(): CurrentTraceContext.ScopeDecorator {
        return MDCScopeDecorator.newBuilder()
            .clear()
            .add(
                CorrelationScopeConfig.SingleCorrelationField.newBuilder(tripIdField())
                    .flushOnUpdate()
                    .build()
            )
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<CityServiceApplication>(*args)
}

/**
 * CRUD
 */
@RequestMapping("cities")
@RestController
class CityController {
    val log = KotlinLogging.logger { }

    @GetMapping
    suspend fun list(): List<Any> {
        log.debug { "Get all cities" }
        delay(1000)
        return emptyList()
    }

    @GetMapping("{id}")
    suspend fun get(@PathVariable id: String): Map<String, Any> {
        log.debug { "Get city $id" }
        delay(1000)
        return mapOf()
    }
}
