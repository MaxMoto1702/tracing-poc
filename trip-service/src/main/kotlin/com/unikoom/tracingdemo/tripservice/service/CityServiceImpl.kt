package com.unikoom.tracingdemo.tripservice.service

import mu.KotlinLogging
import org.springframework.cloud.sleuth.annotation.NewSpan
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Service
class CityServiceImpl(
    private val cityClient: WebClient,
    private val placeClient: WebClient,
    private val eventClient: WebClient,
    private val routeClient: WebClient,
    private val tripBuilderClient: WebClient,
)  : CityService {
    val log = KotlinLogging.logger { }

//    @NewSpan
    override suspend fun getAll(): List<Any> {
        return cityClient.get().uri("/cities").retrieve().awaitBodyOrNull() ?: emptyList()
    }
}
