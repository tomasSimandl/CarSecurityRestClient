package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Event
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository

interface EventRepository : PagingAndSortingRepository<Event, Long> {

    @Query("SELECT * FROM event e WHERE e.car_id = ?1 ORDER BY e.time DESC", nativeQuery = true)
    fun findAllByCarId(carId: Long, pageable: Pageable): Page<Event>

    fun findAllByCar_UsernameOrderByTimeDesc(username: String, pageable: Pageable): Page<Event>

    fun findAllByCarIn(cars: List<Car>): List<Event>
}