package com.example.tomas.carsecurity.model.dto

data class EventCreate(

        val name: String = "",

        val eventTypeId: Long,

        val time: String,

        val position: PositionCreate?,

        val carId: Long,

        val note: String = ""
)