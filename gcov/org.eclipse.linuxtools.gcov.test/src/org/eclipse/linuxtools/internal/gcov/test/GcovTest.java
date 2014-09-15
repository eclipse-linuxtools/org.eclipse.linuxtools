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

import static org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper.contextMenu;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForWidget;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.widgetIsEnabled;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.dataviewers.actions.STExportToCSVAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.Matcher;
import org.junit.After;
import org.osgi.framework.FrameworkUtil;

public abstract class GcovTest {
    private static final String PROJECT_EXPLORER = "Project Explorer";
    private static SWTBotView projectExplorer;
    private static SWTBotShell mainShell;

    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot bot;
    private static String testProjectName;
    private static String testProjectType;

    public static SWTWorkbenchBot init(String projectName, String projectType)
            throws Exception {
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        bot = new SWTWorkbenchBot();
        testProjectName = projectName;
        testProjectType = projectType;
        bot.captureScreenshot(projectName + ".beforeClass.1.jpg");
        try {
            bot.viewByTitle("Welcome").close();
            // hide Subclipse Usage stats popup if present/installed
            bot.shell("Subclipse Usage").activate();
            bot.button("Cancel").click();
        } catch (WidgetNotFoundException e) {
            // ignore
        }

        bot.perspectiveByLabel("C/C++").activate();
        for (SWTBotShell sh : bot.shells()) {
            if (sh.getText().startsWith("C/C++")) {
                sh.activate();
                mainShell = sh;
                break;
            }
        }

        bot.captureScreenshot(projectName + ".beforeClass.2.jpg");
        // Turn off automatic building by default
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = workspace.getDescription();
        boolean isAutoBuilding = desc.isAutoBuilding();
        if (isAutoBuilding) {
            desc.setAutoBuilding(false);
            workspace.setDescription(desc);
        }


        // define & repopulate project explorer
        projectExplorer = bot.viewByTitle(PROJECT_EXPLORER);
        createProject();
        populateProject();
        compileProject();
        return bot;
    }

    @After
    public void cleanUp() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotShell[] shells = bot.shells();
        for (final SWTBotShell shell : shells) {
            String shellTitle = shell.getText();
            if (shellTitle.length() > 0
                    && !shellTitle.startsWith("SystemTap IDE")
                    && !shellTitle.startsWith("Quick Access")) {
                UIThreadRunnable.syncExec(new VoidResult() {
                    @Override
                    public void run() {
                        if (shell.widget.getParent() != null) {
                            shell.close();
                        }
                    }
                });
            }
        }
        bot.closeAllEditors();
    }

    public static void cleanup(SWTWorkbenchBot bot) {
        // clear project explorer
        exitProjectFolder(bot);
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(testProjectName);
        try {
            project.delete(true, new ProgressMonitor());
        } catch (CoreException e) {
            fail("Project deletion failed");
        }
    }

    /**
     * Enter the project folder so as to avoid expanding trees later
     */
    private static SWTBotView enterProjectFolder(SWTWorkbenchBot bot, String projectName) {
        projectExplorer.bot().tree().select(projectName).
            contextMenu("Go Into").click();
        bot.waitUntil(waitForWidget(WidgetMatcherFactory.withText(
                projectName), projectExplorer.getWidget()));
        return projectExplorer;
    }

    /**
     * Exit from the project tree.
     */
    private static void exitProjectFolder(SWTWorkbenchBot bot) {
        try {
            SWTBotToolbarButton forwardButton = projectExplorer.toolbarPushButton("Forward");
            projectExplorer.toolbarPushButton("Back to Workspace").click();
            bot.waitUntil(widgetIsEnabled(forwardButton));
        } catch (WidgetNotFoundException e) {
            // Already exited from project folder
        }
    }

    public static void createProject() {
        mainShell.activate();
        SWTBotMenu fileMenu = bot.menu("File");
        SWTBotMenu newMenu = fileMenu.menu("New");
        SWTBotMenu projectMenu = newMenu.menu(testProjectType);
        projectMenu.click();

        SWTBotShell shell = bot.shell(testProjectType);
        shell.activate();

        bot.tree().expandNode("Makefile project").select("Empty Project");
        bot.textWithLabel("Project name:").setText(testProjectName);
        bot.table().select("Linux GCC");

        bot.button("Next >").click();
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    public static void populateProject() throws Exception {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(testProjectName);
        try (InputStream is = FileLocator.openStream(
                FrameworkUtil.getBundle(GcovTest.class), new Path("resource/"
                        + testProjectName + "/content"), false);
                LineNumberReader lnr = new LineNumberReader(
                        new InputStreamReader(is))) {
            String filename;
            while (null != (filename = lnr.readLine())) {
                final ProgressMonitor pm = new ProgressMonitor();
                final IFile ifile = project.getFile(filename);
                InputStream fis = FileLocator.openStream(FrameworkUtil
                        .getBundle(GcovTest.class), new Path("resource/"
                        + testProjectName + "/" + filename), false);
                ifile.create(fis, true, pm);
                bot.waitUntil(new DefaultCondition() {

                    @Override
                    public boolean test() {
                        return pm.isDone();
                    }

                    @Override
                    public String getFailureMessage() {
                        return ifile + " not yet created after 6000ms";
                    }
                }, 6000);
            }
        }
    }

    public static void compileProject() {
        SWTBotTree treeBot = projectExplorer.bot().tree();
        treeBot.setFocus();
        treeBot = treeBot.select(testProjectName);
        bot.waitUntil(Conditions.treeHasRows(treeBot, 1));
        mainShell.activate();
        SWTBotMenu menu = bot.menu("Build Project");
        menu.click();
        bot.waitUntil(new JobsRunning(ResourcesPlugin.FAMILY_MANUAL_BUILD), 30000);
    }

    private static TreeSet<String> getGcovFiles(SWTWorkbenchBot bot, String projectName) throws Exception {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        TreeSet<String> ret = new TreeSet<>();
        for (IResource r : project.members()) {
            if (r.getType() == IResource.FILE && r.exists()) {
                if (r.getName().endsWith(".gcda") || r.getName().endsWith(".gcno")) {
                    ret.add(r.getFullPath().toOSString());
                }
            }
        }
        return ret;
    }

    private static void testGcovSummary(SWTWorkbenchBot bot, String projectName, String filename, String binName,
            boolean testProducedReference) throws Exception {
        IPath filePath = new Path(filename);
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
        String binPath = file.getProject().getFile(binName).getLocation().toOSString();

        openResource(bot, file.getName());
        Matcher<Shell> withText = withText("Gcov - Open coverage results...");
        bot.waitUntil(Conditions.waitForShell(withText));

        SWTBotShell shell = bot.shell("Gcov - Open coverage results...");
        shell.activate();
        bot.textInGroup("Binary File", 0).setText(binPath);
        bot.button("OK").click();
        bot.waitUntil(Conditions.shellCloses(shell));

        SWTBotView botView = bot.viewByTitle("gcov");
        // The following cannot be tested on 4.2 because the SWTBot implementation of toolbarButton()
        // is broken there because it relies PartPane having a method getPane() which is no longer true.
        botView.toolbarButton("Sort coverage per function").click();
        dumpCSV(botView, "function", testProducedReference);
        botView.toolbarButton("Sort coverage per file").click();
        dumpCSV(botView, "file", testProducedReference);
        botView.toolbarButton("Sort coverage per folder").click();
        dumpCSV(botView, "folder", testProducedReference);
        botView.close();
    }

    private static void openResource(SWTWorkbenchBot bot, String fileName) {
        mainShell.activate();
        bot.menu("Navigate").menu("Open Resource...").click();
        SWTBotShell shell = bot.shell("Open Resource").activate();
        bot.text().setText(fileName);
        bot.button("Open").click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    private static void testGcovFileDetails(SWTWorkbenchBot bot, String projectName, String filename, String binName) throws Exception {
        IPath filePath = new Path(filename);
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
        String binPath = file.getProject().getFile(binName).getLocation().toOSString();

        openResource(bot, file.getName());
        Matcher<Shell> withText = withText("Gcov - Open coverage results...");
        bot.waitUntil(Conditions.waitForShell(withText));

        SWTBotShell shell = bot.shell("Gcov - Open coverage results...");
        shell.activate();
        bot.textInGroup("Binary File", 0).setText(binPath);
        SWTBotRadio button = bot.radioInGroup("Coverage result", 0);
        button.click();
        bot.button("OK").click();
        bot.waitUntil(Conditions.shellCloses(shell));

        SWTBotEditor editor = bot.activeEditor();
        SWTBotEclipseEditor edt = editor.toTextEditor(); /* just to verify that the correct file was found */
        edt.close();
    }

    private static void testGcovLaunchSummary(SWTWorkbenchBot bot, String projectName, String binName) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        String binLocation = project.getFile(binName).getLocation().toOSString();
        IPath binPath = new Path(binLocation);
        IFile binFile = ResourcesPlugin.getWorkspace().getRoot().getFile(binPath);

        SWTBot viewBot = projectExplorer.bot();

        SWTBotTree treeBot = viewBot.tree();
        treeBot.setFocus();
        // We need to select the binary, but in the tree, it may have additional info appended to the
        // name such as [x86_64/le].  So, we look at all nodes of the project and look for the one that
        // starts with our binary file name.  We can then select the node.
        enterProjectFolder(bot, projectName);
        bot.waitUntil(Conditions.waitForWidget(withText(projectName), projectExplorer.getWidget()));

        SWTBotTreeItem[] nodes = treeBot.getAllItems();
        String binNodeName = binFile.getName();
        for (SWTBotTreeItem node : nodes) {
            if (node.getText().startsWith(binNodeName)) {
                node.select();
                break;
            }
        }
        assertNotEquals(treeBot.selectionCount(), 0);
        String menuItem = "Profiling Tools";
        String subMenuItem = "1 Profile Code Coverage";
        click(contextMenu(treeBot, menuItem, subMenuItem));

        final boolean result[] = new boolean[1];
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage()
                            .showView("org.eclipse.linuxtools.gcov.view");
                    result[0] = true;
                } catch (PartInitException e) {
                    result[0] = false;
                }
            }
        });
        assertTrue(result[0]);
        SWTBotView botView = bot.viewByTitle("gcov");

        botView.close();
    }

    private static void dumpCSV(SWTBotView botView, String type, boolean testProducedReference) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(testProjectName);
        botView.toolbarButton("Export to CSV").click();
        SWTBotShell shell = bot.shell("Export to CSV");
        shell.activate();
        String s = project.getLocation() + "/" + type + "-dump.csv";
        new File(s).delete();
        bot.text().setText(s);
        bot.button("OK").click();
        bot.waitUntil(new JobsRunning(STExportToCSVAction.EXPORT_TO_CSV_JOB_FAMILY), 5000);
        if (testProducedReference) {
            String ref = STJunitUtils.getAbsolutePath(FrameworkUtil.getBundle(GcovTest.class).getSymbolicName(), "resource/" + testProjectName + "/" + type + ".csv");
            STJunitUtils.compareIgnoreEOL(project.getLocation() + "/" + type + "-dump.csv", ref, false);
        }
    }

    public static void openGcovFileDetails(SWTWorkbenchBot bot, String projectName) throws Exception {
        openGcovFileDetails(bot, projectName, "a.out");
    }

    public static void openGcovSummary(SWTWorkbenchBot bot, String projectName, boolean testProducedReference)
            throws Exception {
        openGcovSummary(bot, projectName, "a.out", testProducedReference);
    }

    public static void openGcovSummary(SWTWorkbenchBot bot, String projectName, String binName,
            boolean testProducedReference) throws Exception {
        TreeSet<String> ts = getGcovFiles(bot, projectName);
        for (String string : ts) {
            testGcovSummary(bot, projectName, string, binName, testProducedReference);
        }
    }

    public static void openGcovFileDetails(SWTWorkbenchBot bot,
            String projectName, String binName) throws Exception {
        TreeSet<String> ts = getGcovFiles(bot, projectName);
        for (String string : ts) {
            testGcovFileDetails(bot, projectName, string, binName);
        }
    }

    public static void openGcovSummaryByLaunch(SWTWorkbenchBot bot,
            String projectName) {
        testGcovLaunchSummary(bot, projectName, "a.out");
    }

    /**
     * Click on the specified MenuItem.
     * @param menuItem MenuItem item to click
     */
    private static void click(final MenuItem menuItem) {
        final Event event = new Event();
        event.time = (int) System.currentTimeMillis();
        event.widget = menuItem;
        event.display = menuItem.getDisplay();
        event.type = SWT.Selection;

        UIThreadRunnable.asyncExec(menuItem.getDisplay(), new VoidResult() {
            @Override
            public void run() {
                menuItem.notifyListeners(SWT.Selection, event);
            }
        });
    }
}
