package com.unikoom.tracingdemo.tripservice.service

interface EventService {
    suspend fun getAll(): List<Any>
}
