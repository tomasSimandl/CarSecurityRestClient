package com.example.tomas.carsecurity.model

import java.security.Timestamp
import javax.persistence.*

@Entity(name = "position")
data class Position(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @ManyToOne
        val route: Route,

        val latitude: Float = 0f,
        val longitude: Float = 0f,
        val altitude: Float = 0f,
        val time: Timestamp,
        val accuracy: Float = 0f,
        val speed: Float = 0f
)