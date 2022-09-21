package com.unikoom.tracingdemo.tripservice.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Service
class RoutingServiceImpl(
    private val cityClient: WebClient,
    private val placeClient: WebClient,
    private val eventClient: WebClient,
    private val routeClient: WebClient,
    private val tripBuilderClient: WebClient,
)   : RoutingService{
    val log = KotlinLogging.logger { }
    override suspend fun getRoutes(items: List<Any>): List<Any> {
        return routeClient.post().uri("/routes").retrieve().awaitBodyOrNull() ?: emptyList()
    }
}
