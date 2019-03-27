package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Car
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository

/**
 * This repository is used for access car table in database.
 */
interface CarRepository : CrudRepository<Car, Long> {

    /**
     * Return page of cars which owner us specified by [username].
     *
     * @param username is owner of requested cars.
     * @param pageable setting which page of which size is requested.
     * @return page of found cars from database.
     */
    fun findAllByUsername(username: String, pageable: Pageable): Page<Car>
}