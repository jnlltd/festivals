/*
 * Copyright 2013-2015 Alvaro Sanchez-Mariscal <alvaro.sanchezmariscal@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ie.festivals

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.oauth.client.BaseOAuthClient

/**
 * Deals with pac4j library to fetch a user profile from the selected OAuth provider, and stores it on the security context
 */
class OauthService {

    static transactional = false

    GrailsApplication grailsApplication
    LinkGenerator grailsLinkGenerator

    BaseOAuthClient getClient(String provider) {
        log.debug "Creating OAuth client for provider: ${provider}"
        def providerConfig = grailsApplication.config.oauth."${provider}"
        def ClientClass = providerConfig.client

        BaseOAuthClient client
        if (ClientClass?.toString()?.endsWith("CasOAuthWrapperClient")) {
            client = ClientClass.newInstance(providerConfig.key, providerConfig.secret, providerConfig.casOAuthUrl)
        } else {
            client = ClientClass.newInstance(providerConfig.key, providerConfig.secret)
        }

        String callbackUrl = grailsLinkGenerator.link controller: 'oauth', action: 'callback', params: [provider: provider], absolute: true
        log.debug "Callback URL is: ${callbackUrl}"
        client.callbackUrl = callbackUrl

        if (providerConfig.scope) {
            client.scope = providerConfig.scope
        }

        if (providerConfig.fields) {
            client.fields = providerConfig.fields
        }

        return client
    }

    CommonProfile getUserProfile(String provider, WebContext context) {
        BaseOAuthClient client = getClient(provider)
        Credentials credentials = client.getCredentials context

        log.debug "Querying provider to fetch User ID"
        client.getUserProfile credentials, null
    }
}
