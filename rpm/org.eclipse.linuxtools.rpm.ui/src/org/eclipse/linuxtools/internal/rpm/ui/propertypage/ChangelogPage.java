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
 * Property page showing the changelog as fetched from the RPM header.
 *
 */
public class ChangelogPage extends AbstractRPMPropertyPage {

	private static final int CL_ENTRIES_FIELD_WIDTH = 80;

	private static final int CL_ENTRIES_FIELD_HEIGHT = 50;

	@Override
	protected void addFields(Composite composite) {

		// RPM labels and text fields setup

		Label rpmChangelogEntriesLabel = new Label(composite, SWT.NONE);
		rpmChangelogEntriesLabel.setText(Messages
				.getString("ChangelogPage.entries")); //$NON-NLS-1$
		Text rpmChangelogEntriesText = new Text(composite, SWT.MULTI
				| SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdEntries = new GridData();
		gdEntries.widthHint = convertWidthInCharsToPixels(CL_ENTRIES_FIELD_WIDTH);
		gdEntries.heightHint = convertWidthInCharsToPixels(CL_ENTRIES_FIELD_HEIGHT);
		rpmChangelogEntriesText.setLayoutData(gdEntries);

		String rpm_ChangelogEntries = RPMQuery
				.getChangelog((IFile) getElement());
		rpmChangelogEntriesText.setText(rpm_ChangelogEntries);
	}
}