package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

import java.util.*

@Controller
class RouteController {

    private val logger = LoggerFactory.getLogger(RouteController::class.java)

    @Autowired
    private lateinit var routeRepository: RouteRepository

    @Autowired
    private lateinit var carRepository: CarRepository

    @PostMapping(ROUTE_MAPPING)
    fun createRoute(@RequestParam(value = "car_id") carId: Long): String {

        logger.info("Creating new route.")

        val car = carRepository.findById(carId)
        if (!car.isPresent) {
            logger.debug("Car id does not exists.")
            return createJsonSingle("error", "Invalid parameters.")
        }

        val route = Route(0, null, null, ArrayList(), 0f, car.get())
        routeRepository.save(route)

        logger.debug("Created new route.")
        return createJsonSingle("route_id", route.id.toString())
    }


    @GetMapping(ROUTE_MAPPING, params = ["route_id"])
    fun getRoute(@RequestParam(value = "route_id") routeId: Long): String {

        val route = routeRepository.findById(routeId)
        if (!route.isPresent) {
            logger.debug("Route does not exists.")
            return createJsonSingle("error", "Invalid parameters.")
        }

        return Gson().toJson(route.get())
    }

    @GetMapping(ROUTE_MAPPING, params = ["car_id"])
    fun getRoutes(@RequestParam(value = "car_id") carId: Long,
                  @RequestParam(value = "page", defaultValue = "0") page: Int,
                  @RequestParam(value = "limit", defaultValue = "15") limit: Int): String {

        val routes = routeRepository.findAllByCarId(carId, PageRequest.of(page, limit))
        return Gson().toJson(routes)
    }
}