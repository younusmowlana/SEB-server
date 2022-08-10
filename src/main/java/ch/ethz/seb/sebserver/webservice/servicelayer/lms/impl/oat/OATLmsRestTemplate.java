/*
 * Copyright (c) 2021 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ch.ethz.seb.sebserver.gbl.api.JSONMapper;

import ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat.OATLmsData.AssessmentData;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat.OATLmsData.accessToken;


public class OATLmsRestTemplate extends RestTemplate {

    private static final Logger log = LoggerFactory.getLogger(OATLmsRestTemplate.class);

    private String token;
    private ClientCredentialsResourceDetails details;
    final JSONMapper jsonMapper;

    public OATLmsRestTemplate(final JSONMapper jsonMapper, final ClientCredentialsResourceDetails details) {
        super();
        this.details = details;
        this.jsonMapper = jsonMapper;

        this.getInterceptors().add(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
                    final ClientHttpRequestExecution execution) throws IOException {

                try {
                    if (OATLmsRestTemplate.this.token == null) {
                        authenticate();
                    }
                    else if (OATLmsRestTemplate.this.token.equals("authenticating")) {
                        return execution.execute(request, body);
                    }
                    request.getHeaders().set("accept", "application/json");
                    String token = "Bearer "+ OATLmsRestTemplate.this.token;
                    request.getHeaders().set("Authorization", token);
                    ClientHttpResponse response = execution.execute(request, body);
                    log.debug("OAT [regular API call] {} Headers: {} body: {}", response.getStatusCode(), response.getHeaders(), response.getBody() );
                    if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        authenticate();
                        token = "Bearer "+ OATLmsRestTemplate.this.token;
                        request.getHeaders().set("Authorization", token);
                        response = execution.execute(request, body);
                        log.debug("OAT [retry API call] {} Headers: {}", response.getStatusCode(), response.getHeaders());
                    }
                    return response;
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    return null;
                }

                
            }
        });
    }   

    private void authenticate() {
        // Authenticate with OAT and store the received OAT-TOKEN
        this.token = "authenticating";
        final String authUrl = this.details.getAccessTokenUri();
        final Map<String, String> credentials = new HashMap<>();
        credentials.put("user_email", this.details.getClientId());
        credentials.put("password", this.details.getClientSecret());
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", "application/json");
        final HttpEntity<Map<String,String>> requestEntity = new HttpEntity<>(credentials, httpHeaders);
        try {
            final ResponseEntity<String> response = this.postForEntity(authUrl, requestEntity, String.class);
            final HttpHeaders responseHeaders = response.getHeaders();
            log.debug("OAT [authenticate] {} Headers: {}", response.getStatusCode(), responseHeaders);
            final accessToken result = this.jsonMapper.readValue(response.getBody(), accessToken.class);
            this.token = result.accessToken;
            log.debug("Token --- {}", result.accessToken);
        } catch (JsonProcessingException e) {
            this.token = null;
        } catch (Exception e) {
            this.token = null;
            throw e;
        }
    }

    public String getToken () {
        return this.token;
    }

}
