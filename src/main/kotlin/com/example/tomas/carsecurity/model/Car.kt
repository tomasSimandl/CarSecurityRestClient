package com.example.tomas.carsecurity.model

import javax.persistence.*

@Entity(name = "car")
data class Car(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @ManyToOne()
        @JoinColumn(nullable = false)
        val user: User,

        @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
        val routes: List<Route> = ArrayList(),

        @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
        val events: List<Event> = ArrayList(),

        @Column(nullable = false)
        val name: String = "",

        @Column(nullable = false)
        val icon: String = ""
)