package com.example.tomas.carsecurity.service


import com.example.tomas.carsecurity.model.Position
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.ZonedDateTime

class MapServiceImplTest {

    @Mock
    private lateinit var restTemplate: RestTemplate

    private lateinit var mapService: MapServiceImpl

    private val bingKey = "mySuperSecretBingKey"

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)

        mapService = MapServiceImpl(bingKey, restTemplate)
    }

    @Test
    fun mapTest() {

        val startPosition = Position(route = null, time = ZonedDateTime.now(), latitude = 111f, longitude = 222f)
        val endPosition = Position(route = null, time = ZonedDateTime.now(), latitude = 333f, longitude = 444f)
        val byteArray = byteArrayOf(1, 2, 3)

        val urlCaptor = ArgumentCaptor.forClass(String::class.java)
        val entityCaptor = ArgumentCaptor.forClass(HttpEntity::class.java)

        val entity = ResponseEntity(byteArray, HttpStatus.OK)
        doReturn(entity).`when`(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), ArgumentMatchers.eq(ByteArray::class.java))


        val map = mapService.getStaticMap(startPosition, endPosition)


        assertTrue(urlCaptor.value.contains("key=$bingKey"))
        assertTrue(urlCaptor.value.contains("fmt=png"))

        assertTrue(entityCaptor.value.body is String)
        assertTrue((entityCaptor.value.body as String).startsWith("pp=${startPosition.latitude},${startPosition.longitude};"))
        assertTrue((entityCaptor.value.body as String).contains("pp=${endPosition.latitude},${endPosition.longitude};"))

        assertArrayEquals(byteArray, map)
    }
}