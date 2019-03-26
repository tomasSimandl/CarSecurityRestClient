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
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * This controller is used for access and manage positions in database.
 * User is authorized to access only his own data.
 *
 * @param positionRepository is repository for access positions in database.
 * @param routeRepository is repository fo access routes in database.
 * @param uploadFileFolder is path to folder with images of map downloaded from Bing.
 */
@RestController
class PositionController(
        private val positionRepository: PositionRepository,
        private val routeRepository: RouteRepository,

        @Value("\${static.maps.upload.folder}")
        private val uploadFileFolder: String
) {

    /** Logger of this class */
    private val logger = LoggerFactory.getLogger(PositionController::class.java)
    /** Map for caching routes from database. */
    private val cacheRoutes = HashMap<Long, Route>()


    /**
     * Method take input array of positions and store it in database. When any positions contain null as a route id or
     * user has no privilege for requested route no position is stored and error code is returned.
     * Returned status code can be CREATED, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for creating positions.
     * @param response to creating positions request.
     * @param positions array of positions which will be stored to database.
     * @return empty string or json with error message on BAD_REQUEST.
     */
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

        if (principal.name == null) {
            logger.debug("User is not logged in.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val positionsToSave = ArrayList<Position>(positions.size)
        for (positionCreate in positions) {
            val route = if (positionCreate.routeId == null) {
                null
            } else {
                val tempRoute = getRoute(positionCreate.routeId)
                if (tempRoute == null) {
                    logger.debug("Route id does not exists")
                    response.status = HttpServletResponse.SC_BAD_REQUEST
                    return createJsonSingle("error", "Route ${positionCreate.routeId} does not exists.")

                } else if (principal.name != tempRoute.car.username) {
                    logger.debug("User: ${principal.name} is not owner of car: ${tempRoute.car.username}")
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    return ""
                }
                tempRoute
            }

            positionsToSave.add(Position(positionCreate, route))
        }

        try {
            logger.debug("Saving ${positionsToSave.size} positions to database.")
            positionRepository.saveAll(positionsToSave)

            removeMapOfEditedRoutes()
            removeCalculateDataOfEditedRoutes()
            response.status = HttpServletResponse.SC_CREATED
            return ""

        } catch (e: DataIntegrityViolationException) {
            logger.warn("DataIntegrityViolationException when saving positions to DB: ${e.message}")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Can not save. Integrity violation exception.")
        }
    }

    /**
     * Remove cached images of cached routes because route was changed.
     */
    private fun removeMapOfEditedRoutes() {
        for (route in cacheRoutes.values) {
            val file = File("$uploadFileFolder/route-${route.id}.png")
            FileUtils.deleteQuietly(file)
        }
    }

    /**
     * Method removes calculated data of all routes in cache, because route was chenged.
     */
    private fun removeCalculateDataOfEditedRoutes() {
        for (route in cacheRoutes.values) {
            route.removeStatistics()
        }

        routeRepository.saveAll(cacheRoutes.values)
    }

    /**
     * Method return page of positions given by route.
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for getting positions.
     * @param response to getting positions request.
     * @param routeId identification number of route.
     * @param page of positions divided by [limit].
     * @param limit of positions per page.
     * @return Json of positions, json with error message on BAD_REQUEST or empty string on UNAUTHORIZED.
     */
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
        if (!route.isPresent) {
            logger.debug("Requested route does not exists")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Route does not exists.")
        }

        if (principal.name == null || route.get().car.username != principal.name) {
            logger.debug("User: ${principal.name} is not allowed to get route: $routeId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val validLimit = if (limit <= 0) 1 else limit
        val positions = positionRepository.findAllByRouteId(routeId, PageRequest.of(page, validLimit))

        return Position.gson.toJson(positions.content)
    }


    /**
     * Method operate with cache [cacheRoutes]. When requested route is in cache, route is taken from cache.
     * When route is not in cache, route is load from database, stored in [cacheRoutes] and than returned.
     *
     * @param routeId identification of requested route.
     * @return route or null when route is not in database.
     */
    private fun getRoute(routeId: Long): Route? {

        if (cacheRoutes.containsKey(routeId)) {
            return cacheRoutes[routeId]
        }

        val route = routeRepository.findById(routeId)
        if (route.isPresent) {
            cacheRoutes[routeId] = route.get()
            return route.get()
        }

        return null
    }
}