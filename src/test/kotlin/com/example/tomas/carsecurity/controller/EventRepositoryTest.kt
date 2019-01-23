package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.repository.EventRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable


@SpringBootTest
class EventRepositoryTest {

    @Autowired
    lateinit var eventRepository: EventRepository

    @Test
    fun `find all events by car id unpaged`() {

        val found = eventRepository.findAllByCarId(1, Pageable.unpaged())
        assertEquals(4, found.count(), "Number of found elements is incorrect")

        val expectedIds = intArrayOf(1, 2, 4, 6)
        for(event in found.get()) {
            assertTrue(expectedIds.contains(event.id.toInt()))
        }
    }


    @Test
    fun `find all events by car id paged`() {

        var found = eventRepository.findAllByCarId(2, PageRequest.of(0, 3))
        assertEquals(3, found.count(), "Number of found elements is incorrect")

        val expectedIds = intArrayOf(3, 5, 7)
        for(event in found.get()) {
            assertTrue(expectedIds.contains(event.id.toInt()))
        }

        found = eventRepository.findAllByCarId(2, PageRequest.of(1, 3))
        assertEquals(1, found.count(), "Number of found elements is incorrect")

        assertEquals(8, found.first().id.toInt())
    }


    @Test
    fun `find all events by invalid car id unpaged`() {

        val found = eventRepository.findAllByCarId(3, Pageable.unpaged())
        assertEquals(0, found.count(), "Number of found elements is incorrect")
    }
}