package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.model.dto.PositionCreate
import com.example.tomas.carsecurity.repository.PositionRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class PositionController {

    private val logger = LoggerFactory.getLogger(PositionController::class.java)

    @Autowired
    private lateinit var positionRepository: PositionRepository

    @Autowired
    private lateinit var routeRepository: RouteRepository

    private val cacheRoutes = HashMap<Long, Route>()

    @PostMapping(POSITION_MAPPING)
    fun savePositions(@RequestBody positions: Array<PositionCreate>, request: HttpServletRequest,
                      response: HttpServletResponse) {

        assert(cacheRoutes.isEmpty())

        logger.info("Starts creating of new positions.")

        val savePositions = ArrayList<Position>(positions.size)

        for (positionCreate in positions) {

            val route = if (positionCreate.routeId == null) {
                null
            } else {
                val tempRoute = getRoute(positionCreate.routeId)
                if (tempRoute == null) {
                    logger.debug("Route id does not exists")
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    return
                }
                tempRoute
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
            response.status = HttpServletResponse.SC_CONFLICT
            return
        }

        response.status = HttpServletResponse.SC_CREATED
        return
    }

    @ResponseBody
    @GetMapping(POSITION_MAPPING, params = ["route_id"])
    fun getPositions(@RequestParam(value = "route_id") routeId: Long,
                     @RequestParam(value = "page", defaultValue = "0") page: Int,
                     @RequestParam(value = "limit", defaultValue = "15") limit: Int): String {

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