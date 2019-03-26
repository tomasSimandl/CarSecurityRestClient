package com.example.tomas.carsecurity.model.dto

/** Data class used for create car requests. */
data class CarCreate(

        /** Name of car */
        val name: String,

        /** Note of car. */
        val note: String = ""
)