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

/**
 * This controller is used for access and manage routes in database.
 * User is authorized to access only his own data.
 *
 * @param positionRepository is repository for access positions in database.
 * @param routeRepository is repository for access routes in database.
 * @param carRepository is repository for access cars in database.
 * @param deleteUtil is used for removing route and all its positions from database.
 */
@RestController
class RouteController(
        private val routeRepository: RouteRepository,
        private val positionRepository: PositionRepository,
        private val carRepository: CarRepository,
        private val deleteUtil: DeleteUtil
) {

    /** Logger of this class */
    private val logger = LoggerFactory.getLogger(RouteController::class.java)

    /**
     * Method create new route in database.
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for creating route.
     * @param response to creating route request.
     * @param carId identification number of car which creates the route.
     * @param time is start time of route.
     * @return Json with id of created route, json with error message on BAD_REQUEST or empty string on UNAUTHORIZED.
     */
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

        var route = Route(positions = ArrayList(), time = zonedDateTime, car = car.get())
        route = routeRepository.save(route)

        logger.debug("New route created.")
        response.status = HttpServletResponse.SC_CREATED
        return createJsonSingle("route_id", route.id.toString())
    }

    /**
     * Method update note of route in database.
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for getting positions.
     * @param response to getting positions request.
     * @param routeUpdate is route which will be updated.
     * @return Empty string or json with error message on BAD_REQUEST.
     */
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
        if (!dbRoute.isPresent) {
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

    /**
     * Method delete given route from database.
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for remove route.
     * @param response to remove route request.
     * @param routeId identification number of route.
     * @return Empty string or json with error message on BAD_REQUEST.
     */
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

    /**
     * Method return page of routes of actual logged user.
     * Returned status code can be OK, UNAUTHORIZED
     *
     * @param principal of actual logged user.
     * @param request for getting routes of logged user.
     * @param response to getting routes of logged user request.
     * @param page of routes divided by [limit].
     * @param limit of routes per page.
     * @return Json of users routes or empty string on UNAUTHORIZED.
     */
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
        val routes = routeRepository.findDistinctByCar_UsernameAndPositionsIsNotNullOrderByTimeDesc(principal.name, PageRequest.of(page, validLimit))
        updateRouteStatistics(routes.content)
        return Route.gson.toJson(routes.content)
    }

    /**
     * Method return route of given id from database.
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for getting route.
     * @param response to getting route request.
     * @param routeId identification number of route.
     * @return Json with route, json with error message on BAD_REQUEST or empty string on UNAUTHORIZED.
     */
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

    /**
     * Method return page of routes of given car in database.
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for getting routes of car
     * @param response to getting reoutes of car request.
     * @param carId identification number of car.
     * @param page of routes divided by [limit].
     * @param limit of routes per page.
     * @return Json of routes, json with error message on BAD_REQUEST or empty string on UNAUTHORIZED.
     */
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
        val routes = routeRepository.findDistinctByCar_IdAndPositionsIsNotNullOrderByTimeDesc(carId, PageRequest.of(page, validLimit))
        updateRouteStatistics(routes.content)
        return Route.gson.toJson(routes.content)
    }

    /**
     * Method return number or routes of logged user in database.
     * Returned status code can be OK, UNAUTHORIZED
     *
     * @param principal of actual logged user.
     * @param request for getting number of routes.
     * @param response to getting number of routes request.
     * @return Json with number of routes or empty string on UNAUTHORIZED.
     */
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

        val routes = routeRepository.countDistinctByCar_UsernameAndPositionsIsNotNull(principal.name)
        return createJsonSingle("count", routes.toString())
    }

    /**
     * Method return number or routes of given car in database.
     * Returned status code can be OK, UNAUTHORIZED
     *
     * @param principal of actual logged user.
     * @param request for getting number of routes.
     * @param response to getting number of routes request.
     * @param carId identification of car of which routes we want to count.
     * @return Json with number of routes or empty string on UNAUTHORIZED.
     */
    @ResponseBody
    @GetMapping(ROUTE_COUNT_MAPPING, produces = ["application/json; charset=utf-8"], params = ["car_id"])
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

        val routes = routeRepository.countDistinctByCar_IdAndPositionsIsNotNull(carId)
        return createJsonSingle("count", routes.toString())
    }

    /**
     * Method update statistics of input routes in database by calling route method updateStatistics.

     * @param routes list of routes of which statistics will be updated.
     */
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