package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.repository.RouteRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.LinkedMultiValueMap


class RouteControllerTest : BaseControllerTest() {

    private val logger = LoggerFactory.getLogger(RouteControllerTest::class.java)

    @Autowired
    private lateinit var routeRepository: RouteRepository

    @Test
    fun createRoute() {

        logger.info("Testing creating of new route")

        val request = LinkedMultiValueMap<String, String>()
        request.add("car_id", "1")
        val url = getUrl(ROUTE_MAPPING)
        logger.debug("Request url: $url")
        logger.debug("Request params: $request")

        val result = testTemplate.postForEntity(url, request, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val regex = Regex("\\{\"route_id\":\"(\\d+)\"\\}")
        assertTrue(result.body!!.contains(regex))

        logger.debug("Removing created route")
        val matchResult = regex.find(result.body!!)
        routeRepository.deleteById(matchResult!!.groupValues[1].toLong())
    }
}