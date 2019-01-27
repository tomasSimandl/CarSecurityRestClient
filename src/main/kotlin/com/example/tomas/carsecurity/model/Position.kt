package com.example.tomas.carsecurity.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "position")
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["latitude", "longitude", "altitude", "time", "accuracy"])])
data class Position(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @ManyToOne
        val route: Route?,

        @Column(nullable = false)
        val latitude: Float = 0f,

        @Column(nullable = false)
        val longitude: Float = 0f,

        @Column(nullable = false)
        val altitude: Float = 0f,

        @Column(nullable = false)
        val time: LocalDateTime,

        @Column(nullable = false)
        val accuracy: Float = 0f,

        @Column(nullable = false)
        val speed: Float = 0f
)