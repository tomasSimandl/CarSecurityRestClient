package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Route
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository

interface RouteRepository : PagingAndSortingRepository<Route, Long> {

    @Query("SELECT * FROM route r WHERE r.car_id = ?1", nativeQuery = true)
    fun findAllByCarId(carId: Long, pageable: Pageable): Page<Route>

    fun findAllByCar_Username(username: String, pageable: Pageable): Page<Route>
}