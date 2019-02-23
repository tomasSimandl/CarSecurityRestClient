package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.maps.service.MapService
import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.PositionRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.security.Principal
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
class MapController(
        private val routeRepository: RouteRepository,
        private val positionRepository: PositionRepository,
        private val mapService: MapService,

        @Value("\${static.maps.upload.folder}")
        private val uploadFileFolder: String
) {

    private val logger = LoggerFactory.getLogger(MapController::class.java)

    @ResponseBody
    @GetMapping(MAP_MAPPING, produces = ["image/png"])
    fun createStaticMap(
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
        if(!route.isPresent) {
            logger.debug("Can not get static map. Route does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return ByteArray(0)
        }

        if(principal.name != route.get().car.username) {
            logger.debug("Can not get static map. Not owner of route.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ByteArray(0)
        }

        var map = loadMapFromFile(routeId)
        if(map.isEmpty()){
           map = loadMapFromBing(route.get())
        }

        return map
    }

    private fun loadMapFromFile(routeId: Long): ByteArray {
        try {
            val inputStream = FileInputStream("$uploadFileFolder/route-$routeId.png")
            return IOUtils.toByteArray(inputStream)
        }catch (e: Exception) {
            logger.debug("Can not load data from resources.")
            return ByteArray(0)
        }
    }

    private fun loadMapFromBing(route: Route): ByteArray {

        val firstPosition = positionRepository.findFirstByRouteOrderByTimeAsc(route)
        val lastPosition = positionRepository.findFirstByRouteOrderByTimeDesc(route)
        var byteArrayMap = ByteArray(0)

        if(!firstPosition.isPresent || !lastPosition.isPresent) {
            logger.debug("Route without positions")
            return byteArrayMap
        }

        try {
            byteArrayMap =  mapService.getStaticMap(firstPosition.get(), lastPosition.get())
            if(byteArrayMap.isNotEmpty()) {
                val file = File("$uploadFileFolder/route-${route.id}.png")
                FileUtils.writeByteArrayToFile(file, byteArrayMap)
            }

        } catch (e: Exception) {
            logger.error("Can not get data from bing:", e)
        }

        return byteArrayMap
    }
}