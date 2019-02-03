package com.example.tomas.carsecurity.model.dto

data class EventCreate(

        val eventTypeId: Long,

        /** Milliseconds from 1.1.1970 in UTC */
        val time: Long,

        val position: PositionCreate?,

        val carId: Long,

        val note: String = ""
)