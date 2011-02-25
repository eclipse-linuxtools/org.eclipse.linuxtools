/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.ui.properties;

import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class AutotoolsPropertyPage extends AbstractPage {
	
	public AutotoolsPropertyPage() {
		super();
	}
	
	protected boolean isSingle() {
		return false;
	}

	protected Control createContents(Composite parent) {
		return super.createContents(parent);
	}
	
	protected boolean showsConfig() {
		return false;
	}
	
//	protected Combo fACVersionCombo;
//	protected Combo fAMVersionCombo;
//	
//	protected Button fCleanDelete;
//	protected Button fCleanMake;
//	protected Combo  fCleanMakeTarget;
//	protected Button fScannerMakeW;
//
//
//	public boolean isApplicable() {
//		IProject project = getProject();
//		if (project != null && !AutotoolsMakefileBuilder.hasTargetBuilder(project))
//			return false;
//		else
//			return super.isApplicable();
//	}
//	
//	protected Control createContents(Composite parent) {
//		
//		TabFolder folder= new TabFolder(parent, SWT.NONE);
//		folder.setLayout(new TabFolderLayout());	
//		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
//		
//		// Allow end-user to select which version of autoconf to use for hover help
//		// and syntax checking of macros.
//		TabItem item= new TabItem(folder, SWT.NONE);
//		item.setText(AutotoolsPropertyMessages.getString("Edit.name")); //$NON-NLS-1$
//		item.setControl(createEditTabContent(folder));
//					
//		// Build options.
//		item= new TabItem(folder, SWT.NONE);
//		item.setText(AutotoolsPropertyMessages.getString("Build.name")); //$NON-NLS-1$
//		item.setControl(createBuildTabContent(folder));
//					
//		initialize();
//		
//		applyDialogFont(folder);
//		return folder;
//	}
//
//	private Composite createEditTabContent(TabFolder folder) {
//		Composite composite= new Composite(folder, SWT.NULL);
//		// assume parent page uses griddata
//		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
//		composite.setLayoutData(gd);
//		GridLayout layout= new GridLayout();
//		layout.numColumns= 2;
//		//PixelConverter pc= new PixelConverter(composite);
//		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
//		composite.setLayout(layout);
//		
//		
//		/* check box for new editors */
//		fACVersionCombo= new Combo(composite, SWT.CHECK | SWT.DROP_DOWN | SWT.READ_ONLY);
//		fACVersionCombo.setItems(AutotoolsPropertyConstants.fACVersions);
//		fACVersionCombo.select(AutotoolsPropertyConstants.fACVersions.length - 1);
//		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
//		fACVersionCombo.setLayoutData(gd);
//		fACVersionCombo.addSelectionListener(new SelectionListener() {
//			public void widgetSelected(SelectionEvent e) {
//				int index = fACVersionCombo.getSelectionIndex();
//				try {
//					getProject().setPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION, fACVersionCombo.getItem(index));
//				} catch (CoreException ce) {
//					// FIXME: what can we do here?
//				}
//			}
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//				String version = AutotoolsPlugin.getDefault().getPreferenceStore().getString(AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION);
//				String[] items = fACVersionCombo.getItems();
//				// Try and find which list item matches the current preference stored and
//				// select it in the list.
//				int i;
//				for (i = 0; i < items.length; ++i) {
//					if (items[i].equals(version))
//						break;
//				}
//				if (i >= items.length)
//					i = items.length - 1;
//				fACVersionCombo.select(i);
//			}
//		});
//		
//		Label label= new Label(composite, SWT.LEFT);
//		label.setText(AutotoolsPropertyMessages.getString("ACEditor.autoconfVersion")); //$NON-NLS-1$
//		gd= new GridData();
//		gd.horizontalAlignment= GridData.BEGINNING;
//		label.setLayoutData(gd);
//		
//		/* check box for new editors */
//		fAMVersionCombo= new Combo(composite, SWT.CHECK | SWT.DROP_DOWN | SWT.READ_ONLY);
//		fAMVersionCombo.setItems(AutotoolsPropertyConstants.fAMVersions);
//		fAMVersionCombo.select(AutotoolsPropertyConstants.fAMVersions.length - 1);
//		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
//		fAMVersionCombo.setLayoutData(gd);
//		fAMVersionCombo.addSelectionListener(new SelectionListener() {
//			public void widgetSelected(SelectionEvent e) {
//				int index = fAMVersionCombo.getSelectionIndex(); 
//				try {
//					getProject().setPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION, fAMVersionCombo.getItem(index));
//				} catch (CoreException ce) {
//					// FIXME: what can we do here?
//				}
//			}
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//				String version = AutotoolsPlugin.getDefault().getPreferenceStore().getString(AutotoolsEditorPreferenceConstants.AUTOMAKE_VERSION);
//				String[] items = fAMVersionCombo.getItems();
//				// Try and find which list item matches the current preference stored and
//				// select it in the list.
//				int i;
//				for (i = 0; i < items.length; ++i) {
//					if (items[i].equals(version))
//						break;
//				}
//				if (i >= items.length)
//					i = items.length - 1;
//				fAMVersionCombo.select(i);
//			}
//		});
//		
//		Label label2= new Label(composite, SWT.LEFT);
//		label2.setText(AutotoolsPropertyMessages.getString("ACEditor.automakeVersion")); //$NON-NLS-1$
//		gd= new GridData();
//		gd.horizontalAlignment= GridData.BEGINNING;
//		label2.setLayoutData(gd);
//
//		return composite;
//	}
//	
//	private Composite createBuildTabContent(TabFolder folder) {
//		Composite composite= new Composite(folder, SWT.NULL);
//		// assume parent page uses griddata
//		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
//		composite.setLayoutData(gd);
//		GridLayout layout= new GridLayout();
//		layout.numColumns= 2;
//		//PixelConverter pc= new PixelConverter(composite);
//		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
//		composite.setLayout(layout);
//		
//		
//		Group g = new Group(composite, SWT.SHADOW_ETCHED_IN);
//		g.setText(AutotoolsPropertyMessages.getString("CleanBehavior.title"));
//		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
//		gd.horizontalSpan = 2;
//		g.setLayoutData(gd);
//		layout= new GridLayout();
//		layout.numColumns= 2;
//		g.setLayout(layout);
//		
//		fCleanDelete = new Button(g, SWT.RADIO);
//		fCleanDelete.setText(AutotoolsPropertyMessages.getString("CleanDelete.label"));
//		gd= new GridData();
//		gd.horizontalAlignment= GridData.BEGINNING;
//		gd.horizontalSpan = 2;
//		fCleanDelete.setLayoutData(gd);
//		fCleanMake = new Button(g, SWT.RADIO);
//		fCleanMake.setText(AutotoolsPropertyMessages.getString("CleanMake.label"));
//		gd= new GridData();
//		gd.horizontalAlignment= GridData.BEGINNING;
//		gd.horizontalSpan = 2;
//		fCleanMake.setLayoutData(gd);
//		
//		Label label = new Label(g, SWT.LEFT);
//		label.setText(AutotoolsPropertyMessages.getString("CleanMakeTarget.label"));
//		gd= new GridData();
//		gd.horizontalAlignment= GridData.BEGINNING;
//		label.setLayoutData(gd);
//		
//		fCleanMakeTarget = new Combo(g, SWT.SIMPLE);
//		fCleanMakeTarget.setText(AutotoolsPropertyMessages.getString("CleanMakeTarget.default"));
//		fCleanMakeTarget.setTextLimit(40);
//		fCleanMakeTarget.setVisibleItemCount(0);
//		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
//		fCleanMakeTarget.setLayoutData(gd);
//		
//		fCleanDelete.addSelectionListener(new SelectionListener() {
//			public void widgetSelected(SelectionEvent e) {
//				fCleanMake.setSelection(false);
//				fCleanDelete.setSelection(true);
//				fCleanMakeTarget.setEnabled(false);
//				try {
//					getProject().setPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE, "true");
//				} catch (CoreException ce) {
//					// FIXME: what can we do here?
//				}
//			}
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//				fCleanDelete.setSelection(false);
//				fCleanMake.setSelection(true);
//				fCleanMakeTarget.setEnabled(true);
//				try {
//					getProject().setPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE, "false");
//					getProject().setPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET, "distclean");
//				} catch (CoreException ce) {
//					// FIXME: what can we do here?
//				}
//			}
//		});
//		
//		fCleanMake.addSelectionListener(new SelectionListener() {
//			public void widgetSelected(SelectionEvent e) {
//				fCleanDelete.setSelection(false);
//				fCleanMake.setSelection(true);
//				fCleanMakeTarget.setEnabled(true);
//				try {
//					getProject().setPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE, "false");
//				} catch (CoreException ce) {
//					// FIXME: what can we do here?
//				}
//			}
//
//			public void widgetDefaultSelected(SelectionEvent e) {
////				fCleanDelete.setSelection(false);
////				fCleanMake.setSelection(true);
////				try {
////					getProject().setPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE, "false");
////					getProject().setPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET, "distclean");
////				} catch (CoreException ce) {
////					// FIXME: what can we do here?
////				}
//			}
//		});
//		
//		fCleanMakeTarget.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				try {
//					getProject().setPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET, fCleanMakeTarget.getText());
//				} catch (CoreException ce) {
//					// FIXME: what can we do here?
//				}
//			}
//		});
//		
//		fScannerMakeW = new Button(composite, SWT.LEFT | SWT.CHECK); 
//		fScannerMakeW.setText(AutotoolsPropertyMessages.getString("ScannerMakeW.label"));
//		fScannerMakeW.setToolTipText(AutotoolsPropertyMessages.getString("ScannerMakeW.tooltip"));
//		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
//		fScannerMakeW.setLayoutData(gd);
//		fScannerMakeW.addSelectionListener(new SelectionListener() {
//			public void widgetSelected(SelectionEvent e) {
//				try {
//					getProject().setPersistentProperty(AutotoolsPropertyConstants.SCANNER_USE_MAKE_W, "true");
//				} catch (CoreException ce) {
//					// FIXME: what can we do here?
//				}
//			}
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//				fScannerMakeW.setSelection(false);
//				try {
//					// This is only a temporary work-around so we remove the property rather than set it false.
//					getProject().setPersistentProperty(AutotoolsPropertyConstants.SCANNER_USE_MAKE_W, null);
//				} catch (CoreException ce) {
//					// FIXME: what can we do here?
//				}
//			}
//		});
//		
//		return composite;
//	}
//	
//	void initializeACVersion() {
//		String version = "";
//		try {
//			version = getProject().getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION);
//			if (version == null)
//				version = AutotoolsPlugin.getDefault().getPreferenceStore().getString(AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION);
//		} catch (CoreException e) {
//			// do nothing
//		}
//		String[] items = fACVersionCombo.getItems();
//		// Try and find which list item matches the current preference stored and
//		// select it in the list.
//		int i;
//		for (i = 0; i < items.length; ++i) {
//			if (items[i].equals(version))
//				break;
//		}
//		if (i >= items.length)
//			i = items.length - 1;
//		fACVersionCombo.select(i);
//	}
//	
//	void initializeAMVersion() {
//		String version = "";
//		try {
//			version = getProject().getPersistentProperty(AutotoolsPropertyConstants.AUTOMAKE_VERSION);
//			if (version == null)
//				version = AutotoolsPlugin.getDefault().getPreferenceStore().getString(AutotoolsEditorPreferenceConstants.AUTOMAKE_VERSION);
//		} catch (CoreException e) {
//			// do nothing
//		}
//		String[] items = fAMVersionCombo.getItems();
//		// Try and find which list item matches the current preference stored and
//		// select it in the list.
//		int i;
//		for (i = 0; i < items.length; ++i) {
//			if (items[i].equals(version))
//				break;
//		}
//		if (i >= items.length)
//			i = items.length - 1;
//		fAMVersionCombo.select(i);
//	}
//	
//	void initializeBuild() {
//		String cleanDelete = null;
//		String cleanMakeTarget = null;
//		try {
//			cleanDelete = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE);
//			cleanMakeTarget = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET);
//		} catch (CoreException e) {
//			// do nothing
//		}
//		
//		if (cleanMakeTarget == null) {
//			cleanMakeTarget = AutotoolsPropertyMessages.getString("CleanMakeTarget.default"); // $NON-NLS-1$
//			fCleanMakeTarget.setText(cleanMakeTarget);
//		}
//		
//		if (cleanDelete == null || cleanDelete.equals("false")) {
//			fCleanDelete.setSelection(false);
//			fCleanMake.setSelection(true);
//			fCleanMakeTarget.setEnabled(true);
//		} else {
//			fCleanDelete.setSelection(true);
//			fCleanMake.setSelection(false);
//			fCleanMakeTarget.setEnabled(false);
//		}
//	}
//	
//	private void initialize() {
//		initializeACVersion();
//		initializeAMVersion();
//		initializeBuild();
//	}

}
