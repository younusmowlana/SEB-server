/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.dao;

import java.util.List;
import java.util.Map;

import ch.ethz.seb.sebserver.gbl.model.sebconfig.ConfigurationNode;
import ch.ethz.seb.sebserver.gbl.model.sebconfig.View;
import ch.ethz.seb.sebserver.gbl.util.Result;

public interface ViewDAO extends EntityDAO<View, View> {

    Result<Map<Long, Long>> copyDefaultViewsForTemplate(ConfigurationNode node);

    Result<List<View>> getDefaultTemplateViews();

    Result<View> getDefaultViewForTemplate(Long templateId, Long defaultViewId);

}
