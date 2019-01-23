package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.BaseSpringTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate

class BaseControllerTest : BaseSpringTest() {

    private val url = "http://localhost"
    private val port = "8080"

    @Autowired
    lateinit var testTemplate: TestRestTemplate

    fun getUrl(path: String): String {
        return "$url:$port$path"
    }
}