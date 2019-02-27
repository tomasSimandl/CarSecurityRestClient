package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Route
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository

interface RouteRepository : PagingAndSortingRepository<Route, Long> {

    fun findAllByCar_IdOrderByTimeDesc(carId: Long, pageable: Pageable): Page<Route>

    fun findAllByCar_UsernameOrderByTimeDesc(username: String, pageable: Pageable): Page<Route>

    fun countByCar_Username(username: String): Long

    fun countByCar_Id(carId: Long): Long

    fun findAllByCarIn(cars: List<Car>): List<Route>
}