package com.example.tomas.carsecurity.maps.service

import com.example.tomas.carsecurity.model.Position

interface MapService {

    fun getStaticMap(startPosition: Position, endPosition: Position): ByteArray
}