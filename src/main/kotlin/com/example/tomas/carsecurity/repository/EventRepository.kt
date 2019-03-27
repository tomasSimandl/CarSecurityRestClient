package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Event
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository

/**
 * This repository is used for access event table in database.
 */
interface EventRepository : PagingAndSortingRepository<Event, Long> {

    /**
     * Method return list of events which was created by car specified by [carId].
     * Result is ordered descending by time.
     *
     * @param carId is identification of car which create requested events.
     * @param pageable specification of which page of which size will be result.
     * @return page of events which was created by input car.
     */
    @Query("SELECT * FROM event e WHERE e.car_id = ?1 ORDER BY e.time DESC", nativeQuery = true)
    fun findAllByCarId(carId: Long, pageable: Pageable): Page<Event>

    /**
     * Method return list of events of given user.
     * Result is ordered descending by time.
     *
     * @param username of user which is owner of cars which created requested events.
     * @param pageable specification of which page of which size will be result.
     * @return page of events which was created by input car.
     */
    fun findAllByCar_UsernameOrderByTimeDesc(username: String, pageable: Pageable): Page<Event>

    /**
     * Method returns all events which was created by any car in [cars].
     *
     * @param cars is list of cars which created requested events.
     * @return list of found events.
     */
    fun findAllByCarIn(cars: List<Car>): List<Event>
}