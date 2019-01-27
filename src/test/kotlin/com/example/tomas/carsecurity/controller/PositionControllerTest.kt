package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.repository.PositionRepository
import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PositionControllerTest : BaseControllerTest() {

    private val logger = LoggerFactory.getLogger(PositionControllerTest::class.java)

    @Autowired
    private lateinit var positionRepository: PositionRepository

    /** Number of positions in DB */
    private var positionsCount = 0L

    @BeforeAll
    fun prepare() {
        positionsCount = positionRepository.count()
    }

    @Test
    fun `create new positions success`() {

        logger.info("Testing creating of new positions")

        val accuracy = arrayOf(1.1f, 1.2f, 1.0f, 0.1f, 1000.1f)
        val altitude = arrayOf(2.2f, 2.3f, 2.4f, 2.6f, 199.2f)
        val latitude = arrayOf(3.3f, 3.4f, 3.5f, 3.8f, 212.3f)
        val longitude = arrayOf(4.4f, 4.5f, 4.6f, 4.9f, 456.4f)
        val time = arrayOf("2019-01-27T10:15:30", "2019-01-27T10:15:31", "2019-01-27T10:15:33", "2019-01-27T10:15:38", "2019-01-27T10:16:30")
        val speed = arrayOf(5.5f, 6.5f, 7.5f, 12.5f, 205.5f)
        val routeId = arrayOf(1L, 1L, 1L, 1L, null)

        val requestBody = createRequestBody(accuracy, altitude, latitude, longitude, time, speed, routeId)
        val result = postRequest(requestBody)

        // Testing
        assertEquals(201, result.statusCodeValue)
        assertEquals(null, result.body)

        assertEquals(positionsCount + 5, positionRepository.count())

        val positions = positionRepository.findAll(Sort(Sort.Direction.DESC, "id"))

        val positionsToDelete = ArrayList<Position>()
        val positionsIterator = positions.iterator()
        for (i in 0..4) {
            val position = positionsIterator.next()
            assertEquals(accuracy.reversedArray()[i], position.accuracy)
            assertEquals(altitude.reversedArray()[i], position.altitude)
            assertEquals(latitude.reversedArray()[i], position.latitude)
            assertEquals(longitude.reversedArray()[i], position.longitude)
            assertEquals(time.reversedArray()[i], position.time.toString())
            assertEquals(speed.reversedArray()[i], position.speed)
            assertEquals(routeId.reversedArray()[i], position.route?.id)

            positionsToDelete.add(position)
        }

        // Cleaning
        logger.debug("Removing created positions")
        positionRepository.deleteAll(positionsToDelete)
        assertEquals(positionsCount, positionRepository.count())
    }

    @Test
    fun `create new positions invalid route`() {
        logger.info("Testing creating of new positions with invalid route")

        val accuracy = arrayOf(1.1f, 1.2f, 1.0f, 0.1f, 1000.1f)
        val altitude = arrayOf(2.2f, 2.3f, 2.4f, 2.6f, 199.2f)
        val latitude = arrayOf(3.3f, 3.4f, 3.5f, 3.8f, 212.3f)
        val longitude = arrayOf(4.4f, 4.5f, 4.6f, 4.9f, 456.4f)
        val time = arrayOf("2019-01-27T10:15:30", "2019-01-27T10:15:31", "2019-01-27T10:15:33", "2019-01-27T10:15:38", "2019-01-27T10:16:30")
        val speed = arrayOf(5.5f, 6.5f, 7.5f, 12.5f, 205.5f)
        val routeId = arrayOf(1L, 1L, 111L, 1L, null)

        val requestBody = createRequestBody(accuracy, altitude, latitude, longitude, time, speed, routeId)
        val result = postRequest(requestBody)

        // Testing
        assertEquals(400, result.statusCodeValue)
        assertEquals(null, result.body)
        assertEquals(positionsCount, positionRepository.count())
    }

    @Test
    fun `create new positions one position`() {
        logger.info("Testing creating of one new positions")

        val accuracy = arrayOf(1.1f)
        val altitude = arrayOf(2.2f)
        val latitude = arrayOf(3.3f)
        val longitude = arrayOf(4.4f)
        val time = arrayOf("2019-01-27T10:15:30")
        val speed = arrayOf(5.5f)
        val routeId: Array<Long?> = arrayOf(1L)

        val requestBody = createRequestBody(accuracy, altitude, latitude, longitude, time, speed, routeId)
        val result = postRequest(requestBody)

        // Testing
        assertEquals(201, result.statusCodeValue)
        assertEquals(null, result.body)

        assertEquals(positionsCount + 1, positionRepository.count())

        val positions = positionRepository.findAll(Sort(Sort.Direction.DESC, "id"))

        val position = positions.first()
        assertEquals(accuracy.first(), position.accuracy)
        assertEquals(altitude.first(), position.altitude)
        assertEquals(latitude.first(), position.latitude)
        assertEquals(longitude.first(), position.longitude)
        assertEquals(time.first(), position.time.toString())
        assertEquals(speed.first(), position.speed)
        assertEquals(routeId.first(), position.route?.id)

        // Cleaning
        logger.debug("Removing created position")
        positionRepository.delete(position)
        assertEquals(positionsCount, positionRepository.count())
    }

    @Test
    fun `create new positions duplicate positions`() {

        logger.info("Testing creating of new positions with duplicate records")

        val accuracy = arrayOf(1.1f, 1.2f, 1.0f, 0.1f, 1.1f)
        val altitude = arrayOf(2.2f, 2.3f, 2.4f, 2.6f, 2.2f)
        val latitude = arrayOf(3.3f, 3.4f, 3.5f, 3.8f, 3.3f)
        val longitude = arrayOf(4.4f, 4.5f, 4.6f, 4.9f, 4.4f)
        val time = arrayOf("2019-01-27T10:15:30", "2019-01-27T10:15:31", "2019-01-27T10:15:33", "2019-01-27T10:15:38", "2019-01-27T10:15:30")
        val speed = arrayOf(5.5f, 6.5f, 7.5f, 12.5f, 5.5f)
        val routeId: Array<Long?> = arrayOf(1L, 1L, 1L, 1L, 1L)

        val requestBody = createRequestBody(accuracy, altitude, latitude, longitude, time, speed, routeId)
        val result = postRequest(requestBody)

        // Testing
        assertEquals(409, result.statusCodeValue)
        assertEquals(null, result.body)
        assertEquals(positionsCount, positionRepository.count())
    }

    private fun createRequestBody(accuracy: Array<Float>, altitude: Array<Float>, latitude: Array<Float>,
                                  longitude: Array<Float>, time: Array<String>, speed: Array<Float>,
                                  routeId: Array<Long?>): String {

        val requestBody = StringBuilder("[")

        for (i in 0..(accuracy.size - 1)) {
            requestBody.append("{\n")
            requestBody.append("   \"accuracy\": ${accuracy[i]},\n")
            requestBody.append("   \"altitude\": ${altitude[i]},\n")
            requestBody.append("   \"latitude\": ${latitude[i]},\n")
            requestBody.append("   \"longitude\": ${longitude[i]},\n")
            requestBody.append("   \"time\": \"${time[i]}\",\n")
            requestBody.append("   \"speed\": ${speed[i]},\n")
            requestBody.append("   \"routeId\": ${routeId[i]}\n")
            requestBody.append("}")
            if (i != accuracy.size - 1) requestBody.append(",")
        }
        requestBody.append("]")

        return requestBody.toString()
    }

    private fun postRequest(requestBody: String): ResponseEntity<String> {
        val url = getUrl(POSITION_MAPPING)
        logger.debug("Request url: $url")
        logger.debug("Request params: $requestBody")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestData = HttpEntity(requestBody, headers)
        return testTemplate.postForEntity(url, requestData, String::class.java)
    }

    @Test
    fun `get positions success`() {

        logger.info("Testing get of positions")

        val request = HashMap<String, String>()
        request["route_id"] = "1"
        val url = addParams(getUrl(POSITION_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val positions = JsonParser().parse(result.body).asJsonArray
        assertEquals(5, positions.size())

        val expectedIds = arrayListOf(2, 3, 4, 5, 6)
        for (position in positions) {
            val id = position.asJsonObject.get("id").asInt
            Assertions.assertTrue(expectedIds.contains(id))
            expectedIds.remove(id)
        }

        Assertions.assertTrue(expectedIds.isEmpty())
    }

    @Test
    fun `get events with pages`() {

        logger.info("Testing get of positions with pages")

        val request = HashMap<String, String>()
        request["route_id"] = "1"
        request["page"] = "1"
        request["limit"] = "1"
        val url = addParams(getUrl(POSITION_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val positions = JsonParser().parse(result.body).asJsonArray
        assertEquals(1, positions.size())
        assertEquals(3, positions[0].asJsonObject.get("id").asInt)
    }

    @Test
    fun `get events with pages of size 0`() {

        logger.info("Testing get of positions with pages of size 0")

        val request = HashMap<String, String>()
        request["route_id"] = "1"
        request["page"] = "0"
        request["limit"] = "0"
        val url = addParams(getUrl(POSITION_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val positions = JsonParser().parse(result.body).asJsonArray
        assertEquals(1, positions.size())
        assertEquals(2, positions[0].asJsonObject.get("id").asInt)
    }

    @Test
    fun `get positions of route without positions`() {

        logger.info("Testing get of positions of route without positions")

        val request = HashMap<String, String>()
        request["route_id"] = "5"
        val url = addParams(getUrl(POSITION_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val positions = JsonParser().parse(result.body).asJsonArray
        assertEquals(0, positions.size())
    }

    @Test
    fun `get positions of not existing route`() {

        logger.info("Testing get of positions of not existing route")

        val request = HashMap<String, String>()
        request["route_id"] = "111"
        val url = addParams(getUrl(POSITION_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val positions = JsonParser().parse(result.body).asJsonArray
        assertEquals(0, positions.size())
    }
}