package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.repository.EventRepository
import com.example.tomas.carsecurity.repository.PositionRepository
import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventControllerTest : BaseControllerTest() {

    private val logger = LoggerFactory.getLogger(EventControllerTest::class.java)

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var positionRepository: PositionRepository

    /** Number of events in DB */
    private var eventsCount = 0L
    /** Number of positions in DB */
    private var positionsCount = 0L

    @BeforeAll
    fun prepare() {
        eventsCount = eventRepository.count()
        positionsCount = positionRepository.count()
    }

    @Test
    fun `create new event success without position`() {

        logger.info("Testing creating of new event without position")

        val requestBody = "{\n" +
                "    \"eventTypeId\": 1,\n" +
                "    \"time\": \"2019-01-27T10:15:30\",\n" +
                "    \"carId\": 1,\n" +
                "    \"note\": \"zmizelo auto\"\n" +
                "}"

        val url = getUrl(EVENT_MAPPING)
        logger.debug("Request url: $url")
        logger.debug("Request params: $requestBody")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestData = HttpEntity(requestBody, headers)
        val result = testTemplate.postForEntity(url, requestData, String::class.java)

        // Testing
        assertEquals(201, result.statusCodeValue)
        assertEquals(null, result.body)

        assertEquals(eventsCount + 1, eventRepository.count())
        assertEquals(positionsCount, positionRepository.count())

        val events = eventRepository.findAll(Sort(Sort.Direction.DESC, "id"))
        assertEquals(1L, events.first().eventType.id)
        assertEquals(1L, events.first().car.id)
        assertEquals("zmizelo auto", events.first().note)
//        assertNull(events.first().position)
        assertEquals(2019, events.first().time.year)
        assertEquals(1, events.first().time.month?.value)
        assertEquals(27, events.first().time.dayOfMonth)
        assertEquals(10, events.first().time.hour)
        assertEquals(15, events.first().time.minute)
        assertEquals(30, events.first().time.second)
        assertEquals(0, events.first().time.nano)

        // Cleaning
        logger.debug("Removing created event")
        eventRepository.delete(events.first())
        assertEquals(eventsCount, eventRepository.count())
    }

    @Test
    fun `create new event success with position`() {

        logger.info("Testing creating of new event with position")

        val requestBody = "{\n" +
                "    \"eventTypeId\": 1,\n" +
                "    \"time\": \"2019-01-27T10:15:30\",\n" +
                "    \"carId\": 1,\n" +
                "    \"note\": \"zmizelo auto\",\n" +
                "    \"position\": {\n" +
                "        \"accuracy\": 1.1,\n" +
                "        \"altitude\": 2.2,\n" +
                "        \"latitude\": 3.3,\n" +
                "        \"longitude\": 4.4,\n" +
                "        \"time\": \"2019-01-27T10:15:30\",\n" +
                "        \"speed\": 5.5\n" +
                "    }\n" +
                "}"

        val url = getUrl(EVENT_MAPPING)
        logger.debug("Request url: $url")
        logger.debug("Request params: $requestBody")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestData = HttpEntity(requestBody, headers)
        val result = testTemplate.postForEntity(url, requestData, String::class.java)

        // Testing
        assertEquals(201, result.statusCodeValue)
        assertEquals(null, result.body)

        assertEquals(eventsCount + 1, eventRepository.count())
        assertEquals(positionsCount + 1, positionRepository.count())

        val events = eventRepository.findAll(Sort(Sort.Direction.DESC, "id"))
        assertEquals(1L, events.first().eventType.id)
        assertEquals(1L, events.first().car.id)
        assertEquals("zmizelo auto", events.first().note)
//        assertNotNull(events.first().position)
//        assertEquals(1.1f, events.first().position?.accuracy)
//        assertEquals(2.2f, events.first().position?.altitude)
//        assertEquals(3.3f, events.first().position?.latitude)
//        assertEquals(4.4f, events.first().position?.longitude)
//        assertEquals(5.5f, events.first().position?.speed)
//        assertEquals(2019, events.first().position?.time?.year)
//        assertEquals(1, events.first().position?.time?.month?.value)
//        assertEquals(27, events.first().position?.time?.dayOfMonth)
//        assertEquals(10, events.first().position?.time?.hour)
//        assertEquals(15, events.first().position?.time?.minute)
//        assertEquals(30, events.first().position?.time?.second)
//        assertEquals(0, events.first().position?.time?.nano)

        // Cleaning
        logger.debug("Removing created event")
        eventRepository.delete(events.first())
        assertEquals(eventsCount, eventRepository.count())

        logger.debug("Removing created position")
//        positionRepository.delete(events.first().position!!)
        assertEquals(positionsCount, positionRepository.count())
    }

    @Test
    fun `create new event with position and missing values`() {

        logger.info("Testing creating of new event with position and missing values")

        val requestBody = "{\n" +
                "    \"eventTypeId\": 1,\n" +
                "    \"time\": \"2019-01-27T10:15:30\",\n" +
                "    \"carId\": 1,\n" +
                "    \"note\": \"zmizelo auto\",\n" +
                "    \"position\": {\n" +
                "        \"accuracy\": 1.1,\n" +
                "        \"latitude\": 3.3,\n" +
                "        \"longitude\": 4.4,\n" +
                "        \"time\": \"2019-01-27T10:15:30\",\n" +
                "        \"speed\": 5.5\n" +
                "    }\n" +
                "}"

        val url = getUrl(EVENT_MAPPING)
        logger.debug("Request url: $url")
        logger.debug("Request params: $requestBody")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestData = HttpEntity(requestBody, headers)
        val result = testTemplate.postForEntity(url, requestData, String::class.java)

        // Testing
        assertEquals(201, result.statusCodeValue)
        assertEquals(null, result.body)

        assertEquals(eventsCount + 1, eventRepository.count())
        assertEquals(positionsCount + 1, positionRepository.count())

        val events = eventRepository.findAll(Sort(Sort.Direction.DESC, "id"))
//        assertNotNull(events.first().position)
//        assertEquals(0.0f, events.first().position?.altitude)

        // Cleaning
        logger.debug("Removing created event")
        eventRepository.delete(events.first())
        assertEquals(eventsCount, eventRepository.count())

        logger.debug("Removing created position")
//        positionRepository.delete(events.first().position!!)
        assertEquals(positionsCount, positionRepository.count())
    }

    @Test
    fun `create new event with invalid car id`() {

        logger.info("Testing creating of new event with invalid car id")

        val requestBody = "{\n" +
                "    \"eventTypeId\": 1,\n" +
                "    \"time\": \"2019-01-27T10:15:30\",\n" +
                "    \"carId\": 111,\n" +
                "    \"note\": \"zmizelo auto\",\n" +
                "    \"position\": null\n" +
                "}"

        val url = getUrl(EVENT_MAPPING)
        logger.debug("Request url: $url")
        logger.debug("Request params: $requestBody")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestData = HttpEntity(requestBody, headers)
        val result = testTemplate.postForEntity(url, requestData, String::class.java)

        // Testing
        assertEquals(400, result.statusCodeValue)
        assertEquals(null, result.body)

        assertEquals(eventsCount, eventRepository.count())
        assertEquals(positionsCount, positionRepository.count())
    }

    @Test
    fun `create new event without car id`() {

        logger.info("Testing creating of new event without car id")

        val requestBody = "{\n" +
                "    \"eventTypeId\": 1,\n" +
                "    \"time\": \"2019-01-27T10:15:30\",\n" +
                "    \"note\": \"zmizelo auto\",\n" +
                "    \"position\": null\n" +
                "}"

        val url = getUrl(EVENT_MAPPING)
        logger.debug("Request url: $url")
        logger.debug("Request params: $requestBody")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestData = HttpEntity(requestBody, headers)
        val result = testTemplate.postForEntity(url, requestData, String::class.java)

        // Testing
        assertEquals(400, result.statusCodeValue)
        assertEquals(null, result.body)

        assertEquals(eventsCount, eventRepository.count())
        assertEquals(positionsCount, positionRepository.count())
    }

    @Test
    fun `create new event with invalid event type id`() {

        logger.info("Testing creating of new event with invalid event type id")

        val requestBody = "{\n" +
                "    \"eventTypeId\": 111,\n" +
                "    \"time\": \"2019-01-27T10:15:30\",\n" +
                "    \"carId\": 111,\n" +
                "    \"note\": \"zmizelo auto\",\n" +
                "    \"position\": null\n" +
                "}"

        val url = getUrl(EVENT_MAPPING)
        logger.debug("Request url: $url")
        logger.debug("Request params: $requestBody")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestData = HttpEntity(requestBody, headers)
        val result = testTemplate.postForEntity(url, requestData, String::class.java)

        // Testing
        assertEquals(400, result.statusCodeValue)
        assertEquals(null, result.body)

        assertEquals(eventsCount, eventRepository.count())
        assertEquals(positionsCount, positionRepository.count())
    }

    @Test
    fun `get event success`() {

        logger.info("Testing get of event")

        val request = HashMap<String, String>()
        request["event_id"] = "1"
        val url = addParams(getUrl(EVENT_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val event = JsonParser().parse(result.body).asJsonObject

        assertEquals(1L, event.get("id").asLong)
        assertEquals("jedna", event.get("note").asString)
        assertNull(event.get("time"))
        assertEquals(1L, event.get("car_id").asLong)
        assertEquals(1L, event.get("event_type_id").asLong)
        assertEquals(1L, event.get("position_id").asLong)
    }

    @Test
    fun `get event invalid event id`() {

        logger.info("Testing get of event with invalid id")

        val request = HashMap<String, String>()
        request["event_id"] = "111"
        val url = addParams(getUrl(EVENT_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(400, result.statusCodeValue)
        assertNull(result.body)
    }

    @Test
    fun `get events success`() {

        logger.info("Testing get of events")

        val request = HashMap<String, String>()
        request["car_id"] = "1"
        val url = addParams(getUrl(EVENT_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val events = JsonParser().parse(result.body).asJsonArray
        assertEquals(4, events.size())

        val expectedIds = arrayListOf(1, 2, 4, 6)
        for (event in events) {
            val id = event.asJsonObject.get("id").asInt
            assertTrue(expectedIds.contains(id))
            expectedIds.remove(id)
        }

        assertTrue(expectedIds.isEmpty())
    }

    @Test
    fun `get events with pages`() {

        logger.info("Testing get of events with pages")

        val request = HashMap<String, String>()
        request["car_id"] = "1"
        request["page"] = "0"
        request["limit"] = "5"
        val url = addParams(getUrl(EVENT_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val events = JsonParser().parse(result.body).asJsonArray
        assertEquals(4, events.size())

        val expectedIds = arrayListOf(1, 2, 4, 6)
        for (event in events) {
            val id = event.asJsonObject.get("id").asInt
            assertTrue(expectedIds.contains(id))
            expectedIds.remove(id)
        }

        assertTrue(expectedIds.isEmpty())
    }

    @Test
    fun `get events with pages of size 1`() {

        logger.info("Testing get of events with pages of size 1")

        val request = HashMap<String, String>()
        request["car_id"] = "1"
        request["page"] = "2"
        request["limit"] = "1"
        val url = addParams(getUrl(EVENT_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val events = JsonParser().parse(result.body).asJsonArray
        assertEquals(1, events.size())
        assertEquals(4, events[0].asJsonObject.get("id").asInt)
    }

    @Test
    fun `get events with pages of size 0`() {

        logger.info("Testing get of events with pages of size 0")

        val request = HashMap<String, String>()
        request["car_id"] = "1"
        request["page"] = "0"
        request["limit"] = "0"
        val url = addParams(getUrl(EVENT_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)
        val events = JsonParser().parse(result.body).asJsonArray
        assertEquals(1, events.size())
    }

    @Test
    fun `get events of car without events`() {

        logger.info("Testing get of events of car without events")

        val request = HashMap<String, String>()
        request["car_id"] = "3"
        val url = addParams(getUrl(EVENT_MAPPING), request)
        logger.debug("Request url: $url")

        val result = testTemplate.getForEntity(url, String::class.java)

        // Testing
        assertEquals(200, result.statusCodeValue)

        val events = JsonParser().parse(result.body).asJsonArray
        assertEquals(0, events.size())
    }
}