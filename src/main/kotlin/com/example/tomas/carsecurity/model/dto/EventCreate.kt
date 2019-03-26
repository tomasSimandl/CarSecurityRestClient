package com.example.tomas.carsecurity.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

/** Data class used for creating new events in database */
data class EventCreate(

        /** Identification number of event type */
        @JsonProperty("event_type_id")
        val eventTypeId: Long,

        /** Time when event happen. Milliseconds from 1.1.1970 in UTC */
        val time: Long,

        /** Identification of car associated with this event. */
        @JsonProperty("car_id")
        val carId: Long,

        /** Event note */
        val note: String = ""
)