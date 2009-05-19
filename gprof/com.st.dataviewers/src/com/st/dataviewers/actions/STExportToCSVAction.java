/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package com.st.dataviewers.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.st.dataviewers.abstractviewers.AbstractSTViewer;
import com.st.dataviewers.abstractviewers.STDataViewersCSVExporter;
import com.st.dataviewers.abstractviewers.STDataViewersImages;
import com.st.dataviewers.abstractviewers.STDataViewersMessages;
import com.st.dataviewers.dialogs.STDataViewersExportToCSVDialog;

/**
 * This action export the STViewers data to CSV format file
 *
 */
public class STExportToCSVAction extends Action {
	
	private final AbstractSTViewer stViewer;
	
	private STDataViewersCSVExporter exporter;
	
	/**
	 * Constructor
	 * @param stViewer the stViewer to export
	 */
	public STExportToCSVAction(AbstractSTViewer stViewer) {
		super(STDataViewersMessages.exportToCSVAction_title);
		Image img = STDataViewersImages.getImage(STDataViewersImages.IMG_EXPORT); 
		super.setImageDescriptor(ImageDescriptor.createFromImage(img));
		
		this.stViewer = stViewer;
		this.exporter = new STDataViewersCSVExporter(stViewer);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		STDataViewersExportToCSVDialog dialog =
			new STDataViewersExportToCSVDialog(stViewer.getViewer().getControl().getShell(), exporter);
		if (dialog.open() == Dialog.OK) {
			Job exportToCSVJob =
				new Job("Export to CSV") {
					public IStatus run(IProgressMonitor monitor) {
						exporter.export(monitor);
						return Status.OK_STATUS;
					}
				};
			exportToCSVJob.setUser(true);
			exportToCSVJob.schedule();
		}
	}
	
	/**
	 * 
	 * @return exporter
	 */
	public STDataViewersCSVExporter getExporter(){
		return exporter;
	}
}
