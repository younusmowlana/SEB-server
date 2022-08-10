/*
 * Copyright (c) 2021 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ch.ethz.seb.sebserver.gbl.api.JSONMapper;

import ch.ethz.seb.sebserver.ClientHttpRequestFactoryService;
import ch.ethz.seb.sebserver.gbl.api.APIMessage;
import ch.ethz.seb.sebserver.gbl.async.AsyncService;
import ch.ethz.seb.sebserver.gbl.client.ClientCredentialService;
import ch.ethz.seb.sebserver.gbl.client.ClientCredentials;
import ch.ethz.seb.sebserver.gbl.client.ProxyData;
import ch.ethz.seb.sebserver.gbl.model.Domain.LMS_SETUP;
import ch.ethz.seb.sebserver.gbl.model.exam.Chapters;
import ch.ethz.seb.sebserver.gbl.model.exam.Exam;
import ch.ethz.seb.sebserver.gbl.model.exam.QuizData;
import ch.ethz.seb.sebserver.gbl.model.exam.SEBRestriction;
import ch.ethz.seb.sebserver.gbl.model.institution.LmsSetup;
import ch.ethz.seb.sebserver.gbl.model.institution.LmsSetup.LmsType;
import ch.ethz.seb.sebserver.gbl.model.institution.LmsSetupTestResult;
import ch.ethz.seb.sebserver.gbl.model.user.ExamineeAccountDetails;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.gbl.util.Utils;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.FilterMap;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.APITemplateDataSupplier;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.LmsAPIService;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.LmsAPITemplate;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.AbstractCachedCourseAccess;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat.OATLmsData.AssessmentData;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat.OATLmsData.RestrictionData;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat.OATLmsData.RestrictionDataPost;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat.OATLmsData.UserData;
import ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat.OATLmsData.assessmentArray;


public class OATLmsAPITemplate extends AbstractCachedCourseAccess implements LmsAPITemplate {

    private static final Logger log = LoggerFactory.getLogger(OATLmsAPITemplate.class);

    private final ClientHttpRequestFactoryService clientHttpRequestFactoryService;
    private final ClientCredentialService clientCredentialService;
    private final APITemplateDataSupplier apiTemplateDataSupplier;
    private final Long lmsSetupId;
    public final JSONMapper jsonMapper;


    private OATLmsRestTemplate cachedRestTemplate;

    protected OATLmsAPITemplate(
            final ClientHttpRequestFactoryService clientHttpRequestFactoryService,
            final ClientCredentialService clientCredentialService,
            final APITemplateDataSupplier apiTemplateDataSupplier,
            final AsyncService asyncService,
            final Environment environment,
            final CacheManager cacheManager,
            final JSONMapper jsonMapper) {

        super(asyncService, environment, cacheManager);
        this.jsonMapper = jsonMapper;
        this.clientHttpRequestFactoryService = clientHttpRequestFactoryService;
        this.clientCredentialService = clientCredentialService;
        this.apiTemplateDataSupplier = apiTemplateDataSupplier;
        this.lmsSetupId = apiTemplateDataSupplier.getLmsSetup().id;

    }

    @Override
    public LmsType getType() {
        return LmsType.OAT;
    }

    @Override
    public LmsSetup lmsSetup() {
        return this.apiTemplateDataSupplier.getLmsSetup();
    }

    @Override
    protected Long getLmsSetupId() {
        return this.lmsSetupId;
    }

    @Override
    public LmsSetupTestResult testCourseAccessAPI() {
        final LmsSetup lmsSetup = this.apiTemplateDataSupplier.getLmsSetup();
        final LmsSetupTestResult testLmsSetupSettings = testLmsSetupSettings();
        if (testLmsSetupSettings.hasAnyError()) {
            return testLmsSetupSettings;
        }
        try {
            this.getRestTemplate().get();
            this.testConnection(this.getRestTemplate().get(), "api/assessment/seb/testConnection/");
        } catch (final Exception e) {
            log.error("Failed to access OAT course API: ", e);
            return LmsSetupTestResult.ofQuizAccessAPIError(LmsType.OAT, e.getMessage());
        }
        return LmsSetupTestResult.ofOkay(LmsType.OAT);
    }

    @Override
    public LmsSetupTestResult testCourseRestrictionAPI() {
        return testCourseAccessAPI();
    }

    private LmsSetupTestResult testLmsSetupSettings() {

        final LmsSetup lmsSetup = this.apiTemplateDataSupplier.getLmsSetup();
        final ClientCredentials lmsClientCredentials = this.apiTemplateDataSupplier.getLmsClientCredentials();
        final List<APIMessage> missingAttrs = new ArrayList<>();

        // Check given LMS URL
        if (StringUtils.isBlank(lmsSetup.lmsApiUrl)) {
            missingAttrs.add(APIMessage.fieldValidationError(
                    LMS_SETUP.ATTR_LMS_URL,
                    "lmsSetup:lmsUrl:notNull"));
        } else {
            // Try to connect to the URL
            if (!Utils.pingHost(lmsSetup.lmsApiUrl)) {
                missingAttrs.add(APIMessage.fieldValidationError(
                        LMS_SETUP.ATTR_LMS_URL,
                        "lmsSetup:lmsUrl:url.invalid"));
            }
        }

        // Client id is mandatory
        if (!lmsClientCredentials.hasClientId()) {
            missingAttrs.add(APIMessage.fieldValidationError(
                    LMS_SETUP.ATTR_LMS_CLIENTNAME,
                    "lmsSetup:lmsClientname:notNull"));
        }

        // Client secret is mandatory
        if (!lmsClientCredentials.hasSecret()) {
            missingAttrs.add(APIMessage.fieldValidationError(
                    LMS_SETUP.ATTR_LMS_CLIENTSECRET,
                    "lmsSetup:lmsClientsecret:notNull"));
        }

        if (!missingAttrs.isEmpty()) {
            return LmsSetupTestResult.ofMissingAttributes(LmsType.OAT, missingAttrs);
        }

        return LmsSetupTestResult.ofOkay(LmsType.OAT);
    }

    @Override
    public Result<List<QuizData>> getQuizzes(final FilterMap filterMap) {
        return this
                .protectedQuizzesRequest(filterMap)
                .map(quizzes -> quizzes.stream()
                        .filter(LmsAPIService.quizFilterPredicate(filterMap))
                        .collect(Collectors.toList()));
    }

    @Override
    public Result<Collection<QuizData>> getQuizzes(final Set<String> ids) {
        return Result.tryCatch(() -> {
            final HashSet<String> leftIds = new HashSet<>(ids);
            final Collection<QuizData> result = new ArrayList<>();
            ids.stream()
                    .map(id -> super.getFromCache(id))
                    .forEach(q -> {
                        if (q != null) {
                            leftIds.remove(q.id);
                            result.add(q);
                        }
                    });

            if (!leftIds.isEmpty()) {
                result.addAll(super.protectedQuizzesRequest(leftIds).getOrThrow());
            }

            return result;
        });
    }

    @Override
    public Result<QuizData> getQuiz(final String id) {
        final QuizData fromCache = super.getFromCache(id);
        if (fromCache != null) {
            return Result.of(fromCache);
        }

        return super.protectedQuizRequest(id);
    }

    @Override
    protected Supplier<List<QuizData>> allQuizzesSupplier(final FilterMap filterMap) {
        return () -> {
            final List<QuizData> res = getRestTemplate()
                    .map(t -> this.collectAllQuizzes(t, filterMap))
                    .getOrThrow();
            super.putToCache(res);
            return res;
        };
    }

    private List<QuizData> collectAllQuizzes(final OATLmsRestTemplate restTemplate, final FilterMap filterMap) {
        final LmsSetup lmsSetup = this.apiTemplateDataSupplier.getLmsSetup();
        final String quizName = filterMap.getString(QuizData.FILTER_ATTR_QUIZ_NAME);

        String url = "api/assessment/seb/getAllAssessments/";

        try {

            final List<Object> as =
                this.apiGetList(restTemplate, url);
            return as.stream()
                .map(a -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();  
                        String json = mapper.writeValueAsString(a);
                        final AssessmentData result = this.jsonMapper.readValue(json, AssessmentData.class);
                        DateTimeFormatter datetimeformat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
                        DateTime startTime = datetimeformat.parseDateTime(result.start_time);
                        DateTime endTime = datetimeformat.parseDateTime(result.end_time);
                        return new QuizData(
                                String.format("%d", result.id_number),
                                lmsSetup.getInstitutionId(),
                                lmsSetup.id,
                                lmsSetup.getLmsType(),
                                result.name,
                                result.description,
                                startTime,
                                endTime,
                                result.start_url,
                                new HashMap<String, String>());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected Supplier<Collection<QuizData>> quizzesSupplier(final Set<String> ids) {
        return () -> ids.stream().map(id -> quizSupplier(id).get()).collect(Collectors.toList());
    }

    @Override
    protected Supplier<QuizData> quizSupplier(final String id) {
        return () -> getRestTemplate()
                .map(t -> this.quizById(t, id))
                .getOrThrow();
    }

    private QuizData quizById(final OATLmsRestTemplate restTemplate, final String id) {
        final LmsSetup lmsSetup = this.apiTemplateDataSupplier.getLmsSetup();
        final String url = String.format("/restapi/assessment_modes/%s", id);
        final AssessmentData a = this.apiGet(restTemplate, url, AssessmentData.class);
        final DateTime startTime = new DateTime(a.start_time);
        final DateTime endTime = new DateTime(a.end_time);
        return new QuizData(
                String.format("%d", a.id_number),
                lmsSetup.getInstitutionId(),
                lmsSetup.id,
                lmsSetup.getLmsType(),
                a.name,
                a.description,
                startTime,
                endTime,
                a.start_url,
                new HashMap<String, String>());
    }

    private ExamineeAccountDetails getExamineeById(final RestTemplate restTemplate, final String id) {
        final String url = String.format("/restapi/users/%s/name_username", id);
        final UserData u = this.apiGet(restTemplate, url, UserData.class);
        final Map<String, String> attrs = new HashMap<>();
        return new ExamineeAccountDetails(
                String.valueOf(u.key),
                u.lastName + ", " + u.firstName,
                u.username,
                "OAT API does not provide email addresses",
                attrs);
    }

    @Override
    protected Supplier<ExamineeAccountDetails> accountDetailsSupplier(final String id) {
        return () -> getRestTemplate()
                .map(t -> this.getExamineeById(t, id))
                .getOrThrow();
    }

    @Override
    protected Supplier<Chapters> getCourseChaptersSupplier(final String courseId) {
        return () -> {
            throw new UnsupportedOperationException("No Course Chapter available for OpenOAT LMS");
        };
    }

    private SEBRestriction getRestrictionForAssignmentId(final RestTemplate restTemplate, final String id) {
        final String url = String.format("/restapi/assessment_modes/%s/seb_restriction", id);
        final RestrictionData r = this.apiGet(restTemplate, url, RestrictionData.class);
        return new SEBRestriction(Long.valueOf(id), r.configKeys, r.browserExamKeys, new HashMap<String, String>());
    }

    private SEBRestriction setRestrictionForAssignmentId(
            final RestTemplate restTemplate,
            final String id,
            final SEBRestriction restriction) {

        final String url = String.format("/restapi/assessment_modes/%s/seb_restriction", id);
        final RestrictionDataPost post = new RestrictionDataPost();
        post.browserExamKeys = new ArrayList<>(restriction.browserExamKeys);
        post.configKeys = new ArrayList<>(restriction.configKeys);
        final RestrictionData r =
                this.apiPost(restTemplate, url, post, RestrictionDataPost.class, RestrictionData.class);
        return new SEBRestriction(Long.valueOf(id), r.configKeys, r.browserExamKeys, new HashMap<String, String>());
    }

    private SEBRestriction deleteRestrictionForAssignmentId(final RestTemplate restTemplate, final String id) {
        final String url = String.format("/restapi/assessment_modes/%s/seb_restriction", id);
        final RestrictionData r = this.apiDelete(restTemplate, url, RestrictionData.class);
        // OAT returns RestrictionData with null values upon deletion.
        // We return it here for consistency, even though SEB server does not need it
        return new SEBRestriction(Long.valueOf(id), r.configKeys, r.browserExamKeys, new HashMap<String, String>());
    }

    @Override
    public Result<SEBRestriction> getSEBClientRestriction(final Exam exam) {
        return getRestTemplate()
                .map(t -> this.getRestrictionForAssignmentId(t, exam.externalId));
    }

    @Override
    public Result<SEBRestriction> applySEBClientRestriction(
            final String externalExamId,
            final SEBRestriction sebRestrictionData) {
        return getRestTemplate()
                .map(t -> this.setRestrictionForAssignmentId(t, externalExamId, sebRestrictionData));
    }

    @Override
    public Result<Exam> releaseSEBClientRestriction(final Exam exam) {
        return getRestTemplate()
                .map(t -> this.deleteRestrictionForAssignmentId(t, exam.externalId))
                .map(x -> exam);
    }

    private <T> T apiGet(final RestTemplate restTemplate, final String url, final Class<T> type) {
        final LmsSetup lmsSetup = this.apiTemplateDataSupplier.getLmsSetup();
        final ResponseEntity<T> res = restTemplate.exchange(
                lmsSetup.lmsApiUrl + url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                type);
        return res.getBody();
    }

    private List<Object> apiGetList(final RestTemplate restTemplate, final String url) {
        final LmsSetup lmsSetup = this.apiTemplateDataSupplier.getLmsSetup();
        final ResponseEntity<String> res = restTemplate.exchange(
                lmsSetup.lmsApiUrl + url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class);
        log.debug("______ body: {}", res.getBody());
        try {
            log.debug("Body: *** {}", res.getBody());
            final String body = res.getBody();
            final assessmentArray result = this.jsonMapper.readValue(body, assessmentArray.class);
            log.debug("Body: *** {}", result.quizzes);
            return result.quizzes;
        } catch (Exception e) {
            log.error("Failed to access OAT course === ", e);
            return null;
        }
        // return res.getBody();
    }

    private void testConnection(final RestTemplate restTemplate, final String url) {
        
        try {
            final LmsSetup lmsSetup = this.apiTemplateDataSupplier.getLmsSetup();
            final ResponseEntity<String> res = restTemplate.exchange(
                lmsSetup.lmsApiUrl + url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class);
            
        } catch (Exception e) {
            log.error("Failed to access OAT course === ", e);
            throw e;
        }
        // return res.getBody();
    }



    

    private <P, R> R apiPost(final RestTemplate restTemplate, final String url, final P post, final Class<P> postType,
            final Class<R> responseType) {
        final LmsSetup lmsSetup = this.apiTemplateDataSupplier.getLmsSetup();
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", "application/json");
        final HttpEntity<P> requestEntity = new HttpEntity<>(post, httpHeaders);
        final ResponseEntity<R> res = restTemplate.exchange(
                lmsSetup.lmsApiUrl + url,
                HttpMethod.POST,
                requestEntity,
                responseType);
        return res.getBody();
    }

    private <T> T apiDelete(final RestTemplate restTemplate, final String url, final Class<T> type) {
        final LmsSetup lmsSetup = this.apiTemplateDataSupplier.getLmsSetup();
        final ResponseEntity<T> res = restTemplate.exchange(
                lmsSetup.lmsApiUrl + url,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                type);
        return res.getBody();
    }

    private Result<OATLmsRestTemplate> getRestTemplate() {
        return Result.tryCatch(() -> {
            if (this.cachedRestTemplate != null) {
                return this.cachedRestTemplate;
            }

            final LmsSetup lmsSetup = this.apiTemplateDataSupplier.getLmsSetup();
            final ClientCredentials credentials = this.apiTemplateDataSupplier.getLmsClientCredentials();
            final ProxyData proxyData = this.apiTemplateDataSupplier.getProxyData();

            final CharSequence plainClientId = credentials.clientId;
            final CharSequence plainClientSecret = this.clientCredentialService
                    .getPlainClientSecret(credentials)
                    .getOrThrow();

            final ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
            details.setAccessTokenUri(lmsSetup.lmsApiUrl + "api/assessment/seb/accessToken/");
            details.setClientId(plainClientId.toString());
            details.setClientSecret(plainClientSecret.toString());

            final ClientHttpRequestFactory clientHttpRequestFactory = this.clientHttpRequestFactoryService
                    .getClientHttpRequestFactory(proxyData)
                    .getOrThrow();

            final OATLmsRestTemplate template = new OATLmsRestTemplate(this.jsonMapper, details);
            template.setRequestFactory(clientHttpRequestFactory);

            this.cachedRestTemplate = template;
            return this.cachedRestTemplate;
        });
    }

}
