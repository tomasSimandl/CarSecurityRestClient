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
}