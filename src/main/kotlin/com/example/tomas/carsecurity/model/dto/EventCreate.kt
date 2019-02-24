package com.example.tomas.carsecurity.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EventCreate(

        @JsonProperty("event_type_id")
        val eventTypeId: Long,

        /** Milliseconds from 1.1.1970 in UTC */
        val time: Long,

        val position: PositionCreate?,

        val carId: Long,

        val note: String = ""
)