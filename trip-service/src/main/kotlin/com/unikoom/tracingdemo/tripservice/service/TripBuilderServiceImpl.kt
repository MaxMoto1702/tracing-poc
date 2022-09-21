package com.unikoom.tracingdemo.tripservice.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class TripBuilderServiceImpl (
    private val cityClient: WebClient,
    private val placeClient: WebClient,
    private val eventClient: WebClient,
    private val routeClient: WebClient,
    private val tripBuilderClient: WebClient,
)  : TripBuilderService{
    val log = KotlinLogging.logger { }
}
