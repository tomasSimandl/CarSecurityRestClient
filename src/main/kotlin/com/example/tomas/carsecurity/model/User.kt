package com.example.tomas.carsecurity.model

import javax.persistence.*

@Entity(name = "user")
data class User(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
        val cars: List<Car> = ArrayList(),

        @Column(nullable = false)
        val userName: String,

        @Column(nullable = false)
        val firstName: String,

        @Column(nullable = false)
        val surname: String,

        @Column(nullable = false)
        val password: String,

        @Column(nullable = false)
        val role: String
)