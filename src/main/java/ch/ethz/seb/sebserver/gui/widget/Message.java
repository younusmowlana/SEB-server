/*
 * Copyright (c) 2018 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.widget;

import java.util.Locale;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import ch.ethz.seb.sebserver.gbl.util.Utils;
import ch.ethz.seb.sebserver.gui.service.i18n.I18nSupport;
import ch.ethz.seb.sebserver.gui.widget.WidgetFactory.CustomVariant;

public final class Message extends MessageBox {

    private static final int NORMAL_WIDTH = 400;
    private static final long serialVersionUID = 6973272221493264432L;
    private final I18nSupport i18nSupport;

    public Message(
            final Shell parent,
            final String title,
            final String message,
            final int type,
            final I18nSupport i18nSupport) {

        super(parent, type);
        super.setText(title);
        super.setMessage(message);
        this.i18nSupport = i18nSupport;
    }

    @Override
    protected void prepareOpen() {
        try {
            super.prepareOpen();
        } catch (final IllegalArgumentException e) {
            // fallback on markup text error
            super.setMessage(Utils.escapeHTML_XML_EcmaScript(super.getMessage()));
            super.prepareOpen();
        }
        final GridLayout layout = (GridLayout) super.shell.getLayout();
        layout.marginTop = 10;
        layout.marginLeft = 10;
        layout.marginRight = 10;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 10;
        super.shell.setData(RWT.CUSTOM_VARIANT, CustomVariant.MESSAGE.key);

        final Rectangle bounds = super.shell.getBounds();
        if (bounds.width < NORMAL_WIDTH) {
            bounds.x = bounds.x - (NORMAL_WIDTH - bounds.width) / 2;
            bounds.width = NORMAL_WIDTH;
            super.shell.setBounds(bounds);
        } else {
            super.shell.pack(true);
        }
    }

    @Override
    public void open(final DialogCallback dialogCallback) {
        final Locale locale = RWT.getLocale();
        RWT.setLocale(this.i18nSupport.getUsersLanguageLocale());
        super.open(dialogCallback);
        RWT.setLocale(locale);
    }

    @Override
    public int open() {
        final Locale locale = RWT.getLocale();
        RWT.setLocale(this.i18nSupport.getUsersLanguageLocale());
        final int open = super.open();
        RWT.setLocale(locale);
        return open;
    }

}
