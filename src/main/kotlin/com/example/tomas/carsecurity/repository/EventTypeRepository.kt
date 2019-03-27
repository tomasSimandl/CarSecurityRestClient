package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.EventType
import org.springframework.data.repository.CrudRepository

/**
 * This repository is used for access event_type table in database.
 */
interface EventTypeRepository : CrudRepository<EventType, Long>