package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.Route
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository

interface PositionRepository : PagingAndSortingRepository<Position, Long> {

    @Query("SELECT * FROM position p WHERE p.route_id = ?1", nativeQuery = true)
    fun findAllByRouteId(routeId: Long, pageable: Pageable): Page<Position>

    fun findAllByRouteIn(routes: List<Route>): List<Position>
}