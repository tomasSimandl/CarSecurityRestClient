package com.example.tomas.carsecurity.model

import javax.persistence.*

@Entity(name = "event_type")
data class EventType(

        @Id
        val id: Long = 0,

        @Column(nullable = false)
        val name: String = "",

        @Column(nullable = false)
        val description: String = ""
)