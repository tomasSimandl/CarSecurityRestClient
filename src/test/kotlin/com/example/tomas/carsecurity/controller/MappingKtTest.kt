package com.example.tomas.carsecurity.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MappingKtTest {

    @Test
    fun `create json single`() {
        val result = createJsonSingle("error", "text of error")
        assertEquals("{\"error\":\"text of error\"}", result)
    }

    @Test
    fun `create json single with empty strings`() {
        val result = createJsonSingle("", "")
        assertEquals("{\"\":\"\"}", result)
    }

    @Test
    fun `add params empty params`(){

        val map = HashMap<String, String>()

        val result = addParams("url", map)
        assertEquals("url", result)
    }

    @Test
    fun `add params one param`(){

        val map = HashMap<String, String>()
        map["a"] = "10"

        val result = addParams("url", map)
        assertEquals("url?a=10", result)
    }

    @Test
    fun `add params two params`(){

        val map = HashMap<String, String>()
        map["a"] = "10"
        map["b"] = "20"

        val result = addParams("url", map)
        assertEquals("url?a=10&b=20", result)
    }
}