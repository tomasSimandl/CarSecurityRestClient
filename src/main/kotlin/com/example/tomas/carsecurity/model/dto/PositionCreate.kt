package com.example.tomas.carsecurity.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PositionCreate(

        val latitude: Float = 0f,
        val longitude: Float = 0f,
        val altitude: Float = 0f,
        /** Milliseconds from 1.1.1970 in UTC */
        val time: Long = 0L,
        val accuracy: Float = 0f,
        val distance: Float = 0f,
        val speed: Float = 0f,

        @JsonProperty("route_id")
        val routeId: Long? = null
)