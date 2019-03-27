package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.Route
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

/**
 * This repository is used for access position table in database.
 */
interface PositionRepository : PagingAndSortingRepository<Position, Long> {

    /**
     * Method return page of positions which were created on route specified by [routeId].
     *
     * @param routeId identification of route on which were positions created.
     * @param pageable specification of which page of which size will be result.
     * @return page of positions which were created on specific route.
     */
    @Query("SELECT * FROM position p WHERE p.route_id = ?1", nativeQuery = true)
    fun findAllByRouteId(routeId: Long, pageable: Pageable): Page<Position>

    /**
     * Method return list of all positions which were created on any route specified by [routes].
     *
     * @param routes list of routes on which were created requested positions.
     * @return list of found positions.
     */
    fun findAllByRouteIn(routes: List<Route>): List<Position>

    /**
     * Method return first created position of route.
     *
     * @param route is route of which first position is search.
     * @return Optional with found position or empty optional.
     */
    fun findFirstByRouteOrderByTimeAsc(route: Route): Optional<Position>

    /**
     * Method return last created position of route.
     *
     * @param route is route of which last position is search.
     * @return Optional with found position or empty optional.
     */
    fun findFirstByRouteOrderByTimeDesc(route: Route): Optional<Position>

    /**
     * Method calculate distance from all positions which was created of route specified by [routeId].
     *
     * @param routeId is identification of route which length is calculated.
     * @return Optional with length of route in meters.
     */
    @Query("SELECT SUM(p.distance) FROM position p WHERE p.route_id = ?1", nativeQuery = true)
    fun sumDistanceOfRoute(routeId: Long): Optional<Float>

    /**
     * Method calculate average speed of car on route specified by [routeId].
     *
     * @param routeId on which is calculated average speed.
     * @return Optional with average speed in meters per second.
     */
    @Query("SELECT AVG(p.speed) FROM position p WHERE p.route_id = ?1", nativeQuery = true)
    fun avgSpeedOfRoute(routeId: Long): Optional<Float>

    /**
     * Method return number of seconds as travel time on specific route.
     *
     * @param routeId of which time will be calculated.
     * @return Optional with travel time in seconds.
     */
    @Query("SELECT time_to_sec(timediff(max(p.time), min(p.time))) FROM position p WHERE p.route_id = ?1", nativeQuery = true)
    fun travelTimeOfRoute(routeId: Long): Optional<Long>
}