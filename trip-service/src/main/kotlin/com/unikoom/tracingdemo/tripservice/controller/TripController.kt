package com.unikoom.tracingdemo.tripservice.controller

import brave.baggage.BaggageField
import com.unikoom.tracingdemo.tripservice.service.CityService
import com.unikoom.tracingdemo.tripservice.service.EventService
import com.unikoom.tracingdemo.tripservice.service.PlaceService
import com.unikoom.tracingdemo.tripservice.service.RoutingService
import com.unikoom.tracingdemo.tripservice.service.TripBuilderService
import com.unikoom.tracingdemo.tripservice.service.TripBuildingService
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import java.util.UUID

@RequestMapping("trips")
@RestController
class TripController(
    private val tripBuilderClient: WebClient,
    private val queueMessagingTemplate: QueueMessagingTemplate,
//    private val jmsMessagingTemplate: JmsMessagingTemplate,
    private val tripIdField: BaggageField,
    private val cityService: CityService,
    private val placeService: PlaceService,
    private val routingService: RoutingService,
    private val eventService: EventService,
) {
    val log = KotlinLogging.logger { }

    @GetMapping("{id}")
    suspend fun get(@PathVariable id: String): Map<String, Any?> {
        log.debug { "Get trip $id" }
        tripIdField.updateValue(id)
        //  - get trip from db
        val trip = mapOf<String, Any>()
        //  - get cities for trip
        val cities = cityService.getAll()
        //  - get places for trip
        val places = placeService.getAll()
        //  - get events for trip
        val events = eventService.getAll()
        delay(1000)
        return mapOf(
            "places" to places,
            "events" to events,
            "cities" to cities,
            "trip" to trip,
        )
    }

    @PostMapping("generate")
    suspend fun generate(
        @RequestBody request: Map<String, Any>,
        @RequestParam(required = false) isAsync: Boolean?,
    ): Map<String, Any> {
        log.debug { "Generate trip by $request" }
        // by request:
        //  - get cities
        val cities = cityService.getAll()
        log.debug { "Cities $cities" }
        //  - build routes between cities
        val crossCityRoutes = routingService.getRoutes(cities)
        //  - send request to trip builder service over aws
        val tripId = UUID.randomUUID().toString()
        log.debug { "Trip ID $tripId" }
        tripIdField.updateValue(tripId.toString())
        delay(1000)
        when (isAsync) {
            true -> queueMessagingTemplate.convertAndSend(
                "local-max-demo-rq",
                mapOf(
                    "id" to tripId,
                    "crossCityRoutes" to crossCityRoutes,
                ),
                mapOf("tripId" to tripId),
            )

            else -> tripBuilderClient
                .post()
                .uri("/build-trip")
                .bodyValue(
                    mapOf(
                        "id" to tripId,
                        "crossCityRoutes" to crossCityRoutes,
                    )
                )
                .retrieve()
                .awaitBodyOrNull<Any>()
        }
//        jmsMessagingTemplate.convertAndSend(
//            "local-max-demo-rq",
//            mapOf<String, Any>(),
//            mapOf("tripId" to tripId),
//        )
        return mapOf("tripId" to tripId)
    }
}
