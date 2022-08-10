/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.service.examconfig.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import ch.ethz.seb.sebserver.gbl.model.sebconfig.AttributeType;
import ch.ethz.seb.sebserver.gbl.model.sebconfig.ConfigurationAttribute;
import ch.ethz.seb.sebserver.gbl.model.sebconfig.Orientation;
import ch.ethz.seb.sebserver.gbl.profile.GuiProfile;
import ch.ethz.seb.sebserver.gui.service.examconfig.InputField;
import ch.ethz.seb.sebserver.gui.service.examconfig.InputFieldBuilder;
import ch.ethz.seb.sebserver.gui.widget.MultiSelectionCheckbox;
import ch.ethz.seb.sebserver.gui.widget.Selection;
import ch.ethz.seb.sebserver.gui.widget.WidgetFactory;

@Lazy
@Component
@GuiProfile
public class MultiCheckboxSelection extends SelectionFieldBuilder implements InputFieldBuilder {

    private final WidgetFactory widgetFactory;

    protected MultiCheckboxSelection(final WidgetFactory widgetFactory) {
        this.widgetFactory = widgetFactory;
    }

    @Override
    public boolean builderFor(
            final ConfigurationAttribute attribute,
            final Orientation orientation) {

        return attribute != null && attribute.type == AttributeType.MULTI_CHECKBOX_SELECTION;
    }

    @Override
    public InputField createInputField(
            final Composite parent,
            final ConfigurationAttribute attribute,
            final ViewContext viewContext) {

        final Orientation orientation = viewContext
                .getOrientation(attribute.id);
        final Composite innerGrid = InputFieldBuilder
                .createInnerGrid(parent, attribute, orientation);

        final MultiSelectionCheckbox selection = this.widgetFactory.selectionLocalized(
                Selection.Type.MULTI_CHECKBOX,
                innerGrid,
                () -> this.getLocalizedResources(attribute, viewContext),
                null,
                () -> this.getLocalizedResourcesAsToolTip(attribute, viewContext))
                .getTypeInstance();

        selection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        final MultiSelectionCheckboxInputField multiSelectionCheckboxInputField = new MultiSelectionCheckboxInputField(
                attribute,
                orientation,
                selection);

        if (viewContext.readonly) {
            selection.setEnabled(false);
        } else {
            selection.setSelectionListener(event -> {
                multiSelectionCheckboxInputField.clearError();
                viewContext.getValueChangeListener().valueChanged(
                        viewContext,
                        attribute,
                        multiSelectionCheckboxInputField.getValue(),
                        multiSelectionCheckboxInputField.listIndex);
            });
        }

        return multiSelectionCheckboxInputField;
    }

    static final class MultiSelectionCheckboxInputField extends AbstractInputField<MultiSelectionCheckbox> {

        protected MultiSelectionCheckboxInputField(
                final ConfigurationAttribute attribute,
                final Orientation orientation,
                final MultiSelectionCheckbox control) {

            super(attribute, orientation, control, null);
        }

        @Override
        public String getValue() {
            return super.control.getSelectionValue();
        }

        @Override
        protected void setValueToControl(final String value) {
            super.control.select(value);
        }

    }

}
