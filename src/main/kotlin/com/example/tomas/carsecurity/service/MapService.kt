package com.example.tomas.carsecurity.service

import com.example.tomas.carsecurity.model.Position

/**
 * Service used for getting static image of route.
 */
interface MapService {

    /**
     * Method returns static image of map where is mark [startPosition] and [endPosition].
     *
     * @param startPosition which will be marked on map.
     * @param endPosition which will be marked on map.
     * @return ByteArray which contain image of map with two marked points.
     */
    fun getStaticMap(startPosition: Position, endPosition: Position): ByteArray
}