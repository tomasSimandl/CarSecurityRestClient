package com.example.tomas.carsecurity

import org.mockito.ArgumentCaptor
import org.mockito.Mockito

fun <T> anyKotlin(type: Class<T>): T = Mockito.any<T>(type)
