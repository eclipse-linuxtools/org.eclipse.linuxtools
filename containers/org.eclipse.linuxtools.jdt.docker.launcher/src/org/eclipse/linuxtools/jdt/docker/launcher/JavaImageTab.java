/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.jdt.docker.launcher;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public class JavaImageTab extends AbstractLaunchConfigurationTab {

	private ComboViewer connCmb, imageCmb;
	private List directoryList;
	private Button addButton, removeButton;
	private IDockerConnection selectedConnection;
	private IDockerImage selectedImage;

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		Label connLbl = new Label(composite, SWT.NONE);
		connLbl.setText(Messages.ImageSelectionDialog_connection_label);
		connCmb = new ComboViewer(composite, SWT.READ_ONLY);
		connCmb.getCombo().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		connCmb.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				for (IDockerConnection conn : DockerConnectionManager.getInstance().getAllConnections()) {
					try {
						((DockerConnection)conn).open(false);
					} catch (DockerException e) {
					}
				}
				return DockerConnectionManager.getInstance().getAllConnections().stream().filter(c -> c.isOpen()).toArray(size -> new IDockerConnection[size]);
			}
		});
		connCmb.setInput("place_holder"); //$NON-NLS-1$

		Label imageLbl = new Label(composite, SWT.NONE);
		imageLbl.setText(Messages.ImageSelectionDialog_image_label);
		imageCmb = new ComboViewer(composite, SWT.READ_ONLY);
		imageCmb.getCombo().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		imageCmb.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				IDockerConnection conn = (IDockerConnection) inputElement;
				if (conn == null || conn.getImages() == null) {
					return new Object[0];
				} else {
					return conn.getImages().stream()
							.filter(i -> ! i.repoTags().get(0).equals("<none>:<none>")) //$NON-NLS-1$
							.toArray(size -> new IDockerImage[size]);
				}
			}
		});
		imageCmb.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IDockerImage img = (IDockerImage) element;
				return img.repoTags().get(0);
			}
		});
		imageCmb.setInput(null);

		connCmb.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = event.getStructuredSelection();
				IDockerConnection conn = (IDockerConnection) sel.getFirstElement();
				selectedConnection = conn;
				imageCmb.setInput(conn);
				updateLaunchConfigurationDialog();
			}
		});

		imageCmb.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = event.getStructuredSelection();
				IDockerImage img = (IDockerImage) sel.getFirstElement();
				selectedImage = img;
				updateLaunchConfigurationDialog();
			}
		});

		Group dirGroup = new Group(composite, SWT.NONE);
		dirGroup.setText(Messages.JavaImageTab_additional_dirs);
		dirGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		dirGroup.setLayout(new GridLayout(2, false));

		directoryList = new List(dirGroup, SWT.SINGLE | SWT.V_SCROLL);
		directoryList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		directoryList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeButton.setEnabled(true);
			}
		});

		addButton = createPushButton(dirGroup, Messages.JavaImageTab_button_add, null);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				String directory = dialog.open();
				if (directory != null && !listContains(directoryList, directory)) {
					directoryList.add(directory);
					updateLaunchConfigurationDialog();
				}
			}
		});

		removeButton = createPushButton(dirGroup, Messages.JavaImageTab_button_remove, null);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = directoryList.getSelectionIndex();
				if (i >= 0) {
					directoryList.remove(i);
					updateLaunchConfigurationDialog();
				}
				if (directoryList.getItemCount() == 0) {
					removeButton.setEnabled(false);
				}
			}
		});
		removeButton.setEnabled(false);

		setControl(composite);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(JavaLaunchConfigurationConstants.CONNECTION_URI, (String) null);
		configuration.setAttribute(JavaLaunchConfigurationConstants.IMAGE_ID, (String) null);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			DockerConnection conn;
			String connUri = configuration.getAttribute(JavaLaunchConfigurationConstants.CONNECTION_URI, (String) null);
			if (connUri != null) {
				connCmb.getCombo().setText(connUri);
				conn = (DockerConnection) DockerConnectionManager.getInstance().getConnectionByUri(connUri);
				String imageId = configuration.getAttribute(JavaLaunchConfigurationConstants.IMAGE_ID, (String) null);
				if (imageId != null && conn != null) {
					selectedConnection = conn;
					imageCmb.setInput(conn); // generate known images for given connection
					IDockerImage img = conn.getImage(imageId);
					if (img != null) {
						String repoTag = img.repoTags().get(0);
						imageCmb.getCombo().setText(repoTag);
						selectedImage = img;
					}
				}
			}

			java.util.List<String> dirs = configuration.getAttribute(JavaLaunchConfigurationConstants.DIRS, Arrays.asList(new String [0]));
			directoryList.setItems(dirs.toArray(new String [0]));
		} catch (CoreException e) {
		}

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (selectedConnection != null) {
			configuration.setAttribute(JavaLaunchConfigurationConstants.CONNECTION_URI, selectedConnection.getUri());
		}
		if (selectedImage != null) {
			configuration.setAttribute(JavaLaunchConfigurationConstants.IMAGE_ID, selectedImage.id());
		}

		if (directoryList.getItemCount() > 0) {
			String [] directories = directoryList.getItems();
			configuration.setAttribute(JavaLaunchConfigurationConstants.DIRS, Arrays.asList(directories));
		}
	}

	@Override
	public boolean canSave() {
		return selectedConnection != null && selectedImage != null;
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return canSave();
	}

	@Override
	public String getName() {
		return Messages.JavaImageTab_image_tab_title;
	}

	private static boolean listContains (List list, String target) {
		String [] items = list.getItems();
		for (String item : items) {
			if (item.equals(target)) {
				return true;
			}
		}
		return false;
	}

}
