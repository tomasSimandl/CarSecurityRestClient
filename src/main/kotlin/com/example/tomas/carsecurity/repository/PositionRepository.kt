package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Position
import org.springframework.data.repository.CrudRepository

interface PositionRepository : CrudRepository<Position, Long>