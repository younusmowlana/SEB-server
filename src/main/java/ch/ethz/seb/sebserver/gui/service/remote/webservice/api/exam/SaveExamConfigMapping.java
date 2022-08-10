/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.service.remote.webservice.api.exam;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import ch.ethz.seb.sebserver.gbl.api.API;
import ch.ethz.seb.sebserver.gbl.api.EntityType;
import ch.ethz.seb.sebserver.gbl.model.exam.ExamConfigurationMap;
import ch.ethz.seb.sebserver.gbl.profile.GuiProfile;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.RestCall;

@Lazy
@Component
@GuiProfile
public class SaveExamConfigMapping extends RestCall<ExamConfigurationMap> {

    public SaveExamConfigMapping() {
        super(new TypeKey<>(
                CallType.SAVE,
                EntityType.EXAM_CONFIGURATION_MAP,
                new TypeReference<ExamConfigurationMap>() {
                }),
                HttpMethod.PUT,
                MediaType.APPLICATION_JSON,
                API.EXAM_CONFIGURATION_MAP_ENDPOINT);
    }

}
