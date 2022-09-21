package com.unikoom.tracingdemo.tripservice.service

import brave.baggage.BaggageField
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
//import org.springframework.jms.annotation.JmsListener
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Service
class TripBuildingServiceImpl(
    private val tripIdField: BaggageField,
    private val objectMapper: ObjectMapper,
    private val routingService: RoutingService,
    private val placeService: PlaceService,
    private val eventService: EventService,
    private val cityService: CityService,
) : TripBuildingService {
    val log = KotlinLogging.logger { }

    @SqsListener("local-max-demo-rs")
//    @JmsListener(destination = "local-max-demo-rq")
    override fun handle(response: String, @Headers headers: Map<String, Any>) = runBlocking<Unit> {
        log.debug { "Handle trip-builder response $response with headers $headers" }
        log.debug { "Trip ID ${headers["tripId"]}" }
        tripIdField.updateValue(headers["tripId"]?.toString())
        asyncHandle(objectMapper.readValue(response))
    }

    override suspend fun asyncHandle(response: Map<String, Any>): Map<String, Any> {
        log.debug { "Trip built on trip-builder $response" }
        // trip-builder response populate
        //  - from city-service
        val cities = cityService.getAll()
        //  - from place-service
        val places = placeService.getAll()
        //  - from event-service
        val events = eventService.getAll()
        //  - from route-service
        val routes = routingService.getRoutes(places + events)
        // store in db
        return mapOf(
            "cities" to cities,
            "places" to places,
            "events" to events,
            "routes" to routes,
            "trip" to mapOf<String, Any>()
        )
    }
}
