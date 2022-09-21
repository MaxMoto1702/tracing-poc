package com.unikoom.tracingdemo.tripservice.service

import mu.KotlinLogging
import org.springframework.cloud.sleuth.annotation.NewSpan
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Service
class PlaceServiceImpl(
    private val cityClient: WebClient,
    private val placeClient: WebClient,
    private val eventClient: WebClient,
    private val routeClient: WebClient,
    private val tripBuilderClient: WebClient,
)   : PlaceService{
    val log = KotlinLogging.logger { }

//    @NewSpan
    override suspend fun getAll(): List<Any> {
        return placeClient.get().uri("/places").retrieve().awaitBodyOrNull() ?: emptyList<Any>()
    }
}
