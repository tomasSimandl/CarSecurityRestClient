package com.example.tomas.carsecurity.repository

import com.example.tomas.carsecurity.model.Position
import org.springframework.data.repository.PagingAndSortingRepository

interface PositionRepository : PagingAndSortingRepository<Position, Long>