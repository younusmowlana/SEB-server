/*
 * Copyright (c) 2021 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.lms.impl.oat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public final class OATLmsData {

    @JsonIgnoreProperties(ignoreUnknown = true)
    static public final class AssessmentData {
        /*
         * OAT API example:
         * {
         * "courseName": "course 1",
         * "dateFrom": 1624420800000,
         * "dateTo": 1624658400000,
         * "description": "",
         * "key": 6356992,
         * “repositoryEntryKey”: 462324,
         * "name": "SEB test"
         * }
         */
        public long id_number;
        public String start_url;
        public String name;
        public String description;
        public String start_time;
        public String end_time;

        @JsonCreator
        public AssessmentData(
                @JsonProperty(value = "id_number") final long id_number,
                @JsonProperty(value = "start_url") final String start_url,
                @JsonProperty(value = "name") final String name,
                @JsonProperty(value = "description") final String description,
                @JsonProperty(value = "start_time") final String start_time,
                @JsonProperty(value = "end_time") final String end_time) {
            this.id_number = id_number;
            this.start_url = start_url;
            this.name = name;
            this.description = description;
            this.start_time = start_time;
            this.end_time = end_time;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class assessmentArray {
        final List<Object> quizzes;

        @JsonCreator
        public assessmentArray(
                @JsonProperty(value = "quizzes") final List<Object> quizzes) {
            this.quizzes = quizzes;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class accessToken {
        final String accessToken;

        @JsonCreator
        public accessToken(
                @JsonProperty(value = "token") final String accessToken) {
            this.accessToken = accessToken;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class UserData {
        public long key;
        public String firstName;
        public String lastName;
        public String username;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class RestrictionData {
        /*
         * OAT API example:
         * {
         * "browserExamKeys": [ "1" ],
         * "configKeys": null,
         * "key": 8028160
         * }
         */
        public long key;
        public List<String> browserExamKeys;
        public List<String> configKeys;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class RestrictionDataPost {
        /*
         * OAT API example:
         * {
         * "configKeys": ["a", "b"],
         * "browserExamKeys": ["1", "2"]
         * }
         */
        public List<String> browserExamKeys;
        public List<String> configKeys;
    }

}
