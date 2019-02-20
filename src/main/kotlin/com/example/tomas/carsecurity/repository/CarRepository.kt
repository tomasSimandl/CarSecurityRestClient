package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Car
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository

interface CarRepository : CrudRepository<Car, Long> {

    fun findAllByUsername(username: String, pageable: Pageable): Page<Car>
}