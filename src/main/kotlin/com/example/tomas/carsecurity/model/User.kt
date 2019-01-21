package com.example.tomas.carsecurity.model

import javax.persistence.*

@Entity(name = "user")
data class User(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
        val cars: List<Car>,

        val userName: String,
        val firstName: String,
        val surname: String,
        val password: String,
        val role: String
)