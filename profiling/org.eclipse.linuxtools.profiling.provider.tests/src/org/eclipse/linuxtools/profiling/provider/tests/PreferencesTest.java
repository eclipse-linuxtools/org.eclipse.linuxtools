/*******************************************************************************
 * Copyright (c) 2012, 2024 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.provider.tests;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit5.SWTBotJunit5Extension;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.FrameworkUtil;

@ExtendWith(SWTBotJunit5Extension.class)
public class PreferencesTest extends AbstractTest{
    private static final String PROJ_NAME = "fibTest"; //$NON-NLS-1$
    private static final String STUB_TOOLTIP = "tooltip test"; //$NON-NLS-1$
    private static final String STUB_LABEL = "Test Tool [description test]"; //$NON-NLS-1$
    private static final String PROFILING_PREFS_CATEGORY = "Timing"; //$NON-NLS-1$
    private static final String PROFILING_PREFS_TYPE = "timing"; //$NON-NLS-1$
    private static final String[][] PROFILING_PREFS_INFO = {
            { "Coverage", "coverage" }, { "Memory", "memory" },{ "Timing", "timing" } };  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    private static class NodeAvailableAndSelect extends DefaultCondition {

        private SWTBotTree tree;
        private String[] nodes;

        /**
         * Wait for a tree node (with a known parent) to become visible, and select it
         * when it does. Note that this wait condition should only be used after having
         * made an attempt to reveal the node.
         * @param tree The SWTBotTree that contains the node to select.
         * @param nodes A list of the names of each node containing the target node.
         */
        NodeAvailableAndSelect(SWTBotTree tree, String ...nodes) {
            this.tree = tree;
            this.nodes = new String[nodes.length];
            System.arraycopy(nodes, 0, this.nodes, 0, nodes.length);
        }

        @Override
        public boolean test() {
            try {
                SWTBotTreeItem currentNode = tree.getTreeItem(nodes[0]);
                for (int i = 1, n = nodes.length; i < n; i++) {
                    currentNode = currentNode.getNode(nodes[i]);
                }
                currentNode.select();
                return true;
            } catch (WidgetNotFoundException e) {
                return false;
            }
        }

        @Override
        public String getFailureMessage() {
            return "Timed out waiting for " + nodes[nodes.length - 1]; //$NON-NLS-1$
        }
    }

    @BeforeAll
    public static void setUpWorkbench() throws Exception {
        // Set up is based from from GcovTest{c,CPP}.

        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        try {
            bot.viewByTitle("Welcome").close(); //$NON-NLS-1$
        } catch (WidgetNotFoundException e) {
            // ignore
        }

        // Set C/C++ perspective.
        bot.perspectiveByLabel("C/C++").activate(); //$NON-NLS-1$
        bot.sleep(500);
        for (SWTBotShell sh : bot.shells()) {
            if (sh.getText().startsWith("C/C++")) { //$NON-NLS-1$
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
    }

    @Test
    public void testDefaultPreferences() {
        for (String[] preferenceInfo : PROFILING_PREFS_INFO) {
            checkDefaultPreference(preferenceInfo[0], preferenceInfo[1]);
        }
    }

    @Test
    public void testPreferencesPage() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();

        // Set default tool for "timing" profiling.
        checkDefaultPreference(PROFILING_PREFS_CATEGORY, PROFILING_PREFS_TYPE);

        // Open preferences shell.
        SWTBotMenu windowsMenu = bot.menu("Window"); //$NON-NLS-1$
        windowsMenu.menu("Preferences").click(); //$NON-NLS-1$
        SWTBotShell shell = bot.shell("Preferences"); //$NON-NLS-1$
        shell.activate();

        // Go to "Profiling Categories" preferences page.
        bot.text().setText(PROFILING_PREFS_CATEGORY);
        bot.waitUntil(new NodeAvailableAndSelect(bot.tree(),
                "C/C++", "Profiling", "Categories", PROFILING_PREFS_CATEGORY)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Get name of default tool to deselect.
        String defaultToolId = ProviderFramework.getProviderIdToRun(null, PROFILING_PREFS_TYPE);
        String defaultToolName = ProviderFramework.getToolInformationFromId(defaultToolId , "name"); //$NON-NLS-1$

        // Workaround for BZ #344484.
        deselectSelectionByName(defaultToolName, bot);

        // Assert specified tool to select is what we expect and select it.
        SWTBotRadio stubRadio = bot.radio(STUB_LABEL);
        assertNotNull(stubRadio);
        assertEquals(STUB_TOOLTIP,stubRadio.getToolTipText());
        stubRadio.click();

        bot.button("Apply").click(); //$NON-NLS-1$
        bot.button("Apply and Close").click(); //$NON-NLS-1$
    }

    @Disabled
    @Test
    public void testProfileProject() throws Exception {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), PROJ_NAME);
        try {
            testProfileProjectActions(bot);
        } finally {
            deleteProject(proj);
        }
    }

    private void testProfileProjectActions(SWTWorkbenchBot bot) throws Exception {
        testPreferencesPage();

        // Focus on project explorer view.
        SWTBotView projectExplorer = bot.viewByTitle("Project Explorer"); //$NON-NLS-1$
        projectExplorer.bot().tree().select(PROJ_NAME);
        final Shell shellWidget = bot.activeShell().widget;

        // Open profiling configurations dialog
		UIThreadRunnable.asyncExec(() -> {
			DebugUITools.openLaunchConfigurationDialogOnGroup(shellWidget,
					(StructuredSelection) PlatformUI.getWorkbench().getWorkbenchWindows()[0].getSelectionService()
							.getSelection(),
					"org.eclipse.debug.ui.launchGroup.profilee"); //$NON-NLS-1$
		});
        SWTBotShell shell = bot.shell("Profiling Tools Configurations"); //$NON-NLS-1$
        shell.activate();

        // Create new profiling configuration
        SWTBotTree profilingConfigs = bot.tree();
        SWTBotTree perfNode = profilingConfigs.select("Profile Timing"); //$NON-NLS-1$
        perfNode.contextMenu("New").click(); //$NON-NLS-1$
        bot.button("Profile").click(); //$NON-NLS-1$
        bot.waitUntil(Conditions.shellCloses(shell));

        // Assert that the expected tool is running.
        SWTBotShell profileShell = bot.shell("Successful profile launch").activate(); //$NON-NLS-1$
        assertNotNull(profileShell);

        bot.button("Apply and Close").click(); //$NON-NLS-1$
        bot.waitUntil(shellCloses(profileShell));
    }

    private static void checkDefaultPreference(String preferenceCategory, String profilingType){
        SWTWorkbenchBot bot = new SWTWorkbenchBot();

        // Open preferences shell.
        SWTBotMenu windowsMenu = bot.menu("Window"); //$NON-NLS-1$
        windowsMenu.menu("Preferences").click(); //$NON-NLS-1$
        SWTBotShell shell = bot.shell("Preferences"); //$NON-NLS-1$
        shell.activate();

        // Go to specified tree item in "Profiling Categories" preferences page.
        bot.text().setText(preferenceCategory);
        bot.waitUntil(new NodeAvailableAndSelect(bot.tree(),
                "C/C++", "Profiling", "Categories", preferenceCategory)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Restore defaults.
        bot.button("Restore Defaults").click(); //$NON-NLS-1$
        bot.button("Apply").click(); //$NON-NLS-1$

        // Get information for default tool.
        String defaultToolId = ProviderFramework.getProviderIdToRun(null, profilingType);
        String defaultToolName = ProviderFramework.getToolInformationFromId(defaultToolId , "name"); //$NON-NLS-1$
        String defaultToolInfo = ProviderFramework.getToolInformationFromId(defaultToolId , "information"); //$NON-NLS-1$
        String defaultToolDescription = ProviderFramework.getToolInformationFromId(defaultToolId , "description"); //$NON-NLS-1$
        String defaultToolLabel = defaultToolName + " [" + defaultToolDescription + "]"; //$NON-NLS-1$ //$NON-NLS-2$

        // Assert default radio is as expected.
        SWTBotRadio defaultRadio = bot.radio(defaultToolLabel);
        assertNotNull(defaultRadio);
        assertEquals(defaultToolInfo, defaultRadio.getToolTipText());

        bot.button("Apply").click(); //$NON-NLS-1$
        bot.button("Apply and Close").click(); //$NON-NLS-1$
    }

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return null;
    }

    @Override
    protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
    }

    /**
     * Deselect radio button with partial label name.
     *
     * Adapted workaround for BZ #344484:
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=344484#c1
     *
     * @param name partial label of radio button to deselect.
     */
    private static void deselectSelectionByName(final String name, final SWTWorkbenchBot bot) {
		UIThreadRunnable.syncExec(() -> {
			@SuppressWarnings("unchecked")
			Matcher<Button> matcher = allOf(widgetOfType(Button.class), withStyle(SWT.RADIO, "SWT.RADIO"), //$NON-NLS-1$
					withRegex(name + ".*")); //$NON-NLS-1$

			Button b = bot.widget(matcher); // the current selection
			b.setSelection(false);
		});
    }
}