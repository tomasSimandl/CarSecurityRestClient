package com.example.tomas.carsecurity.model

import java.security.Timestamp
import javax.persistence.*

@Entity(name = "event")
data class Event(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @ManyToOne
        val eventType: EventType,

        val time: Timestamp,

        @OneToOne
        val position: Position,

        @ManyToOne
        val car: Car
)