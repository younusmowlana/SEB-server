/*
 * Copyright (c) 2020 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.session.impl.proctoring;

import java.util.Collection;
import java.util.EnumMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import ch.ethz.seb.sebserver.gbl.model.exam.ProctoringServiceSettings.ProctoringServerType;
import ch.ethz.seb.sebserver.gbl.profile.WebServiceProfile;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.webservice.servicelayer.session.ExamProctoringService;

@Lazy
@Service
@WebServiceProfile
public class ExamProctoringServiceFactory {

    private final EnumMap<ProctoringServerType, ExamProctoringService> services;

    public ExamProctoringServiceFactory(final Collection<ExamProctoringService> proctorServices) {
        this.services = new EnumMap<>(proctorServices
                .stream()
                .collect(Collectors.<ExamProctoringService, ProctoringServerType, ExamProctoringService> toMap(
                        s -> s.getType(),
                        Function.identity())));
    }

    public Result<ExamProctoringService> getExamProctoringService(final ProctoringServerType type) {
        if (this.services.containsKey(type)) {
            return Result.of(this.services.get(type));
        }

        return Result.ofRuntimeError("No ExamProctoringService for type: " + type + " registered");
    }

}
