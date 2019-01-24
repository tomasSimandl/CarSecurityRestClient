package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.repository.RouteRepository
import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.LinkedMultiValueMap


class RouteControllerTest : BaseControllerTest() {

    private val logger = LoggerFactory.getLogger(RouteControllerTest::class.java)

    @Autowired
    private lateinit var routeRepository: RouteRepository

    @Test
    fun `create new route success`() {

        logger.info("Testing creating of new route")

        val request = LinkedMultiValueMap<String, String>()
        request.add("car_id", "1")
        val url = getUrl(ROUTE_MAPPING)
        logger.debug("Request url: $url")
        logger.debug("Request params: $request")

        val result = testTemplate.postForEntity(url, request, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val regex = Regex("\\{\"route_id\":\"(\\d+)\"}")
        assertTrue(result.body!!.contains(regex))

        logger.debug("Removing created route")
        val matchResult = regex.find(result.body!!)
        val id = matchResult!!.groupValues[1].toLong()

        assertNotEquals(0, id)
        routeRepository.deleteById(id)
    }

    @Test
    fun `create new route invalid car id`() {

        logger.info("Testing creating of new route with invalid car id")

        val request = LinkedMultiValueMap<String, String>()
        request.add("car_id", "111")
        val url = getUrl(ROUTE_MAPPING)
        logger.debug("Request url: $url")
        logger.debug("Request params: $request")

        val result = testTemplate.postForEntity(url, request, String::class.java)

        // Testing
        assertEquals(400, result.statusCodeValue)
        assertNull(result.body)
    }

    @Test
    fun `get route success`() {

        logger.info("Testing get of route")

        val request = HashMap<String, String>()
        request["route_id"] = "1"
        val url = addParams(getUrl(ROUTE_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val route = JsonParser().parse(result.body).asJsonObject

        assertEquals("0.0", route.get("length").asString)
        assertEquals("1", route.get("car_id").asString)
        assertEquals("2", route.get("start_position_id").asString)
        assertEquals("6", route.get("end_position_id").asString)
        assertNull(route.get("note"))
    }

    @Test
    fun `get route without positions`() {

        logger.info("Testing get of route without position")

        val request = HashMap<String, String>()
        request["route_id"] = "5"
        val url = addParams(getUrl(ROUTE_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val route = JsonParser().parse(result.body).asJsonObject

        assertEquals("0.0", route.get("length").asString)
        assertEquals("2", route.get("car_id").asString)
        assertFalse(route.has("start_position_id"))
        assertFalse(route.has("end_position_id"))
        assertNull(route.get("note"))
    }

    @Test
    fun `get route invalid route id`() {

        logger.info("Testing get of route with invalid id")

        val request = HashMap<String, String>()
        request["route_id"] = "111"
        val url = addParams(getUrl(ROUTE_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(400, result.statusCodeValue)
        assertNull(result.body)
    }

    @Test
    fun `get routes success`() {

        logger.info("Testing get of routes")

        val request = HashMap<String, String>()
        request["car_id"] = "1"
        val url = addParams(getUrl(ROUTE_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val routes = JsonParser().parse(result.body).asJsonArray
        assertEquals(3, routes.size())

        val expectedIds = arrayListOf(1, 2, 4)
        for (route in routes) {
            val id = route.asJsonObject.get("id").asInt
            assertTrue(expectedIds.contains(id))
            expectedIds.remove(id)
        }

        assertTrue(expectedIds.isEmpty())
    }

    @Test
    fun `get routes with pages`() {

        logger.info("Testing get of routes with pages")

        val request = HashMap<String, String>()
        request["car_id"] = "1"
        request["page"] = "0"
        request["limit"] = "2"
        val url = addParams(getUrl(ROUTE_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val routes = JsonParser().parse(result.body).asJsonArray
        assertEquals(2, routes.size())

        val expectedIds = arrayListOf(1, 2)
        for (route in routes) {
            val id = route.asJsonObject.get("id").asInt
            assertTrue(expectedIds.contains(id))
            expectedIds.remove(id)
        }

        assertTrue(expectedIds.isEmpty())
    }

    @Test
    fun `get routes with pages of size 1`() {

        logger.info("Testing get of routes with pages of size 1")

        val request = HashMap<String, String>()
        request["car_id"] = "1"
        request["page"] = "2"
        request["limit"] = "1"
        val url = addParams(getUrl(ROUTE_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val routes = JsonParser().parse(result.body).asJsonArray
        assertEquals(1, routes.size())
        assertEquals(4, routes[0].asJsonObject.get("id").asInt)
    }

    @Test
    fun `get routes with pages of size 0`() {

        logger.info("Testing get of routes with pages of size 0")

        val request = HashMap<String, String>()
        request["car_id"] = "1"
        request["page"] = "0"
        request["limit"] = "0"
        val url = addParams(getUrl(ROUTE_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)
        val routes = JsonParser().parse(result.body).asJsonArray
        assertEquals(1, routes.size())
    }

    @Test
    fun `get routes of car without routes`() {

        logger.info("Testing get of routes of car without routes")

        val request = HashMap<String, String>()
        request["car_id"] = "3"
        val url = addParams(getUrl(ROUTE_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val routes = JsonParser().parse(result.body).asJsonArray
        assertEquals(0, routes.size())
    }
}