package com.example.tomas.carsecurity.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer

/**
 * Class is used for configuration of this resource server.
 */
@Configuration
@EnableResourceServer
class ResourceServerConfig(

        /** Identification of resources on this server. Value is taken from application.properties. */
        @Value("\${oauth2.resource-id}")
        private val resourceId: String

) : ResourceServerConfigurerAdapter() {

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
     * Configuration which sets OAuth resource id to this resource server.
     * Value is taken from application.properties under key oauth2.resource-id.
     *
     * @param resources Configurer of resource server.
     */
    override fun configure(resources: ResourceServerSecurityConfigurer) {
        resources
                .resourceId(resourceId)
    }
}