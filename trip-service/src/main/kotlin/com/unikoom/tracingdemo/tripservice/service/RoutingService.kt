package com.unikoom.tracingdemo.tripservice.service

interface RoutingService {
    suspend fun getRoutes(items: List<Any>): List<Any>
}
