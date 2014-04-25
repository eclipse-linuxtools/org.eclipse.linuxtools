/*******************************************************************************
 * Copyright (c) 2004, 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.propertypage;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Property page displaying the generic info fetched from the RPM headers.
 *
 */
public class SpecFileHeaderPage extends AbstractRPMPropertyPage {

    private static final int QI_FIELD_WIDTH = 80;

    private static final int QI_FIELD_HEIGHT = 40;

    @Override
    protected void addFields(Composite composite) {

        // RPM labels and text fields setup

        Label rpmDescriptionLabel = new Label(composite, SWT.NONE);
        rpmDescriptionLabel.setText(Messages
                .getString("SpecFileHeaderPage.info")); //$NON-NLS-1$
        Text rpmQiText = new Text(composite, SWT.MULTI | SWT.BORDER
                | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
        GridData gdQI = new GridData();
        gdQI.widthHint = convertWidthInCharsToPixels(QI_FIELD_WIDTH);
        gdQI.heightHint = convertWidthInCharsToPixels(QI_FIELD_HEIGHT);
        rpmQiText.setLayoutData(gdQI);

        // Populate RPM text field
        String rpm_qi = RPMQuery.getHeaderInfo((IFile) getElement());
        rpmQiText.setText(rpm_qi);
    }

}