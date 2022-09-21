package com.unikoom.tracingdemo.tripservice.service

interface PlaceService {
    suspend fun getAll(): List<Any>
}
