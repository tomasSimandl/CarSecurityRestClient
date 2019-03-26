package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.repository.PositionRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import com.example.tomas.carsecurity.service.MapService
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileInputStream
import java.security.Principal
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * This controller is used for loading static map from file or over Bing API.
 *
 * @param mapService service for communication with Bing API.
 * @param positionRepository repository for loading positions from local database.
 * @param routeRepository repository for loading routes from local database.
 * @param uploadFileFolder Path to folder where are stored loaded images.
 */
@RestController
class BingMapController(
        private val routeRepository: RouteRepository,
        private val positionRepository: PositionRepository,
        private val mapService: MapService,

        @Value("\${static.maps.upload.folder}")
        private val uploadFileFolder: String
) {

    /** Logger of this class */
    private val logger = LoggerFactory.getLogger(BingMapController::class.java)

    /**
     * Method load static map of given route. Map load from file. Where there is no stored map. Map is loaded from
     * Bing and stored to file for next use.
     *
     * User must be owner of car which creates the route.
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for static map.
     * @param response with static map.
     * @param routeId identification number of route.
     * @return ByteArray which map or empty ByteArray.
     */
    @ResponseBody
    @GetMapping(MAP_MAPPING)
    fun getStaticMap(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam("route_id") routeId: Long
    ): ByteArray {

        logger.info("Get routes static map request.")

        if (principal.name == null) {
            logger.debug("Can not get static map. User is not logged in.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ByteArray(0)
        }

        val route = routeRepository.findById(routeId)
        if (!route.isPresent) {
            logger.debug("Can not get static map. Route does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return ByteArray(0)
        }

        if (principal.name != route.get().car.username) {
            logger.debug("Can not get static map. Not owner of route.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ByteArray(0)
        }

        var map = loadMapFromFile(routeId)
        if (map.isEmpty()) {
            map = loadMapFromBing(route.get())
        }

        return map
    }

    private fun loadMapFromFile(routeId: Long): ByteArray {
        try {
            val inputStream = FileInputStream("$uploadFileFolder/route-$routeId.png")
            return IOUtils.toByteArray(inputStream)
        } catch (e: Exception) {
            logger.debug("Can not load data from resources.")
            return ByteArray(0)
        }
    }

    /**
     * Method load map from Bing according to given params.
     *
     * @param route of which will be created static map.
     * @return ByteArray of created map or empty when error occurs.
     */
    private fun loadMapFromBing(route: Route): ByteArray {

        val firstPosition = positionRepository.findFirstByRouteOrderByTimeAsc(route)
        val lastPosition = positionRepository.findFirstByRouteOrderByTimeDesc(route)
        var byteArrayMap = ByteArray(0)

        if (!firstPosition.isPresent || !lastPosition.isPresent) {
            logger.debug("Route without positions")
            return byteArrayMap
        }

        try {
            byteArrayMap = mapService.getStaticMap(firstPosition.get(), lastPosition.get())
            if (byteArrayMap.isNotEmpty()) {
                val file = File("$uploadFileFolder/route-${route.id}.png")
                FileUtils.writeByteArrayToFile(file, byteArrayMap)
            }

        } catch (e: Exception) {
            logger.error("Can not get data from bing:", e)
        }

        return byteArrayMap
    }
}