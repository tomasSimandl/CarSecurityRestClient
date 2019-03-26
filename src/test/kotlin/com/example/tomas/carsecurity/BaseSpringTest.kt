package com.example.tomas.carsecurity

import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class BaseSpringTest {

    fun addParams(url: String, params: Map<String, String>): String {
        var newUrl = url
        var appendChar = '?'

        for(value in params){
            newUrl = newUrl.plus("$appendChar${value.key}=${value.value}")
            appendChar = '&'
        }
        return newUrl
    }
}