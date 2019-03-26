package com.example.tomas.carsecurity.model.dto

/** Data class used for updating existing events in database */
data class EventUpdate(

        /** Database identification number of event. */
        val id: Long,

        /** Event note. */
        val note: String = ""
)