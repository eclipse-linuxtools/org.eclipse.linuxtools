/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.ui.properties;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AutotoolsToolsPropertyPage extends AbstractCPropertyTab {

	public static final String DEFAULT_ACLOCAL = "aclocal"; // $NON-NLS-1$
	public static final String DEFAULT_AUTOMAKE = "automake"; // $NON-NLS-1$
	public static final String DEFAULT_AUTOCONF = "autoconf"; // $NON-NLS-1$
	
	protected Text fAclocalPath;
	protected Text fAutomakePath;
	protected Text fAutoconfPath;
	private IProject project;

	
	private IProject getProject() {
		IConfiguration c = ManagedBuildManager.getConfigurationForDescription(getResDesc().getConfiguration());
		return (IProject)c.getManagedProject().getOwner();
	}
	
	public boolean canBeVisible() {
		return true;
	}

	public void createControls(Composite parent) {
		// TODO Auto-generated method stub
		super.createControls(parent);
		Composite composite= usercomp;
		
		// assume parent page uses griddata
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gd);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		//PixelConverter pc= new PixelConverter(composite);
		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
		composite.setLayout(layout);
		
		project = getProject();
		
		Label label= new Label(composite, SWT.LEFT);
		label.setText(AutotoolsPropertyMessages.getString("Autotools.aclocalPath")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);
		
		/* text window for aclocal path */
		fAclocalPath = new Text(composite, SWT.BORDER | SWT.SINGLE);
		fAclocalPath.setToolTipText(AutotoolsPropertyMessages.getString("Autotools.aclocalPath.tooltip")); // $NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		fAclocalPath.setLayoutData(gd);
		
		Label label2= new Label(composite, SWT.LEFT);
		label2.setText(AutotoolsPropertyMessages.getString("Autotools.automakePath")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label2.setLayoutData(gd);
		
		/* text window for automake path */
		fAutomakePath = new Text(composite, SWT.BORDER | SWT.SINGLE);
		fAutomakePath.setToolTipText(AutotoolsPropertyMessages.getString("Autotools.automakePath.tooltip")); // $NON-NLS-1#
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		fAutomakePath.setLayoutData(gd);
		
		Label label3 = new Label(composite, SWT.LEFT);
		label3.setText(AutotoolsPropertyMessages.getString("Autotools.autoconfPath")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label3.setLayoutData(gd);
		
		/* text window for autoconf path */
		fAutoconfPath = new Text(composite, SWT.BORDER | SWT.SINGLE);
		fAutoconfPath.setToolTipText(AutotoolsPropertyMessages.getString("Autotools.autoconfPath.tooltip")); // $NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		fAutoconfPath.setLayoutData(gd);

		initialize();
	}

	public void performOK() {
		String aclocalPath = null;
		String automakePath = null;
		String autoconfPath = null;
		boolean changed = false;
		
		try {
			aclocalPath = project.getPersistentProperty(AutotoolsPropertyConstants.ACLOCAL_TOOL);
		} catch (CoreException e1) {
			aclocalPath = DEFAULT_ACLOCAL;
		}

		String newAclocalPath = fAclocalPath.getText().trim();
		if (aclocalPath == null || !newAclocalPath.equals(aclocalPath)) {
			changed = true;
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.ACLOCAL_TOOL, newAclocalPath);
			} catch (CoreException e1) {
				// Not much we can do at this point
			}
		}
		
		try {
			automakePath = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_TOOL);
		} catch (CoreException e1) {
			automakePath = DEFAULT_AUTOMAKE;
		}

		String newAutomakePath = fAutomakePath.getText().trim();
		if (automakePath == null || !newAutomakePath.equals(automakePath)) {
			changed = true;
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_TOOL, newAutomakePath);
			} catch (CoreException e2) {
				// Not much we can do at this point
			}
		}
		
		try {
			autoconfPath = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_TOOL);
		} catch (CoreException e1) {
			autoconfPath = DEFAULT_AUTOCONF;
		}

		String newAutoconfPath = fAutoconfPath.getText().trim();
		if (autoconfPath == null || !newAutoconfPath.equals(autoconfPath)) {
			changed = true;
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_TOOL, newAutoconfPath);
			} catch (CoreException e2) {
				// Not much we can do at this point
			}
		}
	}
	
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		performOK();
	}
	
	public void performDefaults() {
		// For default tool settings, simply default the base tool names
		fAclocalPath.setText(DEFAULT_ACLOCAL);
		fAutomakePath.setText(DEFAULT_AUTOMAKE);
		fAutoconfPath.setText(DEFAULT_AUTOCONF);
	}
	
	public void updateData(ICResourceDescription cfgd) {
		// what to do here?
	}
	
	public void updateButtons() {
		// what to do here?
	}

	public void setVisible (boolean b) {
		super.setVisible(b);
	}
	
	private void initialize() {
		String aclocalPath = null;
		String automakePath = null;
		String autoconfPath = null;
		
		try {
			aclocalPath = project.getPersistentProperty(AutotoolsPropertyConstants.ACLOCAL_TOOL);
		} catch (CoreException e1) {
			// do nothing
		}
		
		if (aclocalPath == null)
			aclocalPath = DEFAULT_ACLOCAL;
		
		fAclocalPath.setText(aclocalPath);
		
		try {
			automakePath = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_TOOL);
		} catch (CoreException e1) {
			// do nothing
		}
		
		if (automakePath == null)
			automakePath = DEFAULT_AUTOMAKE;
		
		fAutomakePath.setText(automakePath);
		
		try {
			autoconfPath = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_TOOL);
		} catch (CoreException e1) {
			// do nothing
		}
		
		if (autoconfPath == null)
			autoconfPath = DEFAULT_AUTOCONF;
		
		fAutoconfPath.setText(autoconfPath);
	}
	
}
