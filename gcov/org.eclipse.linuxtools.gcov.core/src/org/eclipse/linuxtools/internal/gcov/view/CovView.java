/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersImages;
import org.eclipse.linuxtools.dataviewers.actions.STExportToCSVAction;
import org.eclipse.linuxtools.dataviewers.charts.actions.ChartAction;
import org.eclipse.linuxtools.gcov.Activator;
import org.eclipse.linuxtools.internal.gcov.action.SwitchContentProviderAction;
import org.eclipse.linuxtools.internal.gcov.parser.CovManager;
import org.eclipse.linuxtools.internal.gcov.parser.SourceFile;
import org.eclipse.linuxtools.internal.gcov.view.annotatedsource.OpenSourceFileAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


/**
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 *
 */
public class CovView extends AbstractSTDataView {

	private String defaultCSVPath = "gcov.csv";
	
	private Label label;

	private Action folderAction;
	private Action fileAction;
	private Action functionAction;

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView#createAbstractSTViewer
	 * (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected AbstractSTViewer createAbstractSTViewer(Composite parent) {
		return new CovViewer(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataViewer#contributeToToolbar(org.eclipse.jface.action.IToolBarManager)
	 */
	@Override
	protected void contributeToToolbar(IToolBarManager manager) {
		super.contributeToToolbar(manager);
		manager.add(new Separator());
		manager.add(new Separator());
		manager.add(folderAction);
		manager.add(fileAction);
		manager.add(functionAction);
		manager.add(new Separator());
		manager.add(new ChartAction(getViewSite().getShell(), getSTViewer()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataViewer#createActions()
	 */
	@Override
	protected void createActions() {
		STDataViewersImages.getImageDescriptor(""); // workaround a bug
		super.createActions();
		folderAction = new SwitchContentProviderAction(
				"Sort coverage per folder", 
				"icons/directory_obj.gif",
				getSTViewer().getViewer(),
				CovFolderContentProvider.sharedInstance);

		fileAction = new SwitchContentProviderAction(
				"Sort coverage per file",
				"icons/c_file_obj.gif", 
				getSTViewer().getViewer(),
				CovFileContentProvider.sharedInstance);
		fileAction.setChecked(true);

		functionAction = new SwitchContentProviderAction(
				"Sort coverage per function", 
				"icons/function_obj.gif",
				getSTViewer().getViewer(),
				CovFunctionContentProvider.sharedInstance);
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		STDataViewersImages.getImageDescriptor(""); // workaround a bug
		super.createPartControl(parent);
		GridLayout l = (GridLayout) parent.getLayout();
		l.horizontalSpacing = 0;
		l.verticalSpacing = 0;
		l.marginHeight = 0;
		l.marginWidth = 0;
	}

	@Override
	protected void createTitle(Composite parent) {
		label = new Label(parent, SWT.WRAP);
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1);
		label.setLayoutData(data);
	}

	public static void setCovViewTitle(CovView view, String title,
			String binaryPath) {
		view.label.setText(" \n program runs = " + title
				+ " \n program file : " + binaryPath + "\n ");
		view.label.getParent().layout(true);
	}


	public static void displayCovDetailedResult(String binaryPath, String gcdaFile) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile binary = root.getFileForLocation(new Path(binaryPath));
			IProject project = null;
			if (binary != null) project = binary.getProject();

			// parse and process coverage data
			CovManager cvrgeMnger = new CovManager(binaryPath, project);
			List<String> gcdaPaths = new LinkedList<String>();
			gcdaPaths.add(gcdaFile);
			cvrgeMnger.processCovFiles(gcdaPaths, gcdaFile);
			// generate model for view
			cvrgeMnger.fillGcovView();
			
			for (SourceFile sf : cvrgeMnger.getSourceMap().values()) {
				OpenSourceFileAction.sharedInstance.openAnnotatedSourceFile(project, 
						binary, sf, 0);
			}
		} catch (Exception _) {
			reportError(_);
		}
	}
	
	public static CovView displayCovResults(String binaryPath, String gcda) {
		try {

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile binary = root.getFileForLocation(new Path(binaryPath));
			IProject project = null;
			if (binary != null) project = binary.getProject();

			// parse and process coverage data
			CovManager cvrgeMnger = new CovManager(binaryPath, project);
			List<String> gcdaPaths = cvrgeMnger.getGCDALocations();
			cvrgeMnger.processCovFiles(gcdaPaths, gcda);
			// generate model for view
			cvrgeMnger.fillGcovView();
			//load an Eclipse view
			CovView cvrgeView = displayCovResults(cvrgeMnger);
			return cvrgeView;
		} catch (Exception _) {
			reportError(_);
		}
		return null;
	}
	
	private static void reportError(Exception _) {
		final String message = "An error has occured when parsing "
				+ "the coverage data files :\n" + _.getMessage();
			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					IStatus.ERROR, message, _);

			Activator.getDefault().getLog().log(status);
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					Shell s = PlatformUI.getWorkbench().getDisplay().getActiveShell();
					MessageDialog.openError(s, "Gcov Parsing Error", message);
				}
			});
	}
	
	/**
	 * Used by Test engine and OpenSerAction
	 * @param cvrgeMnger
	 */
	public static CovView displayCovResults(CovManager cvrgeMnger) throws PartInitException {
			//load an Eclipse view
			IWorkbenchWindow window = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			CovView cvrgeView = (CovView) page.showView("org.eclipse.linuxtools.gcov.view");

			//view title 
			CovView.setCovViewTitle(cvrgeView, Integer
					.toString((int) cvrgeMnger.getNbrPgmRuns()), cvrgeMnger
					.getBinaryPath());

			// load the controller
			cvrgeView.setInput(cvrgeMnger);
			CovViewer stviewer = (CovViewer) cvrgeView.getSTViewer();
			stviewer.getViewer().expandToLevel(2);
			return cvrgeView;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView#createExportToCSVAction()
	 */
	@Override
	protected IAction createExportToCSVAction() {
		IAction action = new STExportToCSVAction(this.getSTViewer()) {
			@Override
			public void run() {
				Object o = getSTViewer().getInput();
				if (o instanceof CovManager) {
					getExporter().setFilePath(getDefaultCSVPath());
				}
				super.run();
			}
			
		};
		return action;
	}

	/**
	 * @return the defaultCSVPath
	 */
	public String getDefaultCSVPath() {
		return defaultCSVPath;
	}

	/**
	 * @param defaultCSVPath the defaultCSVPath to set
	 */
	public void setDefaultCSVPath(String defaultCSVPath) {
		this.defaultCSVPath = defaultCSVPath;
	}

}
