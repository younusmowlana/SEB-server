/*
 * Copyright (c) 2018 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gbl.model.session;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.seb.sebserver.gbl.api.EntityType;
import ch.ethz.seb.sebserver.gbl.model.Domain;
import ch.ethz.seb.sebserver.gbl.model.GrantEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ClientConnection implements GrantEntity {

    public enum ConnectionStatus {
        UNDEFINED(false, false),
        CONNECTION_REQUESTED(true, false),
        AUTHENTICATED(true, true),
        ACTIVE(false, true),
        CLOSED(false, false),
        DISABLED(false, false);

        public final boolean connectingStatus;
        public final boolean establishedStatus;
        public final boolean clientActiveStatus;

        ConnectionStatus(final boolean connectingStatus, final boolean establishedStatus) {
            this.connectingStatus = connectingStatus;
            this.establishedStatus = establishedStatus;
            this.clientActiveStatus = connectingStatus || establishedStatus;
        }

    }

    public static final ClientConnection EMPTY_CLIENT_CONNECTION = new ClientConnection(
            -1L, -1L, -1L,
            ConnectionStatus.UNDEFINED,
            null, null, null, null,
            false,
            null, null, null, null,
            false);

    public static final String FILTER_ATTR_EXAM_ID = Domain.CLIENT_CONNECTION.ATTR_EXAM_ID;
    public static final String FILTER_ATTR_STATUS = Domain.CLIENT_CONNECTION.ATTR_STATUS;
    public static final String FILTER_ATTR_SESSION_ID = Domain.CLIENT_CONNECTION.ATTR_EXAM_USER_SESSION_ID;
    public static final String FILTER_ATTR_IP_STRING = Domain.CLIENT_CONNECTION.ATTR_CLIENT_ADDRESS;

    @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_ID)
    public final Long id;

    @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_INSTITUTION_ID)
    public final Long institutionId;

    @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_EXAM_ID)
    public final Long examId;

    @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_STATUS)
    public final ConnectionStatus status;

    @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_CONNECTION_TOKEN)
    public final String connectionToken;

    @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_EXAM_USER_SESSION_ID)
    public final String userSessionId;

    @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_CLIENT_ADDRESS)
    public final String clientAddress;

    @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_VDI)
    public final Boolean vdi;

    @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_VDI_PAIR_TOKEN)
    public final String vdiPairToken;

    @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_REMOTE_PROCTORING_ROOM_ID)
    public final Long remoteProctoringRoomId;

    public final String virtualClientId;
    public final Long creationTime;
    public final Long updateTime;
    public final Boolean remoteProctoringRoomUpdate;

    @JsonCreator
    public ClientConnection(
            @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_ID) final Long id,
            @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_INSTITUTION_ID) final Long institutionId,
            @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_EXAM_ID) final Long examId,
            @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_STATUS) final ConnectionStatus status,
            @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_CONNECTION_TOKEN) final String connectionToken,
            @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_EXAM_USER_SESSION_ID) final String userSessionId,
            @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_CLIENT_ADDRESS) final String clientAddress,
            @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_VDI) final Boolean vdi,
            @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_VDI_PAIR_TOKEN) final String vdiPairToken,
            @JsonProperty(Domain.CLIENT_CONNECTION.ATTR_REMOTE_PROCTORING_ROOM_ID) final Long remoteProctoringRoomId) {

        this.id = id;
        this.institutionId = institutionId;
        this.examId = examId;
        this.status = status;
        this.connectionToken = connectionToken;
        this.userSessionId = userSessionId;
        this.clientAddress = clientAddress;
        this.vdi = vdi;
        this.virtualClientId = null;
        this.vdiPairToken = vdiPairToken;
        this.creationTime = 0L;
        this.updateTime = 0L;
        this.remoteProctoringRoomId = remoteProctoringRoomId;
        this.remoteProctoringRoomUpdate = false;
    }

    public ClientConnection(
            final Long id,
            final Long institutionId,
            final Long examId,
            final ConnectionStatus status,
            final String connectionToken,
            final String userSessionId,
            final String clientAddress,
            final String virtualClientId,
            final Boolean vdi,
            final String vdiPairToken,
            final Long creationTime,
            final Long updateTime,
            final Long remoteProctoringRoomId,
            final Boolean remoteProctoringRoomUpdate) {

        this.id = id;
        this.institutionId = institutionId;
        this.examId = examId;
        this.status = status;
        this.connectionToken = connectionToken;
        this.userSessionId = userSessionId;
        this.clientAddress = clientAddress;
        this.virtualClientId = virtualClientId;
        this.vdi = vdi;
        this.vdiPairToken = vdiPairToken;
        this.creationTime = creationTime;
        this.updateTime = updateTime;
        this.remoteProctoringRoomId = remoteProctoringRoomId;
        this.remoteProctoringRoomUpdate =
                (remoteProctoringRoomUpdate != null) ? remoteProctoringRoomUpdate : false;
    }

    @Override
    public EntityType entityType() {
        return EntityType.CLIENT_CONNECTION;
    }

    @Override
    public String getName() {
        return this.userSessionId;
    }

    @Override
    public String getModelId() {
        return (this.id != null)
                ? String.valueOf(this.id)
                : null;
    }

    public Long getId() {
        return this.id;
    }

    @Override
    public Long getInstitutionId() {
        return this.institutionId;
    }

    public Long getExamId() {
        return this.examId;
    }

    public ConnectionStatus getStatus() {
        return this.status;
    }

    public String getConnectionToken() {
        return this.connectionToken;
    }

    public String getClientAddress() {
        return this.clientAddress;
    }

    public String getUserSessionId() {
        return this.userSessionId;
    }

    @JsonIgnore
    public String getVirtualClientId() {
        return this.virtualClientId;
    }

    public Boolean getVdi() {
        return this.vdi;
    }

    public String getVdiPairToken() {
        return this.vdiPairToken;
    }

    @JsonIgnore
    public Long getCreationTime() {
        return this.creationTime;
    }

    @JsonIgnore
    public Long getUpdateTime() {
        return this.updateTime;
    }

    public Long getRemoteProctoringRoomId() {
        return this.remoteProctoringRoomId;
    }

    @JsonIgnore
    public Boolean getRemoteProctoringRoomUpdate() {
        return this.remoteProctoringRoomUpdate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ClientConnection other = (ClientConnection) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        } else if (!this.id.equals(other.id))
            return false;
        return true;
    }

    public boolean dataEquals(final ClientConnection other) {
        if (other == null) {
            return true;
        }
        if (this.clientAddress == null) {
            if (other.clientAddress != null)
                return false;
        } else if (!this.clientAddress.equals(other.clientAddress))
            return false;
        if (this.status != other.status)
            return false;
        if (this.userSessionId == null) {
            if (other.userSessionId != null)
                return false;
        } else if (!this.userSessionId.equals(other.userSessionId)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ClientConnection [id=");
        builder.append(this.id);
        builder.append(", institutionId=");
        builder.append(this.institutionId);
        builder.append(", examId=");
        builder.append(this.examId);
        builder.append(", status=");
        builder.append(this.status);
        builder.append(", connectionToken=");
        builder.append(this.connectionToken);
        builder.append(", userSessionId=");
        builder.append(this.userSessionId);
        builder.append(", clientAddress=");
        builder.append(this.clientAddress);
        builder.append(", vdi=");
        builder.append(this.vdi);
        builder.append(", vdiPairToken=");
        builder.append(this.vdiPairToken);
        builder.append(", remoteProctoringRoomId=");
        builder.append(this.remoteProctoringRoomId);
        builder.append(", virtualClientId=");
        builder.append(this.virtualClientId);
        builder.append(", creationTime=");
        builder.append(this.creationTime);
        builder.append(", updateTime=");
        builder.append(this.updateTime);
        builder.append(", remoteProctoringRoomUpdate=");
        builder.append(this.remoteProctoringRoomUpdate);
        builder.append("]");
        return builder.toString();
    }

    public static Predicate<ClientConnection> getStatusPredicate(final ConnectionStatus status) {
        return connection -> connection.status == status;
    }

    public static Predicate<ClientConnection> getStatusPredicate(final ConnectionStatus... status) {
        final EnumSet<ConnectionStatus> states = EnumSet.allOf(ConnectionStatus.class);
        if (status != null) {
            Collections.addAll(states, status);
        }
        return connection -> states.contains(connection.status);
    }

}
