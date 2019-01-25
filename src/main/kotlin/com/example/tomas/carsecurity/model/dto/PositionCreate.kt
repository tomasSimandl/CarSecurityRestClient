package com.example.tomas.carsecurity.model.dto

import java.security.Timestamp

data class PositionCreate(

        val latitude: Float = 0f,
        val longitude: Float = 0f,
        val altitude: Float = 0f,
        val time: Timestamp?,
        val accuracy: Float = 0f,
        val speed: Float = 0f,
        val route_id: Long? = null
)