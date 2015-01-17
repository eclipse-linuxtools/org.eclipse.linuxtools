/*******************************************************************************
 * Copyright (c) 2011 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.actions.ContextualLaunchAction;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.dataviewers.actions.STExportToCSVAction;
import org.eclipse.linuxtools.internal.gcov.action.OpenGCAction;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

public abstract class GcovTest extends AbstractTest {
    private static IProject project = null;
    private static boolean isCppProject;
    private static IWorkbenchWindow window;
    private static Display display;

    private static TreeSet<String> gcovFiles;

    abstract protected String getTestProjectName();
    abstract protected String getBinName();
    abstract protected boolean getTestProducedReference();

    @BeforeClass
    public static void init() {
        display = Display.getDefault();
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    IWorkbenchPart part = window.getActivePage().getActivePart();
                    if (part.getTitle().equals("Welcome")) {
                        part.dispose();
                    }
                    PlatformUI.getWorkbench().showPerspective(CUIPlugin.ID_CPERSPECTIVE, window);
                } catch (WorkbenchException e) {
                    Assert.fail("Couldn't open C/C++ perspective.");
                }
            }
        });
    }

    @Before
    public void setUp() throws Exception {
        if (project == null) {
            project = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), getTestProjectName()).getProject();
            isCppProject = project.getNature(CCProjectNature.CC_NATURE_ID) != null;

            gcovFiles = new TreeSet<>();
            for (IResource r : project.members()) {
                if (r.getType() == IResource.FILE && r.exists()) {
                    String fileName = r.getName();
                    if (fileName.endsWith(".gcda") || fileName.endsWith(".gcno")) {
                        gcovFiles.add(fileName);
                    }
                }
            }
        }
    }

    @After
    public void cleanUp() {
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                Shell[] shells = Display.getCurrent().getShells();
                for (final Shell shell : shells) {
                    String shellTitle = shell.getText();
                    if (!shellTitle.isEmpty() && !shellTitle.startsWith("Quick Access")
                            && shell.getParent() != null) {
                        shell.close();
                    }
                }
            }
        });
    }

    @AfterClass
    public static void finalCleanUp() {
        try {
            project.delete(true, null);
        } catch (CoreException e) {
            Assert.fail("Project deletion failed");
        } finally {
            project = null;
        }
    }

    @Test
    public void testOpenGcovFileDetails() {
        for (String string : gcovFiles) {
            testGcovFileDetails(string);
        }
    }

    private void testGcovFileDetails(final String filename) {
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                openGcovResult(project.getFile(filename), false);

                final IWorkbenchPage page = window.getActivePage();
                final IEditorPart editorPart = page.getActiveEditor();
                final IFile openedFile = project.getFile(editorPart.getEditorInput().getName());
                final IFile targetFile = project.getFile(
                        new Path(filename).removeFileExtension().addFileExtension(
                                isCppProject ? "cpp" : "c"));
                if (!targetFile.equals(openedFile)) {
                    System.err.println("WARNING: editor for " + targetFile
                            + " is not in focus.");
                    for (IEditorReference ref : page.getEditorReferences()) {
                        if (targetFile.equals(project.getFile(ref.getName()))) {
                            return;
                        }
                    }
                    Assert.fail("Editor for file " + targetFile + " was not opened,"
                            + " instead opened " + openedFile + ".");
                }
            }
        });
    }

    @Test
    public void testOpenGcovSummary() {
        for (String string : gcovFiles) {
            testGcovSummary(string, getBinName(), getTestProducedReference());
        }
    }

    private void testGcovSummary(final String filename, String binName,
            final boolean testProducedReference) {
        openGcovResult(project.getFile(filename), true);
        IViewPart vp = window.getActivePage().findView("org.eclipse.linuxtools.gcov.view");

        // No IDs on toolbar items, so explicitly check each one for tooltip texts
        List<String> sortTypes = new ArrayList<>(Arrays.asList("function", "file", "folder"));
        IContributionItem[] items = vp.getViewSite().getActionBars().getToolBarManager().getItems();
        STExportToCSVAction csvAction = null;
        for (IContributionItem item : items) {
            if (item instanceof ActionContributionItem && ((ActionContributionItem) item).getAction() instanceof STExportToCSVAction) {
                csvAction = (STExportToCSVAction) ((ActionContributionItem) item).getAction();
            }
        }
        Assert.assertNotNull("CSV-Export toolbar button does not exist.", csvAction);

        for (IContributionItem item : items) {
            if (item instanceof ActionContributionItem) {
                final IAction action = ((ActionContributionItem) item).getAction();
                for (int i = 0, n = sortTypes.size(); i < n; i++) {
                    String sortType = sortTypes.get(i);
                    if (action.getText().equals("Sort coverage per " + sortType)) {
                        dumpCSV(action, csvAction, sortType, testProducedReference);
                        if (sortTypes.size() == 1) {
                            return;
                        }
                        sortTypes.remove(i);
                        break;
                    }
                }
            }
        }
    }

    private void dumpCSV(final IAction sortAction, final STExportToCSVAction csvAction, String type, boolean testProducedReference) {
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                sortAction.run();
            }
        });

        String s = project.getLocation() + "/" + type + "-dump.csv";
        new File(s).delete();
        csvAction.getExporter().setFilePath(s);
        csvAction.export();

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            if (Job.getJobManager().find(STExportToCSVAction.EXPORT_TO_CSV_JOB_FAMILY).length == 0) {
                break;
            }
        }

        if (testProducedReference) {
            String ref = STJunitUtils.getAbsolutePath(FrameworkUtil.getBundle(GcovTest.class).getSymbolicName(), "csv/" + project.getName() + "/" + type + ".csv");
            STJunitUtils.compareIgnoreEOL(project.getLocation() + "/" + type + "-dump.csv", ref, false);
        }
    }

    private void openGcovResult(final IFile file, final boolean isCompleteCoverageResultWanted) {
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                new OpenGCAction().autoOpen(file.getLocation(), isCompleteCoverageResultWanted);
            }
        });
    }

    private class ProfileContextualLaunchAction extends ContextualLaunchAction {
        public ProfileContextualLaunchAction(Menu menu) {
            super("linuxtools");
            fillMenu(menu);
        }
    }

    @Test
    public void testGcovSummaryByLaunch() {
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    CommonNavigator vc = (CommonNavigator) window.getActivePage().showView(ProjectExplorer.VIEW_ID);
                    vc.selectReveal(new StructuredSelection(project.getFile(getBinName())));
                    Menu menu = new MenuManager().createContextMenu(vc.getCommonViewer().getControl());
                    new ProfileContextualLaunchAction(menu);
                    for (MenuItem item : menu.getItems()) {
                        if (item.getText().endsWith("Profile Code Coverage")) {
                            ((ActionContributionItem) item.getData()).getAction().run();
                            break;
                        }
                    }
                } catch (PartInitException e) {
                    Assert.fail("Cannot show Project Explorer.");
                }
                try {
                    window.getActivePage().showView("org.eclipse.linuxtools.gcov.view");
                } catch (PartInitException e) {
                    Assert.fail("Cannot show GCov View.");
                }
            }
        });

        // Wait for the build job to finish (note: DebugUIPlugin doesn't put launch jobs in a family)
        Job[] jobs = Job.getJobManager().find(null);
        for (Job job : jobs) {
            if (job.getName().contains("Gcov")) {
                try {
                    job.join();
                } catch (InterruptedException e) {}
                break;
            }
        }
    }

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return null;
    }

    @Override
    protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
    }
}
