package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.Route
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface PositionRepository : PagingAndSortingRepository<Position, Long> {

    @Query("SELECT * FROM position p WHERE p.route_id = ?1", nativeQuery = true)
    fun findAllByRouteId(routeId: Long, pageable: Pageable): Page<Position>

    fun findAllByRouteIn(routes: List<Route>): List<Position>

    fun findFirstByRouteOrderByTimeAsc(route: Route): Optional<Position>

    fun findFirstByRouteOrderByTimeDesc(route: Route): Optional<Position>

    @Query("SELECT SUM(p.distance) FROM position p WHERE p.route_id = ?1", nativeQuery = true)
    fun sumDistanceOfRoute(routeId: Long): Optional<Float>

    @Query ("SELECT AVG(p.speed) FROM position p WHERE p.route_id = ?1", nativeQuery = true)
    fun avgSpeedOfRoute(routeId: Long): Optional<Float>

    @Query("SELECT time_to_sec(timediff(max(p.time), min(p.time))) FROM position p WHERE p.route_id = ?1", nativeQuery = true)
    fun travelTimeOfRoute(routeId: Long): Optional<Long>
}