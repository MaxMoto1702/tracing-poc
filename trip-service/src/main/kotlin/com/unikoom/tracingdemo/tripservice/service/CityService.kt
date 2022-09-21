package com.unikoom.tracingdemo.tripservice.service

interface CityService {
    suspend fun getAll(): List<Any>
}
