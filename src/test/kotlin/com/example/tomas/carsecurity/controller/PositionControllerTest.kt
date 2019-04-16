package com.example.tomas.carsecurity.controller


import com.example.tomas.carsecurity.anyKotlin
import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.model.dto.PositionCreate
import com.example.tomas.carsecurity.repository.PositionRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.grpc.internal.JsonParser
import org.apache.http.auth.BasicUserPrincipal
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.Times
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.io.File
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import javax.servlet.http.HttpServletResponse

class PositionControllerTest {

    @Mock
    private lateinit var positionRepository: PositionRepository
    @Mock
    private lateinit var routeRepository: RouteRepository


    private val uploadFileFolder: String = this.javaClass.classLoader.getResource("bing_map").path

    private lateinit var positionController: PositionController

    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    private val positions = ArrayList<Position>()
    private val car = Car(id = 48, username = "Emanuel")
    private val route = Route(id = 823, time = ZonedDateTime.now(), car = car, positions = positions)
    private val createPositions = ArrayList<PositionCreate>()


    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()

        positionController = PositionController(positionRepository, routeRepository, uploadFileFolder)

        val positionTime1 = ZonedDateTime.of(1111, 11, 11, 11, 11, 11, 0, ZoneOffset.UTC)
        val positionTime2 = ZonedDateTime.of(2222, 2, 22, 22, 22, 22, 0, ZoneOffset.UTC)
        val positionTime3 = ZonedDateTime.of(3333, 3, 3, 3, 33, 33, 0, ZoneOffset.UTC)

        positions.add(Position(1, route, 1f, 2f, 3f, positionTime1, 4f, 5f, 6f))
        positions.add(Position(2, route, 7f, 8f, 9f, positionTime2, 10f, 11f, 12f))
        positions.add(Position(3, route, 13f, 14f, 15f, positionTime3, 16f, 17f, 18f))

        createPositions.add(PositionCreate(1f, 2f, 3f, positionTime1.toEpochSecond() * 1000, 4f, 5f, 6f, 823))
        createPositions.add(PositionCreate(7f, 8f, 9f, positionTime2.toEpochSecond() * 1000, 10f, 11f, 12f, 23))
        createPositions.add(PositionCreate(13f, 14f, 15f, positionTime3.toEpochSecond() * 1000, 16f, 17f, 18f, 823))
    }

    @Test
    fun `save position null principal`() {

        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(null).`when`(principal).name

        val jsonResponse = positionController.savePositions(principal, request, response, createPositions.toTypedArray())

        verify(positionRepository, Times(0)).saveAll(anyKotlin(positions::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `save position not existing route`() {
        val principal = BasicUserPrincipal("Emanuel")

        val routeOptional: Optional<Route> = Optional.empty()

        doReturn(routeOptional).`when`(routeRepository).findById(23)
        doReturn(Optional.of(route)).`when`(routeRepository).findById(823)

        val jsonResponse = positionController.savePositions(principal, request, response, createPositions.toTypedArray())

        verify(routeRepository, Times(2)).findById(anyKotlin(Long::class.java))
        verify(positionRepository, Times(0)).saveAll(anyKotlin(positions::class.java))
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `save position not owner of route`() {
        val principal = BasicUserPrincipal("Zdeněk")

        doReturn(Optional.of(route)).`when`(routeRepository).findById(823)

        val jsonResponse = positionController.savePositions(principal, request, response, createPositions.toTypedArray())

        verify(routeRepository, Times(1)).findById(823)
        verify(routeRepository, Times(0)).findById(23)
        verify(positionRepository, Times(0)).saveAll(anyKotlin(positions::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `save position success`() {
        val principal = BasicUserPrincipal("Emanuel")
        createPositions[1] = createPositions[1].copy(routeId = null)

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val file = File("$uploadFileFolder/route-${route.id}.png")
        file.createNewFile()

        val jsonResponse = positionController.savePositions(principal, request, response, createPositions.toTypedArray())

        verify(routeRepository, Times(1)).findById(anyKotlin(Long::class.java))
        val positionCaptor = argumentCaptor<List<Position>>()
        verify(positionRepository, Times(1)).saveAll(positionCaptor.capture())

        assertEquals(3, positionCaptor.firstValue.size)
        positionEquals(positions[0], positionCaptor.firstValue[0])
        positionEquals(positions[1].copy(route = null), positionCaptor.firstValue[1])
        positionEquals(positions[2], positionCaptor.firstValue[2])

        val routeCaptor = argumentCaptor<Collection<Route>>()
        verify(routeRepository).saveAll(routeCaptor.capture())
        assertEquals(1, routeCaptor.firstValue.size)
        assertEquals(route.id, routeCaptor.firstValue.first().id)
        assertEquals(-1f, routeCaptor.firstValue.first().length)
        assertEquals(-1f, routeCaptor.firstValue.first().avgSpeed)
        assertEquals(-1, routeCaptor.firstValue.first().secondsOfTravel)

        assertFalse(file.exists())

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_CREATED, response.status)
    }


    private fun positionEquals(expected: Position, actual: Position) {
        assertEquals(expected.altitude, actual.altitude)
        assertEquals(expected.longitude, actual.longitude)
        assertEquals(expected.latitude, actual.latitude)
        assertEquals(expected.accuracy, actual.accuracy)
        assertEquals(expected.distance, actual.distance)
        assertEquals(expected.speed, actual.speed)
        assertEquals(expected.route?.id, actual.route?.id)
        assertEquals(expected.time, actual.time)
    }

    @Test
    fun `save position integrity violation`() {
        val principal = BasicUserPrincipal("Emanuel")
        createPositions[1] = createPositions[1].copy(routeId = null)

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)
        doThrow(DataIntegrityViolationException::class.java).`when`(positionRepository).saveAll(ArgumentMatchers.any())

        val file = File("$uploadFileFolder/route-${route.id}.png")
        file.createNewFile()

        val jsonResponse = positionController.savePositions(principal, request, response, createPositions.toTypedArray())

        verify(routeRepository, Times(1)).findById(anyKotlin(Long::class.java))

        assertTrue(file.exists())
        file.delete()

        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `get positions success`() {
        val car = Car(123, "John Doe", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val route = Route(id = 6782, time = ZonedDateTime.now(), car = car)
        val position1 = Position(1, route, 9f, 2f, 2f, ZonedDateTime.now(), 9f, 2f, 2f)
        val position2 = Position(2, route, 2f, 9f, 8f, ZonedDateTime.now(), 4f, 1f, 7f)
        val positions = listOf(position1, position2)
        val principal = BasicUserPrincipal("John Doe")

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)
        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(positions)).`when`(positionRepository).findAllByRouteId(com.nhaarman.mockitokotlin2.eq(route.id), pageableCaptor.capture())

        val jsonResponse = positionController.getPositions(principal, request, response, route.id, 2, 3)

        assertEquals(2, pageableCaptor.firstValue.pageNumber)
        assertEquals(3, pageableCaptor.firstValue.pageSize)
        assertEquals(Position.gson.toJson(positions), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get positions invalid route id`() {

        val principal = BasicUserPrincipal("John Doe")
        val routeOptional: Optional<Route> = Optional.empty()

        doReturn(routeOptional).`when`(routeRepository).findById(72)

        val jsonResponse = positionController.getPositions(principal, request, response, 123, 1, 3)

        verify(positionRepository, Times(0)).findAllByRouteId(com.nhaarman.mockitokotlin2.any(), com.nhaarman.mockitokotlin2.any())
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `get positions null principal`() {
        val car = Car(123, "John Doe", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val route = Route(id = 6782, time = ZonedDateTime.now(), car = car)
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)


        doReturn(null).`when`(principal).name
        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val jsonResponse = positionController.getPositions(principal, request, response, route.id, 1, 3)

        verify(positionRepository, Times(0)).findAllByRouteId(com.nhaarman.mockitokotlin2.any(), com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get positions not owner of route`() {
        val car = Car(123, "John Doe", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val route = Route(id = 6782, time = ZonedDateTime.now(), car = car)
        val principal = BasicUserPrincipal("Michal David")

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val jsonResponse = positionController.getPositions(principal, request, response, route.id, 1, 3)

        verify(positionRepository, Times(0)).findAllByRouteId(com.nhaarman.mockitokotlin2.any(), com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get events invalid limit -5`() {
        val car = Car(123, "John Doe", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val route = Route(id = 6782, time = ZonedDateTime.now(), car = car)
        val position1 = Position(1, route, 9f, 2f, 2f, ZonedDateTime.now(), 9f, 2f, 2f)
        val position2 = Position(2, route, 2f, 9f, 8f, ZonedDateTime.now(), 4f, 1f, 7f)
        val positions = listOf(position1, position2)
        val principal = BasicUserPrincipal("John Doe")

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)
        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(positions)).`when`(positionRepository).findAllByRouteId(com.nhaarman.mockitokotlin2.eq(route.id), pageableCaptor.capture())

        val jsonResponse = positionController.getPositions(principal, request, response, route.id, 2, -5)

        assertEquals(2, pageableCaptor.firstValue.pageNumber)
        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(Position.gson.toJson(positions), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get events invalid limit 0`() {
        val car = Car(123, "John Doe", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val route = Route(id = 6782, time = ZonedDateTime.now(), car = car)
        val position1 = Position(1, route, 9f, 2f, 2f, ZonedDateTime.now(), 9f, 2f, 2f)
        val position2 = Position(2, route, 2f, 9f, 8f, ZonedDateTime.now(), 4f, 1f, 7f)
        val positions = listOf(position1, position2)
        val principal = BasicUserPrincipal("John Doe")

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)
        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(positions)).`when`(positionRepository).findAllByRouteId(com.nhaarman.mockitokotlin2.eq(route.id), pageableCaptor.capture())

        val jsonResponse = positionController.getPositions(principal, request, response, route.id, 2, 0)

        assertEquals(2, pageableCaptor.firstValue.pageNumber)
        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(Position.gson.toJson(positions), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }
}