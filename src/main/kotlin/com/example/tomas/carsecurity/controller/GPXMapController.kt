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


/**
 * Controller for exporting route in GPX format.
 *
 * @param routeRepository is repository for access route from database.
 */
@RestController
class GPXMapController(
        private val routeRepository: RouteRepository
) {

    /** Logger for this class */
    private val logger = LoggerFactory.getLogger(GPXMapController::class.java)

    /**
     * Method load route from database and return it in GPX format as a String.
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for getting GPX route.
     * @param response to getting GPX route request.
     * @param routeId identification number of route.
     * @return GPX format of route or empty string on error.
     */
    @ResponseBody
    @GetMapping(ROUTE_EXPORT_MAPPING)
    fun getGPXMap(
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

    /**
     * Method create and return GPX file format from input positions.
     *
     * @param positions of which is created GPX file.
     * @return String with GPX file of input [positions].
     */
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































