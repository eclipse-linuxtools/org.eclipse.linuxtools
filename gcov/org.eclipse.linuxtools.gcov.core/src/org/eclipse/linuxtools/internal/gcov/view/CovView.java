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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.TreeColumnViewerFilter;
import org.eclipse.linuxtools.dataviewers.actions.STExportToCSVAction;
import org.eclipse.linuxtools.dataviewers.charts.actions.ChartAction;
import org.eclipse.linuxtools.internal.gcov.Activator;
import org.eclipse.linuxtools.internal.gcov.action.SwitchContentProviderAction;
import org.eclipse.linuxtools.internal.gcov.parser.CovManager;
import org.eclipse.linuxtools.internal.gcov.parser.SourceFile;
import org.eclipse.linuxtools.internal.gcov.view.annotatedsource.OpenSourceFileAction;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class CovView extends AbstractSTDataView {

    private String defaultCSVPath = "gcov.csv"; //$NON-NLS-1$

    private Label label;
    private Text fFilterText;
    private TreeColumnViewerFilter fViewerFilter;

    private Action folderAction;
    private Action fileAction;
    private Action functionAction;

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataView#createAbstractSTViewer
     * (org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected AbstractSTViewer createAbstractSTViewer(Composite parent) {
        return new CovViewer(parent);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataViewer#contributeToToolbar(org.eclipse.jface.action
     * .IToolBarManager)
     */
    @Override
    protected void contributeToToolbar(IToolBarManager manager) {
        manager.add(new Separator());
        manager.add(new Separator());
        manager.add(folderAction);
        manager.add(fileAction);
        manager.add(functionAction);
        manager.add(new Separator());
        manager.add(new ChartAction(getViewSite().getShell(), getSTViewer()));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.dataviewers.abstractview.AbstractSTDataViewer#createActions()
     */
    @Override
    protected void createActions() {
        super.createActions();
        folderAction = new SwitchContentProviderAction(Messages.CovView_sort_coverage_per_folder,
                "icons/directory_obj.gif", //$NON-NLS-1$
                getSTViewer().getViewer(), CovFolderContentProvider.sharedInstance);

        fileAction = new SwitchContentProviderAction(Messages.CovView_sort_coverage_per_file, "icons/c_file_obj.gif", //$NON-NLS-1$
                getSTViewer().getViewer(), CovFileContentProvider.sharedInstance);
        fileAction.setChecked(true);

        functionAction = new SwitchContentProviderAction(Messages.CovView_sort_coverage_per_function,
                "icons/function_obj.gif", //$NON-NLS-1$
                getSTViewer().getViewer(), CovFunctionContentProvider.sharedInstance);
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        GridLayout l = (GridLayout) parent.getLayout();
        l.horizontalSpacing = 0;
        l.verticalSpacing = 0;
        l.marginHeight = 0;
        l.marginWidth = 0;
        fViewerFilter = new TreeColumnViewerFilter((TreeViewer) getSTViewer().getViewer(),
                getSTViewer().getAllFields()[0], true);
        getSTViewer().getViewer().addFilter(fViewerFilter);
    }

    @Override
    protected void createTitle(Composite parent) {
        label = new Label(parent, SWT.WRAP);
        GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1);
        label.setLayoutData(data);

        fFilterText = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
        fFilterText.setMessage(Messages.CovView_type_filter_text);
        fFilterText.setToolTipText(Messages.CovView_filter_by_name);
        fFilterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fFilterText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String text = fFilterText.getText();
                fViewerFilter.setMatchingText(text);
            }
        });
    }

    public static void setCovViewTitle(CovView view, String title, String binaryPath, String timestamp) {
        String viewText = NLS.bind(Messages.CovView_view_title, new Object[] { title, binaryPath, timestamp });
        view.label.setText(viewText);
        view.label.getParent().layout(true);
    }

    public static void displayCovDetailedResult(String binaryPath, String gcdaFile) {
        try {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IFile binary = root.getFileForLocation(new Path(binaryPath));
            IProject project = null;
            if (binary != null)
                project = binary.getProject();

            // parse and process coverage data
            CovManager cvrgeMnger = new CovManager(binaryPath, project);
            List<String> gcdaPaths = new LinkedList<String>();
            gcdaPaths.add(gcdaFile);
            cvrgeMnger.processCovFiles(gcdaPaths, gcdaFile);
            // generate model for view
            cvrgeMnger.fillGcovView();

            for (SourceFile sf : cvrgeMnger.getSourceMap().values()) {
                OpenSourceFileAction.sharedInstance.openAnnotatedSourceFile(project, binary, sf, 0);
            }
        } catch (CoreException e) {
            reportError(e);
        } catch (IOException e) {
            reportError(e);
        }
    }

    public static CovView displayCovResults(String binaryPath, String gcda) {
        try {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IFile binary = root.getFileForLocation(new Path(binaryPath));
            IProject project = null;
            if (binary != null)
                project = binary.getProject();

            // parse and process coverage data
            CovManager cvrgeMnger = new CovManager(binaryPath, project);
            List<String> gcdaPaths = cvrgeMnger.getGCDALocations();
            cvrgeMnger.processCovFiles(gcdaPaths, gcda);
            // generate model for view
            cvrgeMnger.fillGcovView();
            // load an Eclipse view
            Date date = new Date(0);
            Date dateCandidate;
            for (String file : gcdaPaths) {
                dateCandidate = new Date(new File(file).lastModified());
                if (dateCandidate.after(date)) {
                    date = dateCandidate;
                }
            }
            String timestamp = DateFormat.getInstance().format(date);
            CovView cvrgeView = displayCovResults(cvrgeMnger, timestamp);
            return cvrgeView;
        } catch (InterruptedException e) {
            reportError(e);
        } catch (IOException e) {
            reportError(e);
        } catch (CoreException e) {
            reportError(e);
        }
        return null;
    }

    private static void reportError(Exception ex) {
        final String message = NLS.bind(Messages.CovView_error_message, ex.getMessage());
        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, message, ex);

        Activator.getDefault().getLog().log(status);
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                Shell s = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                MessageDialog.openError(s, Messages.CovView_parsing_error, message);
            }
        });
    }

    /**
     * Used by Test engine and OpenSerAction
     * @param cvrgeMnger
     */
    public static CovView displayCovResults(CovManager cvrgeMnger, String timestamp) throws PartInitException {
        // load an Eclipse view
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        CovView cvrgeView = (CovView) page.showView("org.eclipse.linuxtools.gcov.view"); //$NON-NLS-1$

        // view title
        CovView.setCovViewTitle(cvrgeView, Integer.toString((int) cvrgeMnger.getNbrPgmRuns()),
                cvrgeMnger.getBinaryPath(), timestamp);

        // load the controller
        cvrgeView.setInput(cvrgeMnger);
        CovViewer stviewer = (CovViewer) cvrgeView.getSTViewer();
        stviewer.getViewer().expandToLevel(2);
        return cvrgeView;
    }

    /*
     * (non-Javadoc)
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
     * @param defaultCSVPath
     *            the defaultCSVPath to set
     */
    public void setDefaultCSVPath(String defaultCSVPath) {
        this.defaultCSVPath = defaultCSVPath;
    }

}
