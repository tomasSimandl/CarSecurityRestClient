package com.example.tomas.carsecurity.model.dto

import java.time.LocalDateTime

data class EventCreate(

        val name: String = "",

        val eventTypeId: Long,

        val time: LocalDateTime,

        val position: PositionCreate?,

        val carId: Long,

        val note: String = ""
)