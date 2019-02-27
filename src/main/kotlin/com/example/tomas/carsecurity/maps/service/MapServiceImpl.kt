package com.example.tomas.carsecurity.maps.service

import com.example.tomas.carsecurity.model.Position
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MapServiceImpl(
        @Value("\${bing.map.key}")
        private val bingKey: String,

        private val restTemplate: RestTemplate
) : MapService {

    override fun getStaticMap(startPosition: Position, endPosition: Position): ByteArray {

        val url = "https://dev.virtualearth.net/REST/V1/Imagery/Map/Road?" +
                "mapSize=600,400&" +
                "fmt=png&" +
                "key=$bingKey"

        val body = "pp=${startPosition.latitude},${startPosition.longitude};16;S&" +
                "pp=${endPosition.latitude},${endPosition.longitude};22;E"

        val header = HttpHeaders()
        header["Content-Type"] = "text/plain;charset=utf-8"
        header["Content-Length"] = body.length.toString()

        val entity = HttpEntity(body, header)

        val resultEntity = restTemplate.postForEntity(url, entity, ByteArray::class.java)
        return resultEntity.body!!

    }
}