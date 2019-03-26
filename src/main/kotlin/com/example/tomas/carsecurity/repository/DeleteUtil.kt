package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Event
import com.example.tomas.carsecurity.model.Route
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DeleteUtil(
        private val carRepository: CarRepository,
        private val eventRepository: EventRepository,
        private val routeRepository: RouteRepository,
        private val positionRepository: PositionRepository
) {

    @Transactional
    fun deleteCars(cars: List<Car>) {

        val events = eventRepository.findAllByCarIn(cars)
        deleteEvents(events)

        val routes = routeRepository.findAllByCarIn(cars)
        deleteRoutes(routes)

        carRepository.deleteAll(cars)
    }

    @Transactional
    fun deleteEvents(events: List<Event>) {
        eventRepository.deleteAll(events)
    }

    @Transactional
    fun deleteRoutes(routes: List<Route>) {

        val positions = positionRepository.findAllByRouteIn(routes)
        positionRepository.deleteAll(positions)

        routeRepository.deleteAll(routes)
    }
}