package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.model.dto.PositionCreate
import com.example.tomas.carsecurity.repository.PositionRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import java.io.File
import java.security.Principal
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class PositionController(
        private val positionRepository: PositionRepository,
        private val routeRepository: RouteRepository,

        @Value("\${static.maps.upload.folder}")
        private val uploadFileFolder: String
) {

    private val logger = LoggerFactory.getLogger(PositionController::class.java)
    private val cacheRoutes = HashMap<Long, Route>()

    @ResponseBody
    @PostMapping(POSITION_MAPPING, produces = ["application/json; charset=utf-8"])
    fun savePositions(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestBody positions: Array<PositionCreate>
    ): String {

        logger.info("Starts creating of new positions.")
        assert(cacheRoutes.isEmpty())

        if(principal.name == null){
            logger.debug("User is not logged in.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val savePositions = ArrayList<Position>(positions.size)
        for (positionCreate in positions) {

            val route = if (positionCreate.routeId == null) {
                null
            } else {
                val tempRoute = getRoute(positionCreate.routeId)
                if (tempRoute == null) {
                    logger.debug("Route id does not exists")
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    return createJsonSingle("error", "Route ${positionCreate.routeId} does not exists.")
                }
                tempRoute
            }

            if(route != null && principal.name != route.car.username) {
                logger.debug("User: ${principal.name} is not owner of car: ${route.car.username}")
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                return ""
            }

            val instant = Instant.ofEpochMilli(positionCreate.time)
            val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)

            val position = Position(0, route, positionCreate.latitude, positionCreate.longitude,
                    positionCreate.altitude, zonedDateTime, positionCreate.accuracy, positionCreate.speed)

            savePositions.add(position)
        }

        try {
            logger.debug("Saving ${savePositions.size} positions to database.")
            positionRepository.saveAll(savePositions)
        } catch (e: DataIntegrityViolationException) {
            logger.warn("DataIntegrityViolationException when saving positions to DB: ${e.message}")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Can not save. Integrity violation exception.")
        }

        removeMapOfEditedRoutes()
        response.status = HttpServletResponse.SC_CREATED
        return ""
    }

    /**
     * Remove cached images of routes because route was changed.
     */
    private fun removeMapOfEditedRoutes(){
        for (route in cacheRoutes.values) {
            val file = File("$uploadFileFolder/route-${route.id}.png")
            FileUtils.deleteQuietly(file)
        }
    }

    @ResponseBody
    @GetMapping(POSITION_MAPPING, params = ["route_id"], produces = ["application/json; charset=utf-8"])
    fun getPositions(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "route_id") routeId: Long,
            @RequestParam(value = "page", defaultValue = "0") page: Int,
            @RequestParam(value = "limit", defaultValue = "15") limit: Int
    ): String {

        val route = routeRepository.findById(routeId)
        if(!route.isPresent) {
            logger.debug("Requested route does not exists")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Route does not exists.")
        }

        if(principal.name == null || route.get().car.username != principal.name) {
            logger.debug("User: ${principal.name} is not allowed to get route: $routeId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val validLimit = if (limit <= 0) 1 else limit
        val positions = positionRepository.findAllByRouteId(routeId, PageRequest.of(page, validLimit))

        return Position.gson.toJson(positions.content)
    }


    private fun getRoute(routeId: Long): Route? {

        if (cacheRoutes.containsKey(routeId)) {
            return cacheRoutes[routeId]
        }

        val route = routeRepository.findById(routeId)
        if (route.isPresent) {
            return route.get()
        }

        return null
    }

}