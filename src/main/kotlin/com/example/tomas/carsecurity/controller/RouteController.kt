package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.model.dto.RouteUpdate
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.DeleteUtil
import com.example.tomas.carsecurity.repository.PositionRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
class RouteController(
        private val routeRepository: RouteRepository,
        private val positionRepository: PositionRepository,
        private val carRepository: CarRepository,
        private val deleteUtil: DeleteUtil
) {

    private val logger = LoggerFactory.getLogger(RouteController::class.java)

    @ResponseBody
    @PostMapping(ROUTE_MAPPING, produces = ["application/json; charset=utf-8"])
    fun createRoute(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "car_id") carId: Long,
            @RequestParam(value = "time") time: Long
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

        val instant = Instant.ofEpochMilli(time)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)

        var route = Route( positions = ArrayList(), time = zonedDateTime, car = car.get())
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
    @DeleteMapping(ROUTE_MAPPING, params = ["route_id"], produces = ["application/json; charset=utf-8"])
    fun deleteRouteById(
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

        deleteUtil.deleteRoutes(listOf(route.get()))
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
        val routes = routeRepository.findAllByCar_UsernameOrderByTimeDesc(principal.name, PageRequest.of(page, validLimit))
        updateRouteStatistics(routes.content)
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

        val routeList = listOf(route.get())
        updateRouteStatistics(routeList)
        return Route.gson.toJson(routeList.first())
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
        val routes = routeRepository.findAllByCar_IdOrderByTimeDesc(carId, PageRequest.of(page, validLimit))
        updateRouteStatistics(routes.content)
        return Route.gson.toJson(routes.content)
    }

    @ResponseBody
    @GetMapping(ROUTE_COUNT_MAPPING, produces = ["application/json; charset=utf-8"])
    fun countRoutesOfLogUser(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse
    ): String {

        if (principal.name == null) {
            logger.debug("Principal is null.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }


        val routes = routeRepository.countByCar_Username(principal.name)
        return createJsonSingle("count", routes.toString())
    }

    @ResponseBody
    @GetMapping(ROUTE_COUNT_MAPPING, produces = ["application/json; charset=utf-8"])
    fun countRoutesByCar(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "car_id") carId: Long
    ): String {

        if (principal.name == null) {
            logger.debug("Principal is null.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val routes = routeRepository.countByCar_Id(carId)
        return createJsonSingle("count", routes.toString())
    }
    private fun updateRouteStatistics(routes: List<Route>) {

        val routesToUpdate: MutableList<Route> = mutableListOf()

        for (route in routes) {
            if (route.updateStatistics(positionRepository)) {
                routesToUpdate.add(route)
            }
        }

        if (routesToUpdate.isNotEmpty()) {
            logger.debug("Updating route statistics.")
            routeRepository.saveAll(routesToUpdate)
        }
    }
}