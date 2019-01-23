package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.BaseSpringTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

class RouteRepositoryTest : BaseSpringTest() {

    @Autowired
    lateinit var routeRepository: RouteRepository

    @Test
    fun `find all routes by car id unpaged`() {

        val found = routeRepository.findAllByCarId(1, Pageable.unpaged())
        assertEquals(3, found.count(), "Number of found elements is incorrect")

        val expectedIds = intArrayOf(1, 2, 4)
        for (route in found.get()) {
            assertTrue(expectedIds.contains(route.id.toInt()))
        }
    }


    @Test
    fun `find all routes by car id paged`() {

        var found = routeRepository.findAllByCarId(1, PageRequest.of(0, 2))
        assertEquals(2, found.count(), "Number of found elements is incorrect")

        val expectedIds = intArrayOf(1, 2)
        for (route in found.get()) {
            assertTrue(expectedIds.contains(route.id.toInt()))
        }

        found = routeRepository.findAllByCarId(1, PageRequest.of(1, 2))
        assertEquals(1, found.count(), "Number of found elements is incorrect")

        assertEquals(4, found.first().id.toInt())
    }


    @Test
    fun `find all routes by invalid car id unpaged`() {

        val found = routeRepository.findAllByCarId(3, Pageable.unpaged())
        assertEquals(0, found.count(), "Number of found elements is incorrect")
    }
}