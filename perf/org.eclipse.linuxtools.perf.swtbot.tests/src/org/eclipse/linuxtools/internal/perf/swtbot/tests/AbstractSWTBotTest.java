/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.swtbot.tests;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForWidget;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.widgetIsEnabled;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.perf.IPerfData;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.FrameworkUtil;

/**
 * Abstract SWTBot test for Perf views, sub-classes must implement the abstract
 * methods to specify the test view and logic.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractSWTBotTest extends AbstractTest {
    private static final String PROJ_NAME = "fibTest";
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTBotView projectExplorer;

    @BeforeClass
    public static void setUpWorkbench() throws Exception {
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        try {
            bot.viewByTitle("Welcome").close();
            // hide Subclipse Usage stats popup if present/installed
            bot.shell("Subclipse Usage").activate();
            bot.button("Cancel").click();
        } catch (WidgetNotFoundException e) {
            // ignore
        }

        // Set C/C++ perspective.
        bot.perspectiveByLabel("C/C++").activate();
        bot.sleep(500);
        for (SWTBotShell sh : bot.shells()) {
            if (sh.getText().startsWith("C/C++")) {
                sh.activate();
                bot.sleep(500);
                break;
            }
        }

        // Turn off automatic building by default to avoid timing issues
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = workspace.getDescription();
        boolean isAutoBuilding = desc.isAutoBuilding();
        if (isAutoBuilding) {
            desc.setAutoBuilding(false);
            workspace.setDescription(desc);
        }

        projectExplorer = bot.viewByTitle("Project Explorer");
    }

    @AfterClass
    public static void resetExplorerState() {
        exitProjectFolder(new SWTWorkbenchBot());
    }

    @Test
    public void runPerfViewTest() throws Exception {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();

        /*
         * - Method returns when the build is complete -
         * AbstractTest#createProjectAndBuild builds a single executable binary
         * under "Binaries".
         */
        proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), PROJ_NAME);
        try {
            runPerfViewTestActions(bot);
        } catch (Exception e) {
            throw e;
        } finally {
            deleteProject(proj);
        }
    }

    private void runPerfViewTestActions(SWTWorkbenchBot bot) throws Exception {
        projectExplorer.bot().tree().select(PROJ_NAME);
        final Shell shellWidget = bot.activeShell().widget;

        // Open profiling configurations dialog
        UIThreadRunnable.asyncExec(new VoidResult() {
            @Override
            public void run() {
                DebugUITools.openLaunchConfigurationDialogOnGroup(shellWidget,
                        (StructuredSelection) PlatformUI.getWorkbench().getWorkbenchWindows()[0].
                        getSelectionService().getSelection(), "org.eclipse.debug.ui.launchGroup.profilee");
            }
        });
        bot.shell("Profiling Tools Configurations").activate();

        // Create new Perf configuration
        SWTBotTree profilingConfigs = bot.tree();
        SWTBotTree perfNode = profilingConfigs.select("Profile with Perf");
        perfNode.contextMenu("New").click();

        // Activate options tab
        bot.cTabItem("Perf Options").activate();

        setPerfOptions(bot);

        bot.button("Apply").click();

        if (PerfCore.checkPerfInPath(null)) {
            bot.button("Profile").click();

        } else {
            bot.button("Close").click();
            openStubView();
        }

        testPerfView();
    }

    /**
     * Compare The selected items in PROJ_NAME with each other
     * @param The name of a tree item to select
     * @param The name of a second tree item to select
     */
    public void compareWithEachOther (String first, String second) {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        enterProjectFolder(bot);

        // Refresh and Select
        SWTBotTree treeBot = projectExplorer.bot().tree();
        treeBot.contextMenu("Refresh").click();
        treeBot.select(new String [] {first, second});

        // Workaround for context menu on multiple selections
        click(ContextMenuHelper.contextMenu(treeBot, "Compare With", "Each Other"));
        exitProjectFolder(bot);
    }

    /**
     * Enter the project folder so as to avoid expanding trees later
     */
    private static SWTBotView enterProjectFolder(SWTWorkbenchBot bot) {
        projectExplorer.bot().tree().select(PROJ_NAME).
            contextMenu("Go Into").click();
        bot.waitUntil(waitForWidget(WidgetMatcherFactory.withText(
                PROJ_NAME), projectExplorer.getWidget()));
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

    /**
     * Click specfied menu item.
     *
     * @param menuItem
     *            menu item to click
     */
    public static void click(final MenuItem menuItem) {
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

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return null;
    }

    @Override
    protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
    }

    /**
     * Set Perf launch options.
     *
     * @param bot SWTWorkbenchBot bot focused in Perf options tab.
     */
    protected abstract void setPerfOptions(SWTWorkbenchBot bot);

    /**
     * Open view with fake data. To be used when perf is not installed.
     */
    protected abstract void openStubView();

    /**
     * Test perf view.
     */
    protected abstract void testPerfView();

    /**
     * Stub data for use in case the Perf tool is not installed.
     */
    protected static class StubPerfData implements IPerfData {
        @Override
        public String getPerfData() {
            return "stub_perf_data";
        }

        @Override
        public String getTitle() {
            return "stub_perf_title";
        }

    }
}
