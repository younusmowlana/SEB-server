/*
 * Copyright (c) 2020 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import ch.ethz.seb.sebserver.gbl.profile.GuiProfile;

@Component
@GuiProfile
public class GuiServiceInfo {

    private final String externalScheme;
    private final String internalServer;
    private final String externalServer;
    private final String internalPort;
    private final String externalPort;
    private final String entryPoint;
    private final String contextPath;
    private final UriComponentsBuilder internalServerURIBuilder;
    private final UriComponentsBuilder externalServerURIBuilder;
    private final boolean distributedSetup;

    public GuiServiceInfo(
            @Value("${server.address}") final String internalServer,
            @Value("${server.port}") final String internalPort,
            @Value("${sebserver.gui.http.external.scheme}") final String externalScheme,
            @Value("${sebserver.gui.http.external.servername}") final String externalServer,
            @Value("${sebserver.gui.http.external.port}") final String externalPort,
            @Value("${sebserver.gui.entrypoint:/gui}") final String entryPoint,
            @Value("${server.servlet.context-path:/}") final String contextPath,
            @Value("${sebserver.webservice.distributed:false}") final boolean distributedSetup) {

        if (StringUtils.isBlank(externalScheme)) {
            throw new RuntimeException("Missing mandatory inital parameter sebserver.gui.http.external.servername");
        }

        if (StringUtils.isBlank(externalServer)) {
            throw new RuntimeException("Missing mandatory inital parameter sebserver.gui.http.external.servername");
        }

        this.externalScheme = externalScheme;
        this.internalServer = internalServer;
        this.externalServer = externalServer;
        this.internalPort = internalPort;
        this.externalPort = externalPort;
        this.entryPoint = entryPoint;
        this.contextPath = contextPath;
        this.internalServerURIBuilder = UriComponentsBuilder
                .fromHttpUrl("http://" + this.internalServer);
        if (StringUtils.isNotBlank(internalPort)) {
            this.internalServerURIBuilder.port(this.internalPort);
        }
        if (StringUtils.isNotBlank(contextPath) && !contextPath.equals("/")) {
            this.internalServerURIBuilder.path(contextPath);
        }
        this.externalServerURIBuilder = UriComponentsBuilder
                .fromHttpUrl(this.externalScheme + "://" + this.externalServer);
        if (StringUtils.isNotBlank(externalPort)) {
            this.externalServerURIBuilder.port(this.externalPort);
        }
        if (StringUtils.isNotBlank(contextPath) && !contextPath.equals("/")) {
            this.externalServerURIBuilder.path(contextPath);
        }

        this.distributedSetup = distributedSetup;
    }

    public String getExternalScheme() {
        return this.externalScheme;
    }

    public String getInternalServer() {
        return this.internalServer;
    }

    public String getExternalServer() {
        return this.externalServer;
    }

    public String getInternalPort() {
        return this.internalPort;
    }

    public String getExternalPort() {
        return this.externalPort;
    }

    public String getEntryPoint() {
        return this.entryPoint;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public UriComponentsBuilder getInternalServerURIBuilder() {
        return this.internalServerURIBuilder.cloneBuilder();
    }

    public UriComponentsBuilder getExternalServerURIBuilder() {
        return this.externalServerURIBuilder.cloneBuilder();
    }

    public boolean isDistributedSetup() {
        return this.distributedSetup;
    }

}
