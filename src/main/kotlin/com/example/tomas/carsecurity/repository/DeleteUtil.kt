package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Event
import com.example.tomas.carsecurity.model.Route
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Class is used for cascade removing of records in database.
 *
 * @param carRepository is repository used for access cars in database.
 * @param eventRepository is repository used for access events in database.
 * @param routeRepository is repository used for access routes in database.
 * @param positionRepository is repository used for access positions in database.
 */
@Component
class DeleteUtil(
        private val carRepository: CarRepository,
        private val eventRepository: EventRepository,
        private val routeRepository: RouteRepository,
        private val positionRepository: PositionRepository
) {

    /**
     * Method delete all cars in database specified by parameter [cars]. With car is deleted all its events and routes.
     *
     * @param cars is list of cars which will be deleted.
     */
    @Transactional
    fun deleteCars(cars: List<Car>) {

        val events = eventRepository.findAllByCarIn(cars)
        deleteEvents(events)

        val routes = routeRepository.findAllByCarIn(cars)
        deleteRoutes(routes)

        carRepository.deleteAll(cars)
    }

    /**
     * Method delete all input events in database.
     *
     * @param events list of events which will be deleted.
     */
    @Transactional
    fun deleteEvents(events: List<Event>) {
        eventRepository.deleteAll(events)
    }

    /**
     * Method delete all routes specified by parameter [routes]. With routes are deleted all its positions.
     *
     * @param routes which will be deleted in database.
     */
    @Transactional
    fun deleteRoutes(routes: List<Route>) {

        val positions = positionRepository.findAllByRouteIn(routes)
        positionRepository.deleteAll(positions)

        routeRepository.deleteAll(routes)
    }
}