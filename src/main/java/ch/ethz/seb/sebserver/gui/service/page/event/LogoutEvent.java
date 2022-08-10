/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.service.page.event;

import ch.ethz.seb.sebserver.gui.service.page.PageContext;

public final class LogoutEvent implements PageEvent {

    public final PageContext pageContext;

    public LogoutEvent(final PageContext pageContext) {
        this.pageContext = pageContext;
    }

}
