package com.example.tomas.carsecurity.model

import javax.persistence.*

@Entity(name = "car")
data class Car(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @ManyToOne
        val user: User,

        @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
        val routes: List<Route>,

        @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
        val events: List<Event>,

        val name: String,

        val icon: String
)