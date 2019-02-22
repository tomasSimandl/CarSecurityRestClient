package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.model.dto.RouteUpdate
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
class RouteController(
        private val routeRepository: RouteRepository,
        private val carRepository: CarRepository
) {

    private val logger = LoggerFactory.getLogger(RouteController::class.java)

    @ResponseBody
    @PostMapping(ROUTE_MAPPING, produces = ["application/json; charset=utf-8"])
    fun createRoute(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "car_id") carId: Long
    ): String {

        logger.info("Create new route request.")

        val car = carRepository.findById(carId)
        if (!car.isPresent) {
            logger.debug("Car id does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Car does not exists.")
        }

        if (principal.name == null || car.get().username != principal.name) {
            logger.debug("User: ${principal.name} is not owner of requested car: ${car.get().id}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        var route = Route(0, null, null, ArrayList(), 0f, car.get())
        route = routeRepository.save(route)

        logger.debug("New route created.")
        response.status = HttpServletResponse.SC_CREATED
        return createJsonSingle("route_id", route.id.toString())
    }

    @ResponseBody
    @PutMapping(ROUTE_MAPPING, produces = ["application/json; charset=utf-8"])
    fun updateRoutesNote(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestBody routeUpdate: RouteUpdate
    ): String {

        logger.info("Route update request.")

        val dbRoute = routeRepository.findById(routeUpdate.id)
        if(!dbRoute.isPresent) {
            logger.debug("Route does not exists")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Route does not exists")
        }

        if (principal.name == null || dbRoute.get().car.username != principal.name) {
            logger.debug("User: ${principal.name} is not owner of route: ${routeUpdate.id}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        dbRoute.get().note = routeUpdate.note
        routeRepository.save(dbRoute.get())

        logger.debug("Route updated.")
        return ""
    }

    @ResponseBody
    @GetMapping(ROUTE_MAPPING, produces = ["application/json; charset=utf-8"])
    fun getRoutesOfLogUser(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "page", defaultValue = "0") page: Int,
            @RequestParam(value = "limit", defaultValue = "15") limit: Int
    ): String {

        if (principal.name == null) {
            logger.debug("Principal is null.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val validLimit = if (limit <= 0) 1 else limit
        val routes = routeRepository.findAllByCar_Username(principal.name, PageRequest.of(page, validLimit))
        return Route.gson.toJson(routes.content)
    }


    @ResponseBody
    @GetMapping(ROUTE_MAPPING, params = ["route_id"], produces = ["application/json; charset=utf-8"])
    fun getRouteById(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "route_id") routeId: Long
    ): String {

        val route = routeRepository.findById(routeId)
        if (!route.isPresent) {
            logger.debug("Route does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Route does not exists.")
        }

        if (principal.name == null || route.get().car.username != principal.name) {
            logger.debug("User: ${principal.name} is not owner of requested route: $routeId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        return Route.gson.toJson(route.get())
    }

    @ResponseBody
    @GetMapping(ROUTE_MAPPING, params = ["car_id"], produces = ["application/json; charset=utf-8"])
    fun getRoutesByCarId(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "car_id") carId: Long,
            @RequestParam(value = "page", defaultValue = "0") page: Int,
            @RequestParam(value = "limit", defaultValue = "15") limit: Int
    ): String {

        val car = carRepository.findById(carId)
        if (!car.isPresent) {
            logger.debug("Car id does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Car does not exists.")
        }

        if (principal.name == null || car.get().username != principal.name) {
            logger.debug("User: ${principal.name} is not owner of car: $carId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val validLimit = if (limit <= 0) 1 else limit
        val routes = routeRepository.findAllByCarId(carId, PageRequest.of(page, validLimit))
        return Route.gson.toJson(routes.content)
    }
}