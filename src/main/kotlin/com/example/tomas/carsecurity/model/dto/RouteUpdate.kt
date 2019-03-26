package com.example.tomas.carsecurity.model.dto

/** Data class used for updating route in database. */
data class RouteUpdate(

        /** Database identification number of route. */
        val id: Long,

        /** Note about route. */
        val note: String = ""
)