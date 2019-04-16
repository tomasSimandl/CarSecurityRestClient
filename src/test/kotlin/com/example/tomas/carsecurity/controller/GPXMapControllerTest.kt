package com.example.tomas.carsecurity.controller


import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.repository.RouteRepository
import org.apache.http.auth.BasicUserPrincipal
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.servlet.http.HttpServletResponse

class GPXMapControllerTest {

    @Mock
    private lateinit var routeRepository: RouteRepository

    private lateinit var gpxMapController: GPXMapController

    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()

        gpxMapController = GPXMapController(routeRepository)
    }

    @Test
    fun `get GPX map success`() {
        val expectedGPX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><gpx version=\"1.1\" creator=\"JPX - https://github.com/jenetics/jpx\" xmlns=\"http://www.topografix.com/GPX/1/1\"><trk><trkseg><trkpt lat=\"1.0\" lon=\"2.0\"><ele>3.0</ele><time>1991-01-01T01:01:01.000000001Z</time></trkpt><trkpt lat=\"4.0\" lon=\"5.0\"><ele>6.0</ele><time>1991-01-01T01:01:01.000000002Z</time></trkpt></trkseg></trk></gpx>"
        val principal = BasicUserPrincipal("mapak")
        val car = Car(id = 82, username = "mapak")
        val position1 = Position(route = null, latitude = 1f, longitude = 2f, altitude = 3f, time = ZonedDateTime.of(1991, 1, 1, 1, 1, 1, 1, ZoneId.of("UTC")))
        val position2 = Position(route = null, latitude = 4f, longitude = 5f, altitude = 6f, time = ZonedDateTime.of(1991, 1, 1, 1, 1, 1, 2, ZoneId.of("UTC")))
        val route = Route(id = 823, time = ZonedDateTime.now(), car = car, positions = listOf(position1, position2))

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val gpxResponse = gpxMapController.getGPXMap(principal, request, response, route.id)

        assertEquals(expectedGPX, gpxResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }


    @Test
    fun `get GPX map invalid route`() {
        val principal = BasicUserPrincipal("Emanuel")
        val routeOptional: Optional<Route> = Optional.empty()

        doReturn(routeOptional).`when`(routeRepository).findById(com.nhaarman.mockitokotlin2.any())

        val gpxResponse = gpxMapController.getGPXMap(principal, request, response, 324)

        assertEquals("", gpxResponse)
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `get GPX map null principal`() {
        val car = Car(id = 82, username = "mapak")
        val position1 = Position(route = null, latitude = 1f, longitude = 2f, altitude = 3f, time = ZonedDateTime.now())
        val position2 = Position(route = null, latitude = 4f, longitude = 5f, altitude = 6f, time = ZonedDateTime.now())
        val route = Route(id = 823, time = ZonedDateTime.now(), car = car, positions = listOf(position1, position2))
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(null).`when`(principal).name
        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val gpxResponse = gpxMapController.getGPXMap(principal, request, response, route.id)

        assertEquals("", gpxResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get GPX map not owner`() {
        val principal = BasicUserPrincipal("mapak")
        val car = Car(id = 82, username = "mapkova")
        val position1 = Position(route = null, latitude = 1f, longitude = 2f, altitude = 3f, time = ZonedDateTime.now())
        val position2 = Position(route = null, latitude = 4f, longitude = 5f, altitude = 6f, time = ZonedDateTime.now())
        val route = Route(id = 823, time = ZonedDateTime.now(), car = car, positions = listOf(position1, position2))

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val gpxResponse = gpxMapController.getGPXMap(principal, request, response, route.id)

        assertEquals("", gpxResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }
}