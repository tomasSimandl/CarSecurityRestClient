package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Route
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository

/**
 * This repository is used for access route table in database.
 */
interface RouteRepository : PagingAndSortingRepository<Route, Long> {

    /**
     * Method return page of routes which was created with [carId].
     * Result is ordered descending by time.
     *
     * @param carId is identification of car which create requested routes.
     * @param pageable specification of which page of which size will be result.
     * @return page of found routes.
     */

//    @Query("SELECT DISTINCT r.* FROM route r " +
//            "INNER JOIN position p ON r.id = p.route_id " +
//            "WHERE r.car_id = ?1 " +
//            "ORDER BY r.time DESC", nativeQuery = true)
    fun findDistinctByCar_IdAndPositionsIsNotNullOrderByTimeDesc(carId: Long, pageable: Pageable): Page<Route>

    /**
     * Method return page of routes which were created with users cars.
     * Result is ordered descending by time.
     *
     * @param username of cars which created requested routes.
     * @param pageable specification of which page of which size will be result.
     * @return page of found routes.
     */
//    @Query("SELECT DISTINCT r.* FROM route r " +
//            "INNER JOIN car c ON r.car_id = c.id " +
//            "INNER JOIN position p ON r.id = p.route_id " +
//            "WHERE c.username = ?1 " +
//            "ORDER BY r.time DESC", nativeQuery = true)
    fun findDistinctByCar_UsernameAndPositionsIsNotNullOrderByTimeDesc(username: String, pageable: Pageable): Page<Route>

    /**
     * Method return number of routes which were created by cars which own [username].
     *
     * @param username of which routes will be calculated.
     * @return number of routes.
     */
//    @Query("SELECT COUNT( DISTINCT r.id) FROM route r " +
//            "INNER JOIN car c ON r.car_id = c.id " +
//            "INNER JOIN position p ON r.id = p.route_id " +
//            "WHERE c.username = ?1", nativeQuery = true)
    fun countDistinctByCar_UsernameAndPositionsIsNotNull(username: String): Long

    /**
     * Method return number of routes which were created by specific car.
     *
     * @param carId is identification of car of which number of routes is requested.
     * @return number of cars routes.
     */
//    @Query("SELECT COUNT( DISTINCT r.id) FROM route r " +
//            "INNER JOIN position p ON r.id = p.route_id " +
//            "WHERE r.car_id = ?1 ", nativeQuery = true)
    fun countDistinctByCar_IdAndPositionsIsNotNull(carId: Long): Long

    /**
     * Method return list of routes which were created by any car in [cars].
     *
     * @param cars of which routes will be returned.
     * @return list of found routes.
     */
    fun findAllByCarIn(cars: List<Car>): List<Route>
}