package com.example.tomas.carsecurity.controller


import com.example.tomas.carsecurity.anyKotlin
import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.repository.CarRepository
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.grpc.internal.JsonParser
import org.apache.http.auth.BasicUserPrincipal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.Times
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.util.*
import javax.servlet.http.HttpServletResponse

class FirebaseControllerTest {

    @Mock
    private lateinit var carRepository: CarRepository

    private lateinit var firebaseController: FirebaseController

    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()

        firebaseController = FirebaseController(carRepository)
    }

    @Test
    fun `save token success`() {
        val token = "hkjdlfhgufioijkmvfjkgfehija;sz"
        val car = Car(id = 48, username = "Emanuel")
        val principal = BasicUserPrincipal("Emanuel")

        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)

        val jsonResponse = firebaseController.saveToken(principal, request, response, car.id, token)

        val carCaptor = argumentCaptor<Car>()
        verify(carRepository, Times(1)).save(carCaptor.capture())

        assertEquals(car.id, carCaptor.firstValue.id)
        assertEquals(car.username, carCaptor.firstValue.username)
        assertEquals(car.note, carCaptor.firstValue.note)
        assertEquals(token, carCaptor.firstValue.firebaseToken)

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_CREATED, response.status)
    }

    @Test
    fun `save token blank token`() {
        val token = "       "
        val principal = BasicUserPrincipal("Emanuel")

        val jsonResponse = firebaseController.saveToken(principal, request, response, 2, token)

        verify(carRepository, Times(0)).save(anyKotlin(Car::class.java))
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `save token invalid car`() {
        val token = "hkjdlfhgufioijkmvfjkgfehija;sz"
        val principal = BasicUserPrincipal("Emanuel")
        val carOptional: Optional<Car> = Optional.empty()

        doReturn(carOptional).`when`(carRepository).findById(com.nhaarman.mockitokotlin2.any())

        val jsonResponse = firebaseController.saveToken(principal, request, response, 2, token)

        verify(carRepository, Times(0)).save(anyKotlin(Car::class.java))
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `save token null principal`() {
        val token = "hkjdlfhgufioijkmvfjkgfehija;sz"
        val car = Car(id = 48, username = "Emanuel")
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(null).`when`(principal).name
        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)

        val jsonResponse = firebaseController.saveToken(principal, request, response, car.id, token)

        verify(carRepository, Times(0)).save(anyKotlin(Car::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `save token not owner`() {
        val token = "hkjdlfhgufioijkmvfjkgfehija;sz"
        val car = Car(id = 48, username = "Emanuel")
        val principal = BasicUserPrincipal("Eleanor")

        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)

        val jsonResponse = firebaseController.saveToken(principal, request, response, car.id, token)

        verify(carRepository, Times(0)).save(anyKotlin(Car::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }
}