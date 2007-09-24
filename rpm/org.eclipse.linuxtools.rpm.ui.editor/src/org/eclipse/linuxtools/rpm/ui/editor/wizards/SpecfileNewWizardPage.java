/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.wizards;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileLog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class SpecfileNewWizardPage extends WizardPage {

	private static final String NAME = "package_name";

	private static final String VERSION = "1.0";

	private static final String SUMMARY = "Summary of the package";

	private static final String GROUP = "Amusements/Games";

	private static final String LICENSE = "GPL";

	private static final String URL = "http://";

	private static final String SOURCE0 = "archive_name-%{version}";

	private Text projectText;

	private Text nameText;

	private Text versionText;

	private Text summaryText;

	private Combo groupCombo;

	private Text licenseText;

	private Text URLText;

	private Text source0Text;

	private GridData gd;

	private Combo templateCombo;

	private ISelection selection;

	private String selectedTemplate = "minimal";

	private String content;

	/**
	 * Constructor for SpecfileNewWizardPage.
	 * 
	 * @param pageName
	 */
	public SpecfileNewWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("New specfile based on a template");
		setDescription("This wizard creates a new specfile based on a selected template.");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		// Project
		Label label = new Label(container, SWT.NULL);
		label.setText("&Project:");
		projectText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projectText.setLayoutData(gd);
		projectText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		Button button = new Button(container, SWT.PUSH);
		button.setText("Select a project...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});

		// Template to use
		label = new Label(container, SWT.NULL);
		label.setText("Select a Template:");
		templateCombo = new Combo(container, SWT.NULL);
		try {
			populateTemplateCombo(templateCombo);
		} catch (CoreException e2) {
			SpecfileLog.logError(e2);
		}
		// empty label for the last row.
		label = new Label(container, SWT.NULL);
		templateCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				selectedTemplate = ((Combo) e.getSource()).getText();
				InputStream inputStream = runRpmdevNewSpec(selectedTemplate);
				LineNumberReader reader = new LineNumberReader(
						new InputStreamReader(inputStream));
				String line;
				try {
					content = "";
					setDefaultValues();
					while ((line = reader.readLine()) != null) {
						if (line.startsWith("Name:")) {
							setTemplateTagValue(nameText, line);
						}
						if (line.startsWith("Version:")) {
							setTemplateTagValue(versionText, line);
						}
						if (line.startsWith("Summary:")) {
							setTemplateTagValue(summaryText, line);
						}
						if (line.startsWith("Group:")) {
							String[] items = line.split(":", 2);
							String value = items[1].trim();
							if (!value.equals(""))
								groupCombo.setText(value);
						}
						if (line.startsWith("License:")) {
							setTemplateTagValue(licenseText, line);
						}
						if (line.startsWith("URL:")) {
							setTemplateTagValue(URLText, line);
						}
						if (line.startsWith("Source0:")) {
							setTemplateTagValue(source0Text, line);
						}
						content += line + "\n";
					}
				} catch (IOException e1) {
					SpecfileLog.logError(e1);
				}
			}
		});

		// Package Name
		nameText = setTextItem(container, "&Name:");

		// Package Version
		versionText = setTextItem(container, "&Version:");

		// Package Summary
		summaryText = setTextItem(container, "&Summary:");

		// Package Group
		label = new Label(container, SWT.NULL);
		label.setText("&Group:");
		groupCombo = new Combo(container, SWT.NULL);
		try {
			populateGroupCombo(groupCombo);
		} catch (CoreException e1) {
			SpecfileLog.logError(e1);
		}
		// empty label for the last row.
		label = new Label(container, SWT.NULL);

		// Package License
		licenseText = setTextItem(container, "&License:");

		// Package URL
		URLText = setTextItem(container, "&URL:");

		// Package Source0
		source0Text = setTextItem(container, "Source&0:");

		initialize();
		dialogChanged();
		setControl(container);
	}

	private Text setTextItem(Composite container, String textLabel) {
		Label label = new Label(container, SWT.NULL);
		label.setText(textLabel);
		Text text = new Text(container, SWT.BORDER | SWT.SINGLE);
		text.setLayoutData(gd);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		// empty label for the last row.
		label = new Label(container, SWT.NULL);
		return text;
	}

	private void setTemplateTagValue(Text text, String line) {
		String[] items = line.split(":", 2);
		String value = items[1].trim();
		if (!value.equals("")) {
			text.setText(value);
		}
	}

	public String getProjectName() {
		return projectText.getText();
	}

	public String getFileName() {
		return nameText.getText() + ".spec";
	}

	public String getSelectedTemplate() {
		return selectedTemplate;
	}

	public String getContent() {
		InputStream inputStream = runRpmdevNewSpec(selectedTemplate);
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(
				inputStream));
		String line;
		try {
			content = "";
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("Name:")) {
					line = "Name:" + "           " + nameText.getText();
				}
				if (line.startsWith("Version:")) {
					line = "Version:" + "        " + versionText.getText();
				}
				if (line.startsWith("Summary:")) {
					line = "Summary:" + "        " + summaryText.getText();
				}
				if (line.startsWith("Group:")) {
					line = "Group:" + "          " + groupCombo.getText();
				}
				if (line.startsWith("License:")) {
					line = "License:" + "        " + licenseText.getText();
				}
				if (line.startsWith("URL:")) {
					line = "URL:" + "            " + URLText.getText();
				}
				if (line.startsWith("Source0:")) {
					line = "Source0:" + "        " + source0Text.getText();
				}
				content += line + "\n";
			}
		} catch (IOException e1) {
			SpecfileLog.logError(e1);
		}
		return content;
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				projectText.setText(container.getFullPath().toString());
			}
		}
		setDefaultValues();
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */
	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				projectText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getProjectName()));
		String fileName = getFileName();
		if (getProjectName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("The Project must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("Spec file name must be specified");
			return;
		}

		/*
		 * Current RPM doc content (4.4.2):
		 * Names must not include whitespace and may include a hyphen '-'
		 * (unlike version and releasetags). Names should not include any
		 * numeric operators ('<', '>','=') as future versions of rpm may need
		 * to reserve characters other than '-'.
		 * 
		 */
		String packageName = nameText.getText();
		if (packageName.indexOf(" ") != -1 || packageName.indexOf("<") != -1
				|| packageName.indexOf(">") != -1 || packageName.indexOf("=") != -1){
			updateStatus("The Name tag must not include whitespace and "
					+ "should not include any numeric operators ('<', '>','=')");
			return;
		}

		if (versionText.getText().contains("-")) {
			updateStatus("Please, no dashes in the version!");
			return;
		}

		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	private void setDefaultValues() {
		nameText.setText(NAME);
		versionText.setText(VERSION);
		summaryText.setText(SUMMARY);
		groupCombo.setText(GROUP);
		licenseText.setText(LICENSE);
		URLText.setText(URL);
		source0Text.setText(SOURCE0);
	}

	private void populateTemplateCombo(Combo templateCombo) throws CoreException {
		// get a list of all files in a directory
		File dir = new File("/etc/rpmdevtools");
		String[] files = dir.list();
		if (dir.exists()) {
			String templateCSV = "";
			for (int i = 0; i < files.length; i++) {
				if (files[i].startsWith("spectemplate-"))
					templateCSV += files[i].split("-", 2)[1].replaceAll("\\.spec",
							"")
							+ ",";
			}
			String[] templates = templateCSV.split(",");
			for (int i = 0; i < templates.length; i++) {
				templateCombo.add(templates[i]);
			}
			templateCombo.setText(selectedTemplate);			
		} else {
			throwCoreException("/etc/rpmdevtools directory was not found");
		}
	}

	private void populateGroupCombo(Combo groupsCombo) throws CoreException {
		// FIXME: Can we assume that all distros place 
		// documentations files in the below path?
		String docDir = "/usr/share/doc/";
		File dir = new File(docDir);
		if (dir.exists()) {
			String files[] = dir.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith("rpm-");
				}
			});
			try {
				// Normally only one version of RPM is installed on a system,
				// so we take the first one here.
				LineNumberReader reader = new LineNumberReader(new FileReader(
						docDir + files[0] + "/GROUPS"));
				String line;
				while ((line = reader.readLine()) != null) {
					groupsCombo.add(line);
				}
			} catch (IOException e) {
				SpecfileLog.logError(e);
			}			
		} else {
			throwCoreException("/usr/share/doc directory was not found" );
		}

	}

	private BufferedInputStream runRpmdevNewSpec(String template) {
		BufferedInputStream in = null;
		// Here we assuming that the rpmdevtools package is installed.
		String[] cmd = { "rpmdev-newspec", "-o", "-", "-t", template };
		try {
			Process child = Runtime.getRuntime().exec(cmd);
			in = new BufferedInputStream(child.getInputStream());
		} catch (IOException e) {
			// FIXME: rpmdev-newspec is not in the system $PATH, what should we do here?.
			SpecfileLog.logError(e);
		}
		return in;
	}
	
	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR,
				"org.eclipse.linuxtools.rpm.ui.editor", IStatus.OK, message,
				null);
		throw new CoreException(status);
	}

}