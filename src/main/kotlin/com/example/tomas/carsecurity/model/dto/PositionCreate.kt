package com.example.tomas.carsecurity.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

/** Data class used for creating new positions. */
data class PositionCreate(

        /** Latitude of record */
        val latitude: Float = 0f,

        /** Longitude of record */
        val longitude: Float = 0f,

        /** Altitude of record */
        val altitude: Float = 0f,

        /** Time when was record recorded. Millis since 1.1.1970 in UTC*/
        val time: Long = 0L,

        /** Sensor accuracy of record */
        val accuracy: Float = 0f,

        /** Distance from last record */
        val distance: Float = 0f,

        /** Actual speed when record was created */
        val speed: Float = 0f,

        /** Identification number of route in database which is associated with this position. */
        @JsonProperty("route_id")
        val routeId: Long? = null
)