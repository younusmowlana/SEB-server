/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.content;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import ch.ethz.seb.sebserver.gbl.api.API;
import ch.ethz.seb.sebserver.gbl.api.EntityType;
import ch.ethz.seb.sebserver.gbl.async.AsyncRunner;
import ch.ethz.seb.sebserver.gbl.model.Domain;
import ch.ethz.seb.sebserver.gbl.model.EntityKey;
import ch.ethz.seb.sebserver.gbl.model.exam.Exam;
import ch.ethz.seb.sebserver.gbl.model.exam.Indicator;
import ch.ethz.seb.sebserver.gbl.model.exam.ProctoringServiceSettings;
import ch.ethz.seb.sebserver.gbl.model.exam.ProctoringServiceSettings.ProctoringFeature;
import ch.ethz.seb.sebserver.gbl.model.session.ClientConnection.ConnectionStatus;
import ch.ethz.seb.sebserver.gbl.model.session.ClientConnectionData;
import ch.ethz.seb.sebserver.gbl.model.user.UserRole;
import ch.ethz.seb.sebserver.gbl.profile.GuiProfile;
import ch.ethz.seb.sebserver.gbl.util.Tuple;
import ch.ethz.seb.sebserver.gbl.util.Utils;
import ch.ethz.seb.sebserver.gui.GuiServiceInfo;
import ch.ethz.seb.sebserver.gui.content.action.ActionDefinition;
import ch.ethz.seb.sebserver.gui.service.ResourceService;
import ch.ethz.seb.sebserver.gui.service.i18n.LocTextKey;
import ch.ethz.seb.sebserver.gui.service.page.PageContext;
import ch.ethz.seb.sebserver.gui.service.page.PageMessageException;
import ch.ethz.seb.sebserver.gui.service.page.PageService;
import ch.ethz.seb.sebserver.gui.service.page.PageService.PageActionBuilder;
import ch.ethz.seb.sebserver.gui.service.page.TemplateComposer;
import ch.ethz.seb.sebserver.gui.service.page.event.ActionActivationEvent;
import ch.ethz.seb.sebserver.gui.service.page.impl.PageAction;
import ch.ethz.seb.sebserver.gui.service.push.ServerPushContext;
import ch.ethz.seb.sebserver.gui.service.push.ServerPushService;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.RestCall;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.RestService;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.exam.GetExam;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.exam.GetIndicators;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.exam.GetProctoringSettings;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.session.GetClientConnectionDataList;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.auth.CurrentUser;
import ch.ethz.seb.sebserver.gui.service.session.ClientConnectionTable;
import ch.ethz.seb.sebserver.gui.service.session.InstructionProcessor;
import ch.ethz.seb.sebserver.gui.service.session.proctoring.MonitoringProctoringService;
import ch.ethz.seb.sebserver.gui.service.session.proctoring.ProctoringGUIService;
import ch.ethz.seb.sebserver.gui.widget.Message;

@Lazy
@Component
@GuiProfile
public class MonitoringRunningExam implements TemplateComposer {

    private static final Logger log = LoggerFactory.getLogger(MonitoringRunningExam.class);

    private static final LocTextKey EMPTY_SELECTION_TEXT_KEY =
            new LocTextKey("sebserver.monitoring.exam.connection.emptySelection");
    private static final LocTextKey EMPTY_ACTIVE_SELECTION_TEXT_KEY =
            new LocTextKey("sebserver.monitoring.exam.connection.emptySelection.active");
    private static final LocTextKey CONFIRM_QUIT_SELECTED =
            new LocTextKey("sebserver.monitoring.exam.connection.action.instruction.quit.selected.confirm");
    private static final LocTextKey CONFIRM_QUIT_ALL =
            new LocTextKey("sebserver.monitoring.exam.connection.action.instruction.quit.all.confirm");
    private static final LocTextKey CONFIRM_OPEN_TOWNHALL =
            new LocTextKey("sebserver.monitoring.exam.connection.action.openTownhall.confirm");
    private static final LocTextKey CONFIRM_CLOSE_TOWNHALL =
            new LocTextKey("sebserver.monitoring.exam.connection.action.closeTownhall.confirm");
    private static final LocTextKey CONFIRM_DISABLE_SELECTED =
            new LocTextKey("sebserver.monitoring.exam.connection.action.instruction.disable.selected.confirm");

    private final ServerPushService serverPushService;
    private final PageService pageService;
    private final RestService restService;
    private final ResourceService resourceService;
    private final AsyncRunner asyncRunner;
    private final InstructionProcessor instructionProcessor;
    private final MonitoringExamSearchPopup monitoringExamSearchPopup;
    private final MonitoringProctoringService monitoringProctoringService;
    private final boolean distributedSetup;
    private final long pollInterval;
    private final long proctoringRoomUpdateInterval;

    protected MonitoringRunningExam(
            final ServerPushService serverPushService,
            final PageService pageService,
            final AsyncRunner asyncRunner,
            final InstructionProcessor instructionProcessor,
            final MonitoringExamSearchPopup monitoringExamSearchPopup,
            final MonitoringProctoringService monitoringProctoringService,
            final GuiServiceInfo guiServiceInfo,
            @Value("${sebserver.gui.webservice.poll-interval:1000}") final long pollInterval,
            @Value("${sebserver.gui.remote.proctoring.rooms.update.poll-interval:5000}") final long proctoringRoomUpdateInterval) {

        this.serverPushService = serverPushService;
        this.pageService = pageService;
        this.restService = pageService.getRestService();
        this.resourceService = pageService.getResourceService();
        this.asyncRunner = asyncRunner;
        this.instructionProcessor = instructionProcessor;
        this.monitoringProctoringService = monitoringProctoringService;
        this.pollInterval = pollInterval;
        this.distributedSetup = guiServiceInfo.isDistributedSetup();
        this.monitoringExamSearchPopup = monitoringExamSearchPopup;
        this.proctoringRoomUpdateInterval = proctoringRoomUpdateInterval;
    }

    @Override
    public void compose(final PageContext pageContext) {
        final RestService restService = this.resourceService.getRestService();
        final EntityKey entityKey = pageContext.getEntityKey();
        final CurrentUser currentUser = this.resourceService.getCurrentUser();
        final Exam exam = restService.getBuilder(GetExam.class)
                .withURIVariable(API.PARAM_MODEL_ID, entityKey.modelId)
                .call()
                .getOrThrow();

        final Collection<Indicator> indicators = restService.getBuilder(GetIndicators.class)
                .withQueryParam(Indicator.FILTER_ATTR_EXAM_ID, entityKey.modelId)
                .call()
                .getOrThrow();

        final Composite content = this.pageService.getWidgetFactory().defaultPageLayout(
                pageContext.getParent(),
                new LocTextKey("sebserver.monitoring.exam", exam.name));

        final Composite tablePane = new Composite(content, SWT.NONE);
        tablePane.setLayout(new GridLayout());
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = 100;
        tablePane.setLayoutData(gridData);

        final PageActionBuilder actionBuilder = this.pageService
                .pageActionBuilder(pageContext.clearEntityKeys());

        final RestCall<Collection<ClientConnectionData>>.RestCallBuilder restCall =
                restService.getBuilder(GetClientConnectionDataList.class)
                        .withURIVariable(API.PARAM_PARENT_MODEL_ID, exam.getModelId());

        final ProctoringUpdateErrorHandler proctoringUpdateErrorHandler =
                new ProctoringUpdateErrorHandler(this.pageService, pageContext);

        final ServerPushContext pushContext = new ServerPushContext(
                content,
                Utils.truePredicate(),
                proctoringUpdateErrorHandler);

        final ClientConnectionTable clientTable = new ClientConnectionTable(
                this.pageService,
                tablePane,
                this.asyncRunner,
                exam,
                indicators,
                restCall,
                pushContext,
                this.distributedSetup);

        clientTable
                .withDefaultAction(
                        actionBuilder
                                .newAction(ActionDefinition.MONITOR_EXAM_CLIENT_CONNECTION)
                                .withParentEntityKey(entityKey)
                                .create(),
                        this.pageService)
                .withSelectionListener(this.pageService.getSelectionPublisher(
                        pageContext,
                        ActionDefinition.MONITOR_EXAM_CLIENT_CONNECTION,
                        ActionDefinition.MONITOR_EXAM_QUIT_SELECTED,
                        ActionDefinition.MONITOR_EXAM_DISABLE_SELECTED_CONNECTION,
                        ActionDefinition.MONITOR_EXAM_NEW_PROCTOR_ROOM));

        this.serverPushService.runServerPush(
                pushContext,
                this.pollInterval,
                context -> clientTable.updateValues(),
                updateTableGUI(clientTable));

        final BooleanSupplier isExamSupporter = () -> currentUser.get().hasRole(UserRole.EXAM_SUPPORTER);

        actionBuilder

                .newAction(ActionDefinition.MONITOR_EXAM_CLIENT_CONNECTION)
                .withParentEntityKey(entityKey)
                .withExec(pageAction -> {
                    final Tuple<String> singleSelection = clientTable.getSingleSelection();
                    if (singleSelection == null) {
                        throw new PageMessageException(EMPTY_SELECTION_TEXT_KEY);
                    }

                    final PageAction copyOfPageAction = PageAction.copyOf(pageAction);
                    copyOfPageAction.withEntityKey(new EntityKey(
                            singleSelection._1,
                            EntityType.CLIENT_CONNECTION));
                    copyOfPageAction.withAttribute(
                            Domain.CLIENT_CONNECTION.ATTR_CONNECTION_TOKEN,
                            singleSelection._2);

                    return copyOfPageAction;
                })
                .publishIf(isExamSupporter, false)

                .newAction(ActionDefinition.MONITOR_EXAM_QUIT_ALL)
                .withEntityKey(entityKey)
                .withConfirm(() -> CONFIRM_QUIT_ALL)
                .withExec(action -> this.quitSEBClients(action, clientTable, true))
                .noEventPropagation()
                .publishIf(isExamSupporter)

                .newAction(ActionDefinition.MONITORING_EXAM_SEARCH_CONNECTIONS)
                .withEntityKey(entityKey)
                .withExec(this::openSearchPopup)
                .noEventPropagation()
                .publishIf(isExamSupporter)

                .newAction(ActionDefinition.MONITOR_EXAM_QUIT_SELECTED)
                .withEntityKey(entityKey)
                .withConfirm(() -> CONFIRM_QUIT_SELECTED)
                .withSelect(
                        () -> this.selectionForQuitInstruction(clientTable),
                        action -> this.quitSEBClients(action, clientTable, false),
                        EMPTY_ACTIVE_SELECTION_TEXT_KEY)
                .noEventPropagation()
                .publishIf(isExamSupporter, false)

                .newAction(ActionDefinition.MONITOR_EXAM_DISABLE_SELECTED_CONNECTION)
                .withEntityKey(entityKey)
                .withConfirm(() -> CONFIRM_DISABLE_SELECTED)
                .withSelect(
                        clientTable::getSelection,
                        action -> this.disableSEBClients(action, clientTable, false),
                        EMPTY_SELECTION_TEXT_KEY)
                .noEventPropagation()
                .publishIf(isExamSupporter, false);

        if (isExamSupporter.getAsBoolean()) {
            addFilterActions(actionBuilder, clientTable, isExamSupporter);
            addProctoringActions(
                    currentUser.getProctoringGUIService(),
                    pageContext,
                    content,
                    actionBuilder);
        }
    }

    private void addProctoringActions(
            final ProctoringGUIService proctoringGUIService,
            final PageContext pageContext,
            final Composite parent,
            final PageActionBuilder actionBuilder) {

        final EntityKey entityKey = pageContext.getEntityKey();
        final ProctoringServiceSettings proctoringSettings = this.restService
                .getBuilder(GetProctoringSettings.class)
                .withURIVariable(API.PARAM_MODEL_ID, entityKey.modelId)
                .call()
                .getOr(null);

        if (proctoringSettings != null && proctoringSettings.enableProctoring) {
            if (proctoringSettings.enabledFeatures.contains(ProctoringFeature.TOWN_HALL)) {

                actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_OPEN_TOWNHALL_PROCTOR_ROOM)
                        .withEntityKey(entityKey)
                        .withConfirm(action -> {
                            if (!this.monitoringProctoringService.isTownhallRoomActive(action.getEntityKey().modelId)) {
                                return CONFIRM_OPEN_TOWNHALL;
                            } else {
                                return CONFIRM_CLOSE_TOWNHALL;
                            }
                        })
                        .withExec(action -> this.monitoringProctoringService.toggleTownhallRoom(
                                proctoringGUIService,
                                action))
                        .noEventPropagation()
                        .publish();

                if (this.monitoringProctoringService.isTownhallRoomActive(entityKey.modelId)) {
                    this.pageService.firePageEvent(
                            new ActionActivationEvent(
                                    true,
                                    new Tuple<>(
                                            ActionDefinition.MONITOR_EXAM_OPEN_TOWNHALL_PROCTOR_ROOM,
                                            ActionDefinition.MONITOR_EXAM_CLOSE_TOWNHALL_PROCTOR_ROOM)),
                            pageContext);
                }
            }

            final ProctoringUpdateErrorHandler proctoringUpdateErrorHandler =
                    new ProctoringUpdateErrorHandler(this.pageService, pageContext);

            final ServerPushContext pushContext = new ServerPushContext(
                    parent,
                    Utils.truePredicate(),
                    proctoringUpdateErrorHandler);

            this.monitoringProctoringService.initCollectingRoomActions(
                    pushContext,
                    pageContext,
                    actionBuilder,
                    proctoringSettings,
                    proctoringGUIService);

            this.serverPushService.runServerPush(
                    pushContext,
                    this.proctoringRoomUpdateInterval,
                    context -> this.monitoringProctoringService.updateCollectingRoomActions(
                            context,
                            pageContext,
                            actionBuilder,
                            proctoringSettings,
                            proctoringGUIService));
        }
    }

    private void addFilterActions(
            final PageActionBuilder actionBuilder,
            final ClientConnectionTable clientTable,
            final BooleanSupplier isExamSupporter) {

        addClosedFilterAction(actionBuilder, clientTable);
        addRequestedFilterAction(actionBuilder, clientTable);
        addDisabledFilterAction(actionBuilder, clientTable);
    }

    private void addDisabledFilterAction(
            final PageActionBuilder actionBuilder,
            final ClientConnectionTable clientTable) {

        if (clientTable.isStatusHidden(ConnectionStatus.DISABLED)) {
            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_SHOW_DISABLED_CONNECTION)
                    .withExec(showStateViewAction(clientTable, ConnectionStatus.DISABLED))
                    .noEventPropagation()
                    .withSwitchAction(
                            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_HIDE_DISABLED_CONNECTION)
                                    .withExec(hideStateViewAction(clientTable, ConnectionStatus.DISABLED))
                                    .noEventPropagation()
                                    .create())
                    .publish();
        } else {
            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_HIDE_DISABLED_CONNECTION)
                    .withExec(hideStateViewAction(clientTable, ConnectionStatus.DISABLED))
                    .noEventPropagation()
                    .withSwitchAction(
                            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_SHOW_DISABLED_CONNECTION)
                                    .withExec(showStateViewAction(clientTable, ConnectionStatus.DISABLED))
                                    .noEventPropagation()
                                    .create())
                    .publish();
        }
    }

    private void addRequestedFilterAction(
            final PageActionBuilder actionBuilder,
            final ClientConnectionTable clientTable) {

        if (clientTable.isStatusHidden(ConnectionStatus.CONNECTION_REQUESTED)) {
            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_SHOW_REQUESTED_CONNECTION)
                    .withExec(showStateViewAction(clientTable, ConnectionStatus.CONNECTION_REQUESTED))
                    .noEventPropagation()
                    .withSwitchAction(
                            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_HIDE_REQUESTED_CONNECTION)
                                    .withExec(
                                            hideStateViewAction(clientTable, ConnectionStatus.CONNECTION_REQUESTED))
                                    .noEventPropagation()
                                    .create())
                    .publish();
        } else {
            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_HIDE_REQUESTED_CONNECTION)
                    .withExec(hideStateViewAction(clientTable, ConnectionStatus.CONNECTION_REQUESTED))
                    .noEventPropagation()
                    .withSwitchAction(
                            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_SHOW_REQUESTED_CONNECTION)
                                    .withExec(
                                            showStateViewAction(clientTable, ConnectionStatus.CONNECTION_REQUESTED))
                                    .noEventPropagation()
                                    .create())
                    .publish();
        }
    }

    private void addClosedFilterAction(
            final PageActionBuilder actionBuilder,
            final ClientConnectionTable clientTable) {

        if (clientTable.isStatusHidden(ConnectionStatus.CLOSED)) {
            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_SHOW_CLOSED_CONNECTION)
                    .withExec(showStateViewAction(clientTable, ConnectionStatus.CLOSED))
                    .noEventPropagation()
                    .withSwitchAction(
                            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_HIDE_CLOSED_CONNECTION)
                                    .withExec(hideStateViewAction(clientTable, ConnectionStatus.CLOSED))
                                    .noEventPropagation()
                                    .create())
                    .publish();
        } else {
            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_HIDE_CLOSED_CONNECTION)
                    .withExec(hideStateViewAction(clientTable, ConnectionStatus.CLOSED))
                    .noEventPropagation()
                    .withSwitchAction(
                            actionBuilder.newAction(ActionDefinition.MONITOR_EXAM_SHOW_CLOSED_CONNECTION)
                                    .withExec(showStateViewAction(clientTable, ConnectionStatus.CLOSED))
                                    .noEventPropagation()
                                    .create())
                    .publish();
        }
    }

    private PageAction openSearchPopup(final PageAction action) {
        this.monitoringExamSearchPopup.show(action.pageContext());
        return action;
    }

    private static Function<PageAction, PageAction> showStateViewAction(
            final ClientConnectionTable clientTable,
            final ConnectionStatus status) {

        return action -> {
            clientTable.showStatus(status);
            clientTable.removeSelection();
            return action;
        };
    }

    private static Function<PageAction, PageAction> hideStateViewAction(
            final ClientConnectionTable clientTable,
            final ConnectionStatus status) {

        return action -> {
            clientTable.hideStatus(status);
            clientTable.removeSelection();
            return action;
        };
    }

    private Set<EntityKey> selectionForQuitInstruction(final ClientConnectionTable clientTable) {
        final Set<String> connectionTokens = clientTable.getConnectionTokens(
                cc -> cc.status.clientActiveStatus,
                true);
        if (connectionTokens == null || connectionTokens.isEmpty()) {
            return Collections.emptySet();
        }

        return clientTable.getSelection();
    }

    private PageAction quitSEBClients(
            final PageAction action,
            final ClientConnectionTable clientTable,
            final boolean all) {

        this.instructionProcessor.propagateSEBQuitInstruction(
                clientTable.getExam().id,
                statesPredicate -> clientTable.getConnectionTokens(
                        statesPredicate,
                        !all),
                action.pageContext());

        clientTable.removeSelection();
        clientTable.forceUpdateAll();
        return action;
    }

    private PageAction disableSEBClients(
            final PageAction action,
            final ClientConnectionTable clientTable,
            final boolean all) {

        this.instructionProcessor.disableConnection(
                clientTable.getExam().id,
                statesPredicate -> clientTable.getConnectionTokens(
                        statesPredicate,
                        !all),
                action.pageContext());

        clientTable.removeSelection();
        clientTable.forceUpdateAll();
        return action;
    }

    private Consumer<ServerPushContext> updateTableGUI(final ClientConnectionTable clientTable) {
        return context -> {
            if (!context.isDisposed()) {
                try {
                    clientTable.updateGUI();
                    context.layout();
                } catch (final Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Unexpected error while trying to update GUI: ", e);
                    }
                }
            }
        };
    }

    static final class ProctoringUpdateErrorHandler implements Function<Exception, Boolean> {

        private final PageService pageService;
        private final PageContext pageContext;

        private int errors = 0;

        public ProctoringUpdateErrorHandler(
                final PageService pageService,
                final PageContext pageContext) {

            this.pageService = pageService;
            this.pageContext = pageContext;
        }

        private boolean checkUserSession() {
            try {
                this.pageService.getCurrentUser().get();
                return true;
            } catch (final Exception e) {
                try {
                    this.pageContext.forwardToLoginPage();
                    final MessageBox logoutSuccess = new Message(
                            this.pageContext.getShell(),
                            this.pageService.getI18nSupport().getText("sebserver.logout"),
                            Utils.formatLineBreaks(
                                    this.pageService.getI18nSupport()
                                            .getText("sebserver.logout.invalid-session.message")),
                            SWT.ICON_INFORMATION,
                            this.pageService.getI18nSupport());
                    logoutSuccess.open(null);
                } catch (final Exception ee) {
                    log.warn("Unable to auto-logout: ", ee.getMessage());
                }
                return true;
            }
        }

        @Override
        public Boolean apply(final Exception error) {
            this.errors++;
            log.error("Failed to update server push: {}", error.getMessage());
            if (this.errors > 5) {
                checkUserSession();
            }
            return this.errors > 5;
        }
    }

}
