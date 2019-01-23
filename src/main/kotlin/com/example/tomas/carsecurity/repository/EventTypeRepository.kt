package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.EventType
import org.springframework.data.repository.CrudRepository

interface EventTypeRepository : CrudRepository<EventType, Long>