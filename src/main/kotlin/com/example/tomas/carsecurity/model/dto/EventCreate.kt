package com.example.tomas.carsecurity.model.dto

import java.security.Timestamp

data class EventCreate(

        val name: String = "",

        val eventTypeId: Long,

        val time: Timestamp?,

        val position: PositionCreate?,

        val carId: Long,

        val note: String = ""
)