/*
 * Copyright (c) 2021 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import ch.ethz.seb.sebserver.ClientHttpRequestFactoryService;
import ch.ethz.seb.sebserver.gbl.async.AsyncService;
import ch.ethz.seb.sebserver.gbl.client.ClientCredentialService;
import ch.ethz.seb.sebserver.gbl.model.institution.LmsSetup.LmsType;
import ch.ethz.seb.sebserver.gbl.profile.WebServiceProfile;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.APITemplateDataSupplier;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.LmsAPITemplate;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.LmsAPITemplateFactory;

import ch.ethz.seb.sebserver.gbl.api.JSONMapper;



@Lazy
@Service
@WebServiceProfile
/** Factory for OATLmsAPITemplate. Since a LmsAPITemplate of a specific LMS type
 * is whether a singleton component nor a simple prototype but one (singleton) instance
 * can exist per defined LMSSetup, we need a specialized factory to build such
 * a LmsAPITemplate for a specific LMSSetup.
 * </p>
 * Add needed dependencies as final fields and let them inject within the constructor
 * as usual. Just add the additionally needed dependencies used to build a OATLmsAPITemplate. */
public class OATLmsAPITemplateFactory implements LmsAPITemplateFactory {

    private final ClientHttpRequestFactoryService clientHttpRequestFactoryService;
    private final ClientCredentialService clientCredentialService;
    private final AsyncService asyncService;
    private final Environment environment;
    private final CacheManager cacheManager;
    public final JSONMapper jsonMapper;

    public OATLmsAPITemplateFactory(
            final ClientHttpRequestFactoryService clientHttpRequestFactoryService,
            final ClientCredentialService clientCredentialService,
            final AsyncService asyncService,
            final Environment environment,
            final CacheManager cacheManager,
            final JSONMapper jsonMapper) {

        this.clientHttpRequestFactoryService = clientHttpRequestFactoryService;
        this.clientCredentialService = clientCredentialService;
        this.asyncService = asyncService;
        this.environment = environment;
        this.cacheManager = cacheManager;
        this.jsonMapper = jsonMapper;

    }

    @Override
    public LmsType lmsType() {
        return LmsType.OAT;
    }

    @Override
    public Result<LmsAPITemplate> create(final APITemplateDataSupplier apiTemplateDataSupplier) {
        return Result.tryCatch(() -> {
            return new OATLmsAPITemplate(
                    this.clientHttpRequestFactoryService,
                    this.clientCredentialService,
                    apiTemplateDataSupplier,
                    this.asyncService,
                    this.environment,
                    this.cacheManager,
                    this.jsonMapper);
        });
    }

}
