/*
 * Copyright (c) 2020 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.service.remote.webservice.api.session;

import java.util.Collection;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.ethz.seb.sebserver.gbl.api.API;
import ch.ethz.seb.sebserver.gbl.api.EntityType;
import ch.ethz.seb.sebserver.gbl.model.session.ClientConnection;
import ch.ethz.seb.sebserver.gbl.profile.GuiProfile;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.RestCall;

@Lazy
@Component
@GuiProfile
public class GetCollectingRoomConnections extends RestCall<Collection<ClientConnection>> {

    public GetCollectingRoomConnections() {
        super(new TypeKey<>(
                CallType.GET_LIST,
                EntityType.CLIENT_CONNECTION,
                new TypeReference<Collection<ClientConnection>>() {
                }),
                HttpMethod.GET,
                MediaType.APPLICATION_FORM_URLENCODED,
                API.EXAM_PROCTORING_ENDPOINT
                        + API.MODEL_ID_VAR_PATH_SEGMENT
                        + API.EXAM_PROCTORING_ROOM_CONNECTIONS_PATH_SEGMENT);
    }
}
