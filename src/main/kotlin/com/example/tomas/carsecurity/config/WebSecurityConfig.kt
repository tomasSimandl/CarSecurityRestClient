package com.example.tomas.carsecurity.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
import org.springframework.security.oauth2.provider.token.RemoteTokenServices
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices

/**
 * Class is used for configuration of this resource server.
 */
@Configuration
@EnableWebSecurity
class WebSecurityConfig(

        /** Url of authorization server */
        @Value("\${oauth2.check.token.url}")
        private val checkTokenUrl: String,

        /** Id of this client (resource server) */
        @Value("\${oauth2.client.client-id}")
        private val clientId: String,

        /** Secret for this client (resource server) */
        @Value("\${oauth2.client.client-secret}")
        private val clientSecret: String
) : WebSecurityConfigurerAdapter() {

    /**
     * Authorization setting of resource access. All request must be authenticated.
     * @param http HttpSecurity object on which is set authentication privileges.
     */
    override fun configure(http: HttpSecurity) {
        http
                .authorizeRequests()
                .anyRequest()
                .authenticated()
    }

    /**
     * Configuration of token service for authenticated over authorization server.
     */
    @Bean
    fun tokenServices(): ResourceServerTokenServices {
        val tokenServices = RemoteTokenServices()
        tokenServices.setClientId(clientId)
        tokenServices.setClientSecret(clientSecret)
        tokenServices.setCheckTokenEndpointUrl(checkTokenUrl)
        return tokenServices
    }

    /**
     * Configuration of authentication manager. As a token service is used [tokenServices].
     */
    override fun authenticationManagerBean(): AuthenticationManager {
        val authenticationManager = OAuth2AuthenticationManager()
        authenticationManager.setTokenServices(tokenServices())
        return authenticationManager
    }
}