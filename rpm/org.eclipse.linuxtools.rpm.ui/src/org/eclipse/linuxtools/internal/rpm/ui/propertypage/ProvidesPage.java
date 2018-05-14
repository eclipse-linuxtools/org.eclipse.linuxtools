/*******************************************************************************
 * Copyright (c) 2004, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * Property page displaying provides as fetched by RPM headers.
 *
 */
public class ProvidesPage extends AbstractRPMPropertyPage {

	private static final int QL_FIELD_WIDTH = 80;

	private static final int QL_FIELD_HEIGHT = 40;

	@Override
	protected void addFields(Composite composite) {
		// RPM labels and text fields setup

		Label rpmDescriptionLabel = new Label(composite, SWT.NONE);
		rpmDescriptionLabel.setText(Messages.getString("ProvidesPage.Provides")); //$NON-NLS-1$
		Text rpmQlText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdQL = new GridData();
		gdQL.widthHint = convertWidthInCharsToPixels(QL_FIELD_WIDTH);
		gdQL.heightHint = convertWidthInCharsToPixels(QL_FIELD_HEIGHT);
		rpmQlText.setLayoutData(gdQL);

		// Populate RPM text fields
		String rpm_ql = RPMQuery.getProvides((IFile) getElement());
		rpmQlText.setText(rpm_ql);
	}

}