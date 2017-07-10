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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

public class ImageSelectionDialog extends SelectionDialog {

	private IDockerConnection connection;
	private IDockerImage image;

	public ImageSelectionDialog () {
		this(Display.getDefault().getActiveShell());
	}

	protected ImageSelectionDialog(Shell parentShell) {
		super(parentShell);
		setTitle(Messages.ImageSelectionDialog_title);
	}

	@Override
    public void create() {
        super.create();
        if (getInitialElementSelections().isEmpty()) {
            getOkButton().setEnabled(false);
        }

        Shell shell = getShell();
        shell.setSize(shell.getSize().x + 100, shell.getSize().y);
    }

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		Label connLbl = new Label(composite, SWT.NONE);
		connLbl.setText(Messages.ImageSelectionDialog_connection_label);
		ComboViewer connCmb = new ComboViewer(composite, SWT.READ_ONLY);
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
		ComboViewer imageCmb = new ComboViewer(composite, SWT.READ_ONLY);
		imageCmb.getCombo().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		imageCmb.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				IDockerConnection conn = (IDockerConnection) inputElement;
				return conn.getImages().stream()
						.filter(i -> ! i.repoTags().get(0).equals("<none>:<none>")) //$NON-NLS-1$
						.toArray(size -> new IDockerImage[size]);
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
				connection = conn;
				imageCmb.setInput(conn);
			}
		});

		imageCmb.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = event.getStructuredSelection();
				IDockerImage img = (IDockerImage) sel.getFirstElement();
				image = img;
				getOkButton().setEnabled(true);
			}
		});

		return composite;
	}

	public IDockerConnection getConnection () {
		return connection;
	}

	public IDockerImage getImage () {
		return image;
	}

}
