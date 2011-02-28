/*******************************************************************************
 * Copyright (c) 2004-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.propertypage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

public class ChangelogPage extends PropertyPage {

	private static final String RPM_CHANGELOG_ENTRIES = Messages
			.getString("ChangelogPage.entries"); //$NON-NLS-1$

	private static final int CL_ENTRIES_FIELD_WIDTH = 80;

	private static final int CL_ENTRIES_FIELD_HEIGHT = 50;

	private Text rpm_ChangelogEntriesText;

	/**
	 * Constructor for RPMPropertyPage.
	 */
	public ChangelogPage() {
		super();
	}

	private void addChangelogField(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// RPM labels and text fields setup

		Label rpmChangelogEntriesLabel = new Label(composite, SWT.NONE);
		rpmChangelogEntriesLabel.setText(RPM_CHANGELOG_ENTRIES);
		rpm_ChangelogEntriesText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdEntries = new GridData();
		gdEntries.widthHint = convertWidthInCharsToPixels(CL_ENTRIES_FIELD_WIDTH);
		gdEntries.heightHint = convertWidthInCharsToPixels(CL_ENTRIES_FIELD_HEIGHT);
		rpm_ChangelogEntriesText.setLayoutData(gdEntries);

		try {
			String rpm_ChangelogEntries = RPMQuery
					.getChangelog((IFile) getElement());
			rpm_ChangelogEntriesText.setText(rpm_ChangelogEntries);
		} catch (CoreException e) {
			StatusManager.getManager().handle(new StatusAdapter(e.getStatus()),
					StatusManager.LOG | StatusManager.SHOW);
		}

	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addChangelogField(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}
}