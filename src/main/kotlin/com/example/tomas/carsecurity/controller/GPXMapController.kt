package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.repository.RouteRepository
import io.jenetics.jpx.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.security.Principal
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
class GPXMapController(
        private val routeRepository: RouteRepository
) {

    private val logger = LoggerFactory.getLogger(GPXMapController::class.java)

    @ResponseBody
    @GetMapping(ROUTE_EXPORT_MAPPING)
    fun createStaticMap(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam("route_id") routeId: Long
    ): String {

        logger.info("Export map to GPX request.")

        if (principal.name == null) {
            logger.debug("Can not export map. User is not logged in.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val route = routeRepository.findById(routeId)
        if (!route.isPresent) {
            logger.debug("Can not export map. Route does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return ""
        }

        if (principal.name != route.get().car.username) {
            logger.debug("Can not export map. Not owner of route.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        return createGPX(route.get().positions)
    }


    private fun createGPX(positions: List<Position>): String {

        val trackSegment = TrackSegment.builder()
        positions.forEach { position ->
            trackSegment.addPoint(
                    WayPoint.of(
                            Latitude.ofDegrees(position.latitude.toDouble()),
                            Longitude.ofDegrees(position.longitude.toDouble()),
                            Length.of(position.altitude.toDouble(), Length.Unit.METER),
                            position.time
                    )
            )
        }

        val gpx = GPX.builder()
                .addTrack { track ->
                    track.addSegment(trackSegment.build())
                }
                .build()

        val stream = ByteArrayOutputStream()
        GPX.write(gpx, stream)
        return String(stream.toByteArray())
    }
}































