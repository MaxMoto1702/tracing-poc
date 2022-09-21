package com.unikoom.tracingdemo.tripservice.service

interface TripBuildingService {
    fun handle(response: String, headers: Map<String, Any>)
    suspend fun asyncHandle(response: Map<String, Any>): Map<String, Any>
}
