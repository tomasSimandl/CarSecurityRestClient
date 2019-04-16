package com.example.tomas.carsecurity.controller


import com.example.tomas.carsecurity.anyKotlin
import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.repository.PositionRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import com.example.tomas.carsecurity.service.MapService
import org.apache.commons.io.IOUtils
import org.apache.http.auth.BasicUserPrincipal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.Times
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.io.File
import java.time.ZonedDateTime
import java.util.*
import javax.servlet.http.HttpServletResponse

class BingMapControllerTest {

    @Mock
    private lateinit var routeRepository: RouteRepository
    @Mock
    private lateinit var positionRepository: PositionRepository
    @Mock
    private lateinit var mapService: MapService

    private val uploadFileFolder = this.javaClass.classLoader.getResource("bing_map").path
    private lateinit var bingMapController: BingMapController


    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    private val username = "Karel Got"

    private val image3 = IOUtils.toByteArray(this.javaClass.classLoader.getResourceAsStream("bing_map/route-3.png"))
    private val image6 = IOUtils.toByteArray(this.javaClass.classLoader.getResourceAsStream("bing_map/route-6.png"))

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()

        bingMapController = BingMapController(routeRepository, positionRepository, mapService, uploadFileFolder)
    }

    @Test
    fun getStaticMapTestLocal() {

        val car = Car(username = username)
        val route = Route(id = 6, car = car, time = ZonedDateTime.now())
        val principal = BasicUserPrincipal(username)

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val map = bingMapController.getStaticMap(principal, request, response, route.id)

        assertArrayEquals(image6, map)
        verify(routeRepository).findById(route.id)
        verify(positionRepository, Times(0)).findFirstByRouteOrderByTimeAsc(anyKotlin(Route::class.java))
    }


    @Test
    fun getStaticMapTestDownload() {

        val car = Car(username = username)
        val route = Route(id = 22, car = car, time = ZonedDateTime.now())
        val principal = BasicUserPrincipal(username)

        val startPosition = Position(time = ZonedDateTime.now(), route = route)
        val endPosition = Position(time = ZonedDateTime.now(), route = route)

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)
        doReturn(Optional.of(startPosition)).`when`(positionRepository).findFirstByRouteOrderByTimeAsc(route)
        doReturn(Optional.of(endPosition)).`when`(positionRepository).findFirstByRouteOrderByTimeDesc(route)
        doReturn(image3).`when`(mapService).getStaticMap(startPosition, endPosition)

        val map = bingMapController.getStaticMap(principal, request, response, route.id)

        assertArrayEquals(image3, map)
        verify(routeRepository).findById(route.id)
        verify(positionRepository, Times(1)).findFirstByRouteOrderByTimeAsc(route)
        verify(positionRepository, Times(1)).findFirstByRouteOrderByTimeDesc(route)
        verify(mapService, Times(1)).getStaticMap(startPosition, endPosition)


        val newMap = File(this.javaClass.classLoader.getResource("bing_map/route-22.png").toURI())

        assertTrue(newMap.exists())
        newMap.delete()
    }


    @Test
    fun `get static map wrong route`() {

        val routeOptional: Optional<Route> = Optional.empty()
        val principal = BasicUserPrincipal(username)

        doReturn(routeOptional).`when`(routeRepository).findById(6)

        val map = bingMapController.getStaticMap(principal, request, response, 6)

        assertTrue(map.isEmpty())
        verify(routeRepository, Times(1)).findById(6)
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }


    @Test
    fun `get static map wrong owner`() {

        val car = Car(username = "Helena Vondrackova")
        val route = Route(id = 6, car = car, time = ZonedDateTime.now())
        val principal = BasicUserPrincipal(username)

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val map = bingMapController.getStaticMap(principal, request, response, 6)

        assertTrue(map.isEmpty())
        verify(routeRepository, Times(1)).findById(6)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }
}