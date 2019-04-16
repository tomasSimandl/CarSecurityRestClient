package com.example.tomas.carsecurity.service


import org.apache.http.auth.BasicUserPrincipal
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.Times
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

class UserServiceImplTest {

    @Mock
    private lateinit var restTemplate: RestTemplate

    @Mock
    private lateinit var principal: OAuth2Authentication

    @Mock
    private lateinit var tokenDetails: OAuth2AuthenticationDetails

    private lateinit var userService: UserServiceImpl

    private val authorizationServerUrl = "https://authorization.server.url.com:1234"
    private val token = "ultraSecretToken"

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)

        userService = UserServiceImpl(authorizationServerUrl, restTemplate)
    }

    @Test
    fun getUserEmailTest() {

        val email = "john@doe.com"
        val name = "John doe"

        doReturn(token).`when`(tokenDetails).tokenValue
        doReturn(tokenDetails).`when`(principal).details
        doReturn(name).`when`(principal).name

        val urlCaptor = ArgumentCaptor.forClass(String::class.java)
        val entityCaptor = ArgumentCaptor.forClass(HttpEntity::class.java)

        doReturn(ResponseEntity("{'email':'$email'}", HttpStatus.OK)).`when`(restTemplate).exchange(
                urlCaptor.capture(),
                ArgumentMatchers.eq(HttpMethod.GET),
                entityCaptor.capture(),
                ArgumentMatchers.eq(String::class.java))

        val actualEmail = userService.getUserEmail(principal)

        assertEquals(email, actualEmail)
        assertEquals("$authorizationServerUrl/user?username=$name", urlCaptor.value)
        assertEquals("Bearer $token", entityCaptor.value.headers["Authorization"]?.first())
    }

    @Test
    fun getUserEmailExceptionTest() {

        val name = "John doe"

        doReturn(token).`when`(tokenDetails).tokenValue
        doReturn(tokenDetails).`when`(principal).details
        doReturn(name).`when`(principal).name

        doThrow(RestClientException::class.java).`when`(restTemplate).exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.any(Class::class.java)
        )

        val actualEmail = userService.getUserEmail(principal)

        assertEquals("", actualEmail)
    }

    @Test
    fun getUserEmailWrongPrincipal() {

        val principal = BasicUserPrincipal("John Snow")
        val actualEmail = userService.getUserEmail(principal)

        verify(restTemplate, Times(0)).exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.any(Class::class.java))

        assertEquals("", actualEmail)
    }

    @Test
    fun getUserEmailWrongDetails() {

        val details = TestingAuthenticationToken("John", "Snow")
        doReturn(details).`when`(principal).details

        val actualEmail = userService.getUserEmail(principal)

        verify(restTemplate, Times(0)).exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.any(Class::class.java))

        assertEquals("", actualEmail)
    }
}