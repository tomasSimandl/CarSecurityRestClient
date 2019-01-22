package com.example.tomas.carsecurity.model

import javax.persistence.*

@Entity(name = "route")
data class Route(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @OneToOne
        val startPosition: Position?,

        @OneToOne
        val endPosition: Position?,

        @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
        val positions: List<Position>,

        val length: Float = 0f,

        @ManyToOne
        val car: Car
)