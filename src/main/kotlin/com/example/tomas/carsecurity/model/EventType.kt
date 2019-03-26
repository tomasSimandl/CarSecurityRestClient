package com.example.tomas.carsecurity.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

/**
 * Database entity of event type
 */
@Entity(name = "event_type")
data class EventType(

        /** Identification number of event type in database. */
        @Id
        val id: Long = 0,

        /** Name of event type. */
        @Column(nullable = false)
        val name: String = "",

        /** Description of event type. */
        @Column(nullable = false)
        val description: String = ""
)