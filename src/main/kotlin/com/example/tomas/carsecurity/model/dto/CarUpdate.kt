package com.example.tomas.carsecurity.model.dto

/** Data class used for update car in database */
data class CarUpdate(

        /** Identification database number of car */
        val id: Long = 0,

        /** Name of car */
        val name: String,

        /** Car note */
        val note: String = ""
)