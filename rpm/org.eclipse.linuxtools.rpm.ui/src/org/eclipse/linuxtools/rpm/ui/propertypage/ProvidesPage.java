/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.ui.propertypage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.eclipse.linuxtools.rpm.ui.util.ExceptionHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class ProvidesPage extends PropertyPage {
	
	private static final String RPM_QL = Messages.getString("ProvidesPage.Provides"); //$NON-NLS-1$

	private static final int NAME_FIELD_WIDTH = 20;

	private static final int QL_FIELD_WIDTH = 80;

	private static final int QL_FIELD_HEIGHT = 40;

	private Text rpm_nameText;

	private Text rpm_qlText;

	/**
	 * Constructor for RPMPropertyPage.
	 */
	public ProvidesPage() {
		super();
	}

	private void addProvidesField(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// RPM labels and text fields setup

		Label rpmDescriptionLabel = new Label(composite, SWT.NONE);
		rpmDescriptionLabel.setText(RPM_QL);
		rpm_qlText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY
				| SWT.V_SCROLL | SWT.WRAP);
		GridData gdQL = new GridData();
		gdQL.widthHint = convertWidthInCharsToPixels(QL_FIELD_WIDTH);
		gdQL.heightHint = convertWidthInCharsToPixels(QL_FIELD_HEIGHT);
		rpm_qlText.setLayoutData(gdQL);

		// Populate RPM text fields
		String rpm_path = ((IResource) getElement()).getRawLocation()
				.toString();
		
		try {
			String rpm_ql = RPMQuery.getProvides((IFile) getElement());
			rpm_qlText.setText(rpm_ql);
		} catch(CoreException e) {
			ExceptionHandler.handle(e, getShell(),
					Messages.getString("ErrorDialog.title"), e.getMessage());
		}

	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addProvidesField(composite);
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

	protected void performDefaults() {

	}

	public boolean performOk() {

		return true;
	}

}