package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@Controller
class RouteController {

    private val logger = LoggerFactory.getLogger(RouteController::class.java)

    @Autowired
    private lateinit var routeRepository: RouteRepository

    @Autowired
    private lateinit var carRepository: CarRepository

    @PostMapping("/route/new")
    fun createRoute(@RequestParam(value = "car_id") carId: Long): Map<String, String> {

        logger.info("Creating new route.")

        val car = carRepository.findById(carId)
        if (!car.isPresent) {
            logger.debug("Car id does not exists.")
            return Collections.singletonMap("error", "Invalid parameters.")
        }

        val route = Route(0, null, null, ArrayList(), 0f, car.get())
        routeRepository.save(route)

        logger.debug("Created new route.")
        return Collections.singletonMap("route_id", route.id.toString())
    }

}