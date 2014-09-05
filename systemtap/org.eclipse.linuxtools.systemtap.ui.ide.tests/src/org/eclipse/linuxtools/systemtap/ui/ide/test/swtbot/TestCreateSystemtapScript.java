/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial implementation
 *******************************************************************************/


package org.eclipse.linuxtools.systemtap.ui.ide.test.swtbot;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers.ImportDataSetHandler;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher.Messages;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TreeSettings;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.FuncparamNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.FunctionNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbeNodeData;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata.ProbevarNodeData;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.FilteredRowDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowEntry;
import org.eclipse.linuxtools.systemtap.graphing.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphing.ui.charts.AbstractChartBuilder;
import org.eclipse.linuxtools.systemtap.graphing.ui.wizards.graph.GraphFactory;
import org.eclipse.linuxtools.systemtap.structures.TreeDefinitionNode;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotScale;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotSlider;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.Range;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestCreateSystemtapScript {
    private static final String SYSTEMTAP_PROJECT_NAME = "SystemtapTest";

    private static SWTWorkbenchBot bot;
    private static SWTBotView projectExplorer;
    private static SWTBotShell mainShell;

    // dummy probe/function information
    private static File probeDef, funcDef;
    private static final String probeCategoryFull = "Static Probes";
    private static final String probeCategoryEmpty = "Probe Aliases";
    private static final String probeGroup = "probegroup";
    private static final String probeSingleWithoutDef = "testprobe";
    private static final String funcNodeName = "ftest";

    private static class NodeAvailableAndSelect extends DefaultCondition {

        private SWTBotTree tree;
        private String parent;
        private String node;

        /**
         * Wait for a tree node (with a known parent) to become visible, and select it
         * when it does. Note that this wait condition should only be used after having
         * made an attempt to reveal the node.
         * @param tree The SWTBotTree that contains the node to select.
         * @param parent The text of the parent node that contains the node to select.
         * @param node The text of the node to select.
         */
        NodeAvailableAndSelect(SWTBotTree tree, String parent, String node) {
            this.tree = tree;
            this.node = node;
            this.parent = parent;
        }

        @Override
        public boolean test() {
            try {
                SWTBotTreeItem parentNode = tree.getTreeItem(parent);
                parentNode.getNode(node).select();
                return true;
            } catch (WidgetNotFoundException e) {
                return false;
            }
        }

        @Override
        public String getFailureMessage() {
            return "Timed out waiting for " + node; //$NON-NLS-1$
        }
    }

    private static class TreeItemPopulated extends DefaultCondition {

        private SWTBotTreeItem parent;

        TreeItemPopulated(SWTBotTreeItem parent) {
            this.parent = parent;
        }

        @Override
        public boolean test() {
            return this.parent.getItems().length > 0;
        }

        @Override
        public String getFailureMessage() {
            return "Timed out waiting for tree to populate.";
        }
    }

    private static class TableHasUpdated extends DefaultCondition {

        final private int expectedRows;
        final private boolean exact;
        final private SWTBotTable table;
        /**
         * Wait for the provided GraphSelectorEditor table to be fully updated.
         * @param graphTable Which graph set table to watch for updates.
         * @param expectedRows How many entries/rows are expected to be in the table when it's fully updated.
         * @param exact Set this to <code>true</code> if the number of graph columns should exactly match the
         * expected amount, or <code>false</code> if it may be greater than the expected amount.
         */
        public TableHasUpdated(SWTBotTable graphTable, int expectedRows, boolean exact) {
            this.expectedRows = expectedRows;
            this.exact = exact;
            table = graphTable;
        }
        @Override
        public boolean test() {
            if (!exact) {
                return table.rowCount() >= expectedRows;
            } else {
                return table.rowCount() == expectedRows;
            }
        }

        @Override
        public String getFailureMessage() {
            return "Timed out waiting for data table to update";
        }
    }

    private static class ChartHasUpdated extends DefaultCondition {

        private Chart chart;
        int oldCount;
        int expectedCount;
        /**
         * Wait for the provided chart to become updated.
         * @param chart The chart to watch for an update.
         * @param expectedCount The expected number of series points. Set to -1 to instead
         * check for when there are more points than there were at the beginning.
         */
        public ChartHasUpdated(Chart chart, int expectedCount) {
            this.chart = chart;
            ISeries[] seriesSet = chart.getSeriesSet().getSeries();
            this.oldCount = seriesSet.length > 0 ? seriesSet[0].getXSeries().length : 0;
            this.expectedCount = expectedCount;
        }
        @Override
        public boolean test() {
            ISeries[] seriesSet = chart.getSeriesSet().getSeries();
            int newCount = seriesSet.length > 0 ? seriesSet[0].getXSeries().length : 0;
            return expectedCount < 0 ? newCount > oldCount : newCount == expectedCount;
        }

        @Override
        public String getFailureMessage() {
            return "Timed out waiting for chart to update";
        }

    }

    private static class EditorIsActive extends DefaultCondition {

        private String editorName;
        public EditorIsActive(String editorName) {
            this.editorName = editorName;
        }

        @Override
        public boolean test() {
            return TestCreateSystemtapScript.bot.activeEditor().getTitle().equals(editorName);
        }

        @Override
        public String getFailureMessage() {
            return "Timed out waiting for editor with name \"" + editorName + "\" to become active.";
        }
    }

    @BeforeClass
    public static void beforeClass() {
        bot = new SWTWorkbenchBot();

        try {
            bot.viewByTitle("Welcome").close();
            // hide Subclipse Usage stats popup if present/installed
            bot.shell("Subclipse Usage").activate();
            bot.button("Cancel").click();
        } catch (WidgetNotFoundException e) {
            //ignore
        }

        prepareTreeSettings();

        // Set SystemTap IDE perspective.
        bot.perspectiveByLabel("SystemTap IDE").activate();
        bot.sleep(500);
        for (SWTBotShell sh : bot.shells()) {
            if (sh.getText().startsWith("SystemTap IDE")) {
                mainShell = sh;
                sh.activate();
                bot.sleep(500);
                break;
            }
        }

        // Dismiss "Systemtap not installed" dialog(s) if present.
        try {
            SWTBotShell shell = bot.shell("Cannot Run SystemTap").activate();
            shell.close();

            shell = bot.shell("Cannot Run SystemTap").activate();
            shell.close();
        } catch (WidgetNotFoundException e) {
            //ignore
        }

        // Create a Systemtap project.
        SWTBotMenu fileMenu = bot.menu("File");
        SWTBotMenu newMenu = fileMenu.menu("New");
        SWTBotMenu projectMenu = newMenu.menu("Project...");
        projectMenu.click();

        SWTBotShell shell = bot.shell("New Project");
        shell.setFocus();
        shell.bot().text().setText("Project");
        bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "General", "Project"));
        bot.button("Next >").click();
        bot.textWithLabel("Project name:").setText(SYSTEMTAP_PROJECT_NAME);
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(shell));
        projectExplorer = bot.viewByTitle("Project Explorer");
        projectExplorer.setFocus();
        projectExplorer.bot().tree().select(SYSTEMTAP_PROJECT_NAME)
            .contextMenu("Go Into").click();
    }

    /**
     * Load custom contents into the Function/Probe views.
     */
    private static void prepareTreeSettings() {
        TapsetLibrary.stop();

        try {
            probeDef = File.createTempFile("probeDef", ".stp");
            funcDef = File.createTempFile("funcDef", ".stp");
            probeDef.deleteOnExit();
            funcDef.deleteOnExit();
        } catch (IOException e) {}
        // Leave one of the files blank to test token search failures.
        try (PrintWriter writer = new PrintWriter(probeDef)) {
            writer.print("test file\nptest\nlast line\n");
        } catch (FileNotFoundException e) {}

        TreeNode testProbNodeParent = new TreeNode(null, false);
        // Have static/alias folders to comply with convention (change this later).
        testProbNodeParent.add(new TreeNode(probeCategoryEmpty, true));
        testProbNodeParent.add(new TreeNode(probeCategoryFull, true));
        TreeNode testProbNodeGroup = new TreeNode(probeGroup, true);
        String innerProbe = "probegroup.inner";
        TreeNode testProbNode = new TreeDefinitionNode(new ProbeNodeData(innerProbe), innerProbe, probeDef.getPath(), true);
        testProbNode.add(new TreeNode(new ProbevarNodeData("s:string"), false));
        testProbNodeGroup.add(testProbNode);
        testProbNodeParent.getChildAt(1).add(testProbNodeGroup);
        TreeNode testProbNode2 = new TreeDefinitionNode(new ProbeNodeData(probeSingleWithoutDef), probeSingleWithoutDef, null, true);
        testProbNodeParent.getChildAt(1).add(testProbNode2);

        TreeNode testFuncNodeParent = new TreeNode(null, false);
        TreeNode testFuncNode = new TreeDefinitionNode(new FunctionNodeData("function ftest(x:long)", null), funcNodeName, funcDef.getPath(), true);
        testFuncNode.add(new TreeNode(new FuncparamNodeData("long"), "x", false));
        testFuncNodeParent.add(testFuncNode);

        TreeSettings.setTrees(testFuncNodeParent, testProbNodeParent);
        TapsetLibrary.readTreeFile();
    }

    @After
    public void cleanUp() {
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
        mainShell.activate();
    }

    @AfterClass
    public static void finalCleanUp() {
        projectExplorer.setFocus();
        SWTBotToolbarButton forwardButton = projectExplorer.toolbarPushButton("Forward");
        projectExplorer.toolbarPushButton("Back to Workspace").click();
        bot.waitUntil(Conditions.widgetIsEnabled(forwardButton));

        projectExplorer.bot().tree().select(SYSTEMTAP_PROJECT_NAME)
            .contextMenu("Delete").click();
        SWTBotShell deleteShell = bot.shell("Delete Resources");
        deleteShell.bot().button("OK").click();
        bot.waitUntil(Conditions.shellCloses(deleteShell));
    }

    public static void createScript(SWTWorkbenchBot bot, String scriptName) {
        SWTBotMenu fileMenu = bot.menu("File");
        SWTBotMenu newMenu = fileMenu.menu("New");
        SWTBotMenu projectMenu = newMenu.menu("Other...");
        projectMenu.click();

        SWTBotShell shell = bot.shell("New");
        shell.setFocus();
        shell.bot().text().setText("SystemTap");
        bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "SystemTap", "SystemTap Script"));
        bot.button("Next >").click();

        SWTBotText text = bot.textWithLabel("Script Name:").setText(scriptName);
        assertEquals(scriptName, text.getText());
        text = bot.textWithLabel("Project:").setText(SYSTEMTAP_PROJECT_NAME);
        assertEquals(SYSTEMTAP_PROJECT_NAME, text.getText());

        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(shell));

        assertEquals(scriptName, bot.activeEditor().getTitle());
    }

    private static SWTBotShell prepareScript(String scriptName, String scriptContents) {
        createScript(bot, scriptName);
        if (scriptContents != null) {
            SWTBotEclipseEditor editor = bot.editorByTitle(scriptName).toTextEditor();
            editor.setText(scriptContents);
            editor.save();
        }

        openRunConfigurations(scriptName);
        SWTBotShell shell = bot.shell("Run Configurations");
        shell.setFocus();
        bot.tree().select("SystemTap").contextMenu("New").click();

        // Select the "Graphing" tab and enable output graphing.
        bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_graphingTitle).activate();
        bot.checkBox(Messages.SystemTapScriptGraphOptionsTab_graphOutputRun).click();
        return shell;
    }

    @Test
    public void testCreateScript() {
        String scriptName = "testScript.stp";
        createScript(bot, scriptName);

        // Type a script
        SWTBotEclipseEditor editor = bot.editorByTitle(scriptName).toTextEditor();
        editor.typeText(0, editor.getText().length(), "\nprobe begin{log(\"began");
        editor.typeText(0, editor.getText().length() - 1, "); exit(");
        editor.typeText(0, editor.getText().length(), "}");
        editor.save();

        openRunConfigurations(scriptName);
        SWTBotShell shell = bot.shell("Run Configurations");
        shell.setFocus();
        bot.tree().select("SystemTap").contextMenu("New").click();
    }

    @Test
    public void testTapsetContents() {
        // Create a blank script and add a function to it while it's open.
        String scriptName = "probeScript.stp";
        createScript(bot, scriptName);

        SWTBotView funcView = bot.viewByTitle("Function");
        funcView.setFocus();
        SWTBotTree funcTree = funcView.bot().tree();

        SWTBotTreeItem item = funcTree.getTreeItem(funcNodeName);
        item.doubleClick();
        SWTBotEclipseEditor editor = bot.activeEditor().toTextEditor();
        assertTrue(editor.getText().contains(item.getText()));

        // Open a non-stap file and add a probe. This should bring up a dialog
        // asking if the function should be added to the only open .stp file.
        SWTBotMenu fileMenu = bot.menu("File");
        SWTBotMenu newMenu = fileMenu.menu("New");
        SWTBotMenu projectMenu = newMenu.menu("Other...");
        projectMenu.click();
        SWTBotShell shell = bot.shell("New");
        shell.setFocus();
        shell.bot().text().setText("Untitled Text File");
        bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "General", "Untitled Text File"));
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(shell));

        SWTBotView probeView = bot.viewByTitle("Probe Alias");
        probeView.setFocus();
        SWTBotTree probeTree = probeView.bot().tree();
        SWTBotTreeItem probeCategory = probeTree.getTreeItem(probeCategoryFull);
        probeCategory.expand();
        bot.waitUntil(new TreeItemPopulated(probeCategory));

        String dialogTitle = "Select Script";
        item = probeCategory.getNode(probeGroup);
        item.expand();
        bot.waitUntil(new TreeItemPopulated(item));
        item = item.getNode(0);
        item.doubleClick();
        shell = bot.shell(dialogTitle);
        shell.setFocus();
        bot.button("Yes").click();
        bot.waitUntil(Conditions.shellCloses(shell));

        // The editor containing the script should now be in focus.
        bot.waitUntil(new EditorIsActive(scriptName));
        assertTrue(wasProbeInserted(editor, item, false));

        // Open the probe's definition file (an .stp script).
        probeView.show();
        item.contextMenu("View Definition").click();
        bot.waitUntil(new EditorIsActive(probeDef.getName()));

        // Adding a probe while an .stp editor is in focus should always add it
        // to that editor, even if multiple .stp editors are open.
        item = probeCategory.getNode(probeGroup);
        item.doubleClick();
        assertTrue(wasProbeInserted(bot.activeEditor().toTextEditor(), item, true));
        assertFalse(wasProbeInserted(editor, item, true));

        // Switch to the non-stp editor, and add a probe. A dialog should appear
        // to let the user choose which of the open files to add to.
        editor = bot.editorByTitle("Untitled 1").toTextEditor();
        editor.show();
        item = probeCategory.getNode(probeSingleWithoutDef);
        item.doubleClick();
        shell = bot.shell(dialogTitle);
        shell.setFocus();
        SWTBotTable table = bot.table();
        assertTrue(table.containsItem(scriptName));
        assertTrue(table.containsItem(probeDef.getName()));
        table.select(scriptName);
        bot.button("OK").click();
        bot.waitUntil(Conditions.shellCloses(shell));
        bot.waitUntil(new EditorIsActive(scriptName));
        assertTrue(wasProbeInserted(bot.activeEditor().toTextEditor(), item, false));
    }

    private static boolean wasProbeInserted(SWTBotEclipseEditor editor, SWTBotTreeItem probeNode, boolean isGroup) {
        String scriptText = editor.getText();
        int entryIndex = scriptText.indexOf("probe " + probeNode.getText() + (isGroup ? ".*\n" : "\n"));
        if (entryIndex == -1) {
            return false;
        }
        String probeText = scriptText.substring(entryIndex);
        if (!isGroup) {
            SWTBotTreeItem[] variables = probeNode.getItems();
            if (variables.length > 0) {
                // If the probe has variables, each one should be mentioned in comments.
                for (SWTBotTreeItem variable : probeNode.getItems()) {
                    if (!probeText.contains(variable.getText())) {
                        return false;
                    }
                }
            } else if (probeText.contains("variables")) {
                // If the probe has no variables, no mention of variables should be added in comments.
                return false;
            }
        }
        return true;
    }

    @Test
    public void testGraphErrors() {
        SWTBotShell shell = prepareScript("missingColumns.stp", null);

        SWTBotButton runButton = bot.button("Run");
        SWTBotButton addButton = bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton);
        SWTBotButton editButton = bot.button(Messages.SystemTapScriptGraphOptionsTab_EditGraphButton);
        SWTBotButton dupButton = bot.button(Messages.SystemTapScriptGraphOptionsTab_DuplicateGraphButton);
        SWTBotButton remButton = bot.button(Messages.SystemTapScriptGraphOptionsTab_RemoveGraphButton);
        String graphID = "org.eclipse.linuxtools.systemtap.graphing.ui.charts.scatterchartbuilder";

        // As soon as the Graphing tab is entered, no regular expression exists & nothing can be run.
        SWTBotCombo combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
        assertEquals("", combo.getText());
        assertFalse(runButton.isEnabled());
        assertFalse(addButton.isEnabled());
        combo.setText("(1)(2)");
        assertEquals("(1)(2)", combo.getText());
        assertTrue(runButton.isEnabled());
        assertTrue(addButton.isEnabled());

        setupGraphWithTests("Graph", false);
        assertTrue(runButton.isEnabled());

        // Removing groups from the regex disables graphs that rely on those groups.
        combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
        combo.setText("(1)");
        assertFalse(runButton.isEnabled());
        combo.setText("(1)(2)(3)");
        assertTrue(runButton.isEnabled());

        final SWTBotTable table = bot.table();
        table.select(0);
        dupButton.click();
        assertEquals(2, table.rowCount());
        assertTrue(runButton.isEnabled());

        combo.setText("(1)");
        assertFalse(runButton.isEnabled());
        for (int i = 0, n = table.rowCount(); i < n; i++) {
            String itemTitle = table.getTableItem(i).getText();
            assertTrue("Graph " + i + " should be invalid, but it's not: " + itemTitle,
                    table.getTableItem(i).getText().contains(Messages.SystemTapScriptGraphOptionsTab_invalidGraph));
        }

        setupGraphGeneral("Safe", 1, graphID, true, false);
        assertFalse(table.getTableItem(2).getText().contains(Messages.SystemTapScriptGraphOptionsTab_invalidGraph));
        combo.setText("(1)(2)(3)");
        assertTrue(runButton.isEnabled());

        setupGraphGeneral("Unsafe", 3, graphID, true, false);
        assertTrue(runButton.isEnabled());
        combo.setText("(1)(2)");
        assertFalse(runButton.isEnabled());
        for (int i = 0, n = table.rowCount(); i < n; i++) {
            String itemTitle = table.getTableItem(i).getText();
            assertTrue("Graph " + i + " has incorrect validity: " + itemTitle,
                    !itemTitle.contains(Messages.SystemTapScriptGraphOptionsTab_invalidGraph)
                    || itemTitle.contains("Unsafe"));
        }

        table.select(3);
        dupButton.click();
        remButton.click();
        assertTrue(!runButton.isEnabled());

        table.select(3);
        editButton.click();
        SWTBotShell graphShell = bot.shell("Edit Graph").activate();
        SWTBotButton finishButton = bot.button("Finish");
        assertTrue(!finishButton.isEnabled());
        bot.comboBox("<Deleted>").setSelection("NA");
        finishButton.click();
        bot.waitUntil(Conditions.shellCloses(graphShell));
        shell.setFocus();
        assertTrue(runButton.isEnabled());

        // Perform tests when graphs have an invalid graphID.
        UIThreadRunnable.syncExec(new VoidResult() {

            @Override
            public void run() {
                GraphData gd = (GraphData) table.getTableItem(0).widget.getData();
                gd.graphID = "invalidID";
                table.getTableItem(0).widget.setData(gd);
            }
        });

        combo.setText(combo.getText().concat(" ")); // Just to refresh the dialog
        assertFalse(runButton.isEnabled());
        assertTrue(table.getTableItem(0).getText().contains(Messages.SystemTapScriptGraphOptionsTab_invalidGraphID));

        table.select(0);
        dupButton.click();
        remButton.click();
        assertFalse(runButton.isEnabled());
        assertTrue(table.getTableItem(table.rowCount() - 1).getText().contains(Messages.SystemTapScriptGraphOptionsTab_invalidGraphID));

        table.select(table.rowCount() - 1);
        editButton.click();
        graphShell = bot.shell("Edit Graph").activate();
        finishButton = bot.button("Finish");
        assertFalse(finishButton.isEnabled());
        bot.radio(0).click();
        finishButton.click();
        bot.waitUntil(Conditions.shellCloses(graphShell));
        shell.setFocus();
        assertTrue(runButton.isEnabled());

        // Removing all invalid graphs should restore validity.
        combo.setText("(1)");
        assertFalse(runButton.isEnabled());
        for (int i = table.rowCount() - 1; i >= 0; i--) {
            if (table.getTableItem(i).getText().contains(Messages.SystemTapScriptGraphOptionsTab_invalidGraph)) {
                table.select(i);
                remButton.click();
            }
        }
        assertTrue(runButton.isEnabled());
        while (table.rowCount() > 0) {
            table.select(0);
            remButton.click();
        }

        bot.button("Apply").click();
    }

    @Test
    public void testDeleteBlankRegex() {
        SWTBotShell shell = prepareScript("blank.stp", null);

        // Confirm that adding a new regex when the current one is blank has no effect.
        SWTBotCombo combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
        assertEquals(2, combo.itemCount());
        combo.setSelection(1);
        assertEquals(2, combo.itemCount());
        assertEquals(0, combo.selectionIndex());

        // Confirm that adding a regex works when the current regex is not empty.
        combo.setText("(a) b (c)");
        assertEquals("(a) b (c)", combo.getText());
        combo.setSelection(1);
        assertEquals(3, combo.itemCount());
        assertEquals(1, combo.selectionIndex());

        // Confirm that a blank regex is not removed when other data is not empty.
        combo.setText("(a)");
        bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).click();
        SWTBotShell graphShell = bot.shell("Create Graph").activate();
        graphShell.setFocus();
        bot.textWithLabel("Title:").setText("Test");
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(graphShell));
        shell.setFocus();
        combo.setText("");
        assertEquals("", combo.getText());
        combo.setSelection(0);
        assertEquals(3, combo.itemCount());

        // Confirm that auto-deleting a blank regex in the middle of the regex list works properly.
        combo.setSelection(2);
        assertEquals(4, combo.itemCount());
        combo.setText("sample");
        combo.setSelection(1);
        assertEquals(4, combo.itemCount());
        SWTBotTable table = bot.table();
        SWTBotButton remButton = bot.button(Messages.SystemTapScriptGraphOptionsTab_RemoveGraphButton);
        assertTrue(!remButton.isEnabled());
        table.select(0);
        assertTrue(remButton.isEnabled());
        remButton.click();
        assertTrue(!remButton.isEnabled());
        combo.setSelection(2);
        assertEquals(3, combo.itemCount());
        assertEquals("sample", combo.getText());
        assertEquals("(a) b (c)", combo.items()[0]);
        assertEquals("sample", combo.items()[1]);

        // Confirm that auto-deleting a regex from the beginning of the list works properly.
        combo.setSelection(2);
        combo.setText("another sample");
        combo.setSelection(0);
        combo.setText("");
        combo.setSelection(1);
        assertEquals(3, combo.itemCount());
        assertEquals("sample", combo.getText());
        assertEquals("sample", combo.items()[0]);
        assertEquals("another sample", combo.items()[1]);
    }

    @Test
    public void testGraphConfig() {
        String scriptName = "testGraph.stp";
        createScript(bot, scriptName);

        final String val0 = "i";
        final String val1 = "j";
        final String val2 = "k";

        openRunConfigurations(scriptName);
        SWTBotShell shell = bot.shell("Run Configurations");
        shell.setFocus();
        bot.tree().select("SystemTap").contextMenu("New").click();
        bot.textWithLabel("Name:").setText(scriptName);

        // Select the "Graphing" tab.
        SWTBotCTabItem tab = bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_graphingTitle);
        tab.activate();
        bot.checkBox(Messages.SystemTapScriptGraphOptionsTab_graphOutputRun).click();

        // Create first regex.
        SWTBotCombo combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
        combo.setText("Value:(\\d+) (\\d+)");
        assertEquals("Value:(\\d+) (\\d+)", combo.getText());
        SWTBotText text = bot.textWithLabel(Messages.SystemTapScriptGraphOptionsTab_sampleOutputLabel);
        text.setText("Value:1 2");
        assertEquals("Value:1 2", text.getText());

        text = bot.text(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultColumnTitleBase, 1));
        text.setText(val0);
        assertEquals(val0, text.getText());
        text = bot.text(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultColumnTitleBase, 2));
        text.setText(val1);
        assertEquals(val1, text.getText());
        setupGraphWithTests("Values", false);

        // Make a second regex, and a graph for it.
        shell.setFocus();
        combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
        assertEquals(2, combo.itemCount());
        combo.setSelection(combo.selectionIndex() + 1);
        assertEquals(3, combo.itemCount());
        assertEquals("", combo.getText());
        combo.setText("Other:(\\d+) (\\d+)");
        assertEquals("Other:(\\d+) (\\d+)", combo.getText());

        text = bot.text(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultColumnTitleBase, 1));
        text.setText(val0);
        assertEquals(val0, text.getText());
        text = bot.text(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultColumnTitleBase, 2));
        text.setText(val2);
        assertEquals(val2, text.getText());

        text = bot.textWithLabel(Messages.SystemTapScriptGraphOptionsTab_sampleOutputLabel);
        assertEquals("", text.getText());
        setupGraphWithTests("Others", false);

        // Apply the changes, then close the menu & reopen it to make sure settings were saved.
        shell.setFocus();
        bot.button("Apply").click();
        bot.button("Close").click();
        bot.waitUntil(Conditions.shellCloses(shell));
        openRunConfigurations(scriptName);
        shell = bot.shell("Run Configurations");
        shell.setFocus();
        shell.bot().text().setText(scriptName); // Set the filter text to show configs of the current script
        bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "SystemTap", scriptName));
        tab = bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_graphingTitle);
        tab.activate();

        combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
        text = bot.textWithLabel(Messages.SystemTapScriptGraphOptionsTab_sampleOutputLabel);
        SWTBotTable table = bot.table();
        assertEquals(3, combo.itemCount());
        assertEquals("Value:(\\d+) (\\d+)", combo.getText());
        assertEquals("Value:1 2", text.getText());
        assertEquals(1, table.rowCount());
        String graphName = GraphFactory.getGraphName("org.eclipse.linuxtools.systemtap.graphing.ui.charts.scatterchartbuilder");
        assertTrue(table.containsItem(graphName.concat(":Values")));
        combo.setSelection(1);
        assertEquals("Other:(\\d+) (\\d+)", combo.getText());
        assertEquals("", text.getText());
        assertEquals(1, table.rowCount());
        assertTrue(table.containsItem(graphName.concat(":Others")));
    }

    @Test
    public void testGraphContents() {
        final String valA1 = "A1";
        final String valB1 = "B1";
        createAndViewDummyData(
                new String[]{valA1, valB1},
                new Integer[]{
                        0,0, 1,2, 2,4, 3,6, 4,8, 5,10, 6,12, 7,14, 8,16, 9,18});
        SWTBotEditor graphEditorA = bot.activeEditor();

        final String valA2 = "A2";
        final String valB2 = "B2";
        createAndViewDummyData(
                new String[]{valA2, valB2},
                new Integer[]{
                        2,0, 5,1, 7,2, 10,3});
        SWTBotEditor graphEditorB = bot.activeEditor();

        // Add graphs.
        setupGraphWithTests("Others", true);
        String graphTitle2 = "Others - Scatter Graph";
        graphEditorA.show();
        setupGraphWithTests("Values", true);
        String graphTitle1 = "Values - Scatter Graph";

        // Test table & graph contents.
        graphEditorA.bot().cTabItem("Data View").activate();
        SWTBotTable dataTable = bot.table();
        List<String> colNames = dataTable.columns();
        assertEquals(3, colNames.size());
        assertEquals(valA1, colNames.get(1));
        assertEquals(valB1, colNames.get(2));
        assertEquals("2", dataTable.cell(2, 1));
        assertEquals("4", dataTable.cell(2, 2));

        graphEditorA.bot().cTabItem(graphTitle1).activate();
        Matcher<AbstractChartBuilder> matcher = widgetOfType(AbstractChartBuilder.class);
        AbstractChartBuilder cb = bot.widget(matcher);
        ISeries[] series = cb.getChart().getSeriesSet().getSeries();
        assertEquals(2, series.length);
        assertEquals(10, series[0].getXSeries().length);
        assertEquals(10, series[1].getXSeries().length);
        assertEquals(2, (int) series[0].getYSeries()[2]);
        assertEquals(4, (int) series[1].getYSeries()[2]);

        graphEditorB.show();
        graphEditorB.bot().cTabItem("Data View").activate();
        dataTable = bot.table();
        colNames = dataTable.columns();
        assertEquals(3, colNames.size());
        assertEquals(valA2, colNames.get(1));
        assertEquals(valB2, colNames.get(2));
        assertEquals("7", dataTable.cell(2, 1));
        assertEquals("2", dataTable.cell(2, 2));

        graphEditorB.bot().cTabItem(graphTitle2).activate();
        cb = bot.widget(matcher);
        series = cb.getChart().getSeriesSet().getSeries();
        assertEquals(2, series.length);
        assertEquals(4, series[0].getXSeries().length);
        assertEquals(4, series[1].getXSeries().length);
        assertEquals(7, (int) series[0].getYSeries()[2]);
        assertEquals(2, (int) series[1].getYSeries()[2]);

        // Test filters on the data table & graphs.
        graphEditorA.show();
        graphEditorA.bot().cTabItem("Data View").activate();
        dataTable = bot.table();
        new SWTBotMenu(ContextMenuHelper.contextMenu(dataTable, "Add filter...")).click();
        SWTBotShell shell = bot.shell("Create Filter");
        shell.setFocus();

        // Match Filter - Remove a matching
        bot.button("Match Filter").click();
        bot.button("Next >").click();
        bot.text().setText("2");
        deselectDefaultSelection(0);
        bot.radio(1).click();
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(shell));
        bot.waitUntil(new TableHasUpdated(graphEditorA.bot().table(), 9, true));
        assertEquals("3", dataTable.cell(2, 1));
        assertEquals("6", dataTable.cell(2, 2));

        // Filters should be applied to graphs as well as data tables.
        graphEditorA.bot().cTabItem(graphTitle1).activate();
        cb = bot.widget(matcher);
        series = cb.getChart().getSeriesSet().getSeries();
        bot.waitUntil(new ChartHasUpdated(cb.getChart(), 9));
        assertEquals(3, (int) series[0].getYSeries()[2]);
        assertEquals(6, (int) series[1].getYSeries()[2]);

        // Each graph set should have its own filters.
        graphEditorB.show();
        graphEditorB.bot().cTabItem("Data View").activate();
        dataTable = bot.table();
        assertEquals(4, dataTable.rowCount());
        assertEquals("2", dataTable.cell(0, 1));

        // Test removing a filter.
        graphEditorA.show();
        graphEditorA.bot().cTabItem("Data View").activate();
        dataTable = bot.table();
        new SWTBotMenu(ContextMenuHelper.contextMenu(dataTable,
                "Remove filter...",
                "Match Filter: \"" + valA1 + "\" removing \"2\"")).click();
        bot.waitUntil(new TableHasUpdated(graphEditorA.bot().table(), 10, true));
        assertEquals("2", dataTable.cell(2, 1));
        assertEquals("4", dataTable.cell(2, 2));
    }

    @Test
    public void testGenerateFromPrintf() {
        SWTBotShell shell = prepareScript("testGenerates.stp", "#!/usr/bin/env stap"
                + "\nglobal i,j,k,a"
                + "\nprobe begin{i=0;j=5;k=20;a=65}"
                + "\n#probe begin{printf(\"%1b%1b%1blo %1b%1brld\\n\", 72,101,108,87,111)}"
                + "\nprobe timer.ms(100){printf(\"%5i|\\n%10.5d|\\n%-10.5i|\\n%05c\\n\",i,j,k,a);i++;j+=5;k+=20;a++}"
                + "\n//printf(\"this is a comment\\n\");"
                + "\n/*printf(\"this is a...\\n\");"
                + "\nprintf(\"...multiline comment\\n\");*/"
                + "\nprobe begin{b = sprintf(\"Here\"); printf(\"-->%s<--\\n\", b)}"
                + "\nprobe timer.ms(100){printf(\"%x - %#x - %#X\\n\",i,i,i);}"
                + "\nprobe timer.ms(100){printf(\"%o - %#o\\n\",i,i);}"
                + "\nprobe begin{printf(\"%1b-\\\\n-%p\\n\", 65, 0x8000000002345678)}");

        // Generate regexs.
        bot.button(Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsButton).click();

        SWTBotShell dialogShell = bot.shell(Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsTitle);
        dialogShell.setFocus();
        bot.button("Yes").click();
        bot.waitUntil(Conditions.shellCloses(dialogShell));
        shell.setFocus();

        SWTBotCombo combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
        assertEquals(9, combo.itemCount()); // One extra entry for "Add New Regex"

        String[] expectedRegexs = new String[]{
                " {0,4}(-?\\d+)\\|",
                " {0,9}(-?\\d+)\\|",
                "(-?\\d+) {0,9}\\|",
                " {0,4}(.)",
                "-->(.+)<--",
                "([a-f0-9]+) - (0x[a-f0-9]+) - (0X[A-F0-9]+)",
                "(\\d+) - (0\\d+)",
                "(.)-\\\\n-(0x[a-f0-9]+)",
                Messages.SystemTapScriptGraphOptionsTab_regexAddNew
        };
        for (int i = 0, n = combo.itemCount(); i < n; i++) {
            assertEquals(expectedRegexs[i], combo.items()[i]);
        }
        bot.button("Apply").click();
    }

    @Test
    public void testLabelledGraphScript() {
        final int numItems = 13;
        final int numNumberItems = 4;
        final int numCategories = 3;
        createAndViewDummyData(
                new String[] {
                "Fruit", "Number", "Freshness", "Tastiness"},

                new Object[] {
                "Apples", 2, 14, 16,
                1, 1, 2, 3,
                "Bananas (2)", 10, 10, 10,
                "Cherries", 10, 20, 30,
                2, 2, 4, 6,
                "Apples", 12, 5, 16,
                "Bananas", 0, 1, 0,
                3, 3, 6, 9,
                "Dates", 12, 5, 16,
                "Bananas", 2, 1, 2,
                4, 4, 8, 12,
                "Apples", 12, 5, 16,
                "Bananas (2)", 3, 1, 3
                });
        SWTBotEditor graphEditor = bot.activeEditor();

        // Add bar, pie, and line graphs that use the same column data.
        String title = "Fruit Info";
        setupGraphGeneral(title, 4, "org.eclipse.linuxtools.systemtap.graphing.ui.charts.barchartbuilder", false, true);
        setupGraphGeneral(title, 4, "org.eclipse.linuxtools.systemtap.graphing.ui.charts.piechartbuilder", false, true);
        setupGraphGeneral(title, 4, "org.eclipse.linuxtools.systemtap.graphing.ui.charts.linechartbuilder", false, true);

        // Confirm that the bar & pie charts display the String categories, but the line chart ignores them.
        String titleBar = title + " - Bar Graph";
        String titlePie = title + " - Pie Chart";
        String titleLine = title + " - Line Graph";
        graphEditor.bot().cTabItem(titleLine).activate();
        Matcher<AbstractChartBuilder> matcher = widgetOfType(AbstractChartBuilder.class);
        AbstractChartBuilder cb = bot.widget(matcher);
        assertEquals(numNumberItems, cb.getChart().getSeriesSet().getSeries()[0].getXSeries().length);

        graphEditor.bot().cTabItem(titlePie).activate();
        cb = bot.widget(matcher);
        assertEquals(numItems, cb.getChart().getSeriesSet().getSeries().length);

        graphEditor.bot().cTabItem(titleBar).activate();
        cb = bot.widget(matcher);
        assertEquals(numItems, cb.getChart().getSeriesSet().getSeries()[0].getXSeries().length);

        // Test graph scaling & scrolling
        discreteXControlTests(cb, numItems); //Bar Chart
        continuousControlTests(cb, false);
        graphEditor.bot().cTabItem(titlePie).activate();
        cb = bot.widget(matcher);
        discreteXControlTests(cb, numCategories);
        graphEditor.bot().cTabItem(titleLine).activate();
        cb = bot.widget(matcher);
        continuousControlTests(cb, true);
        continuousControlTests(cb, false);
    }

    private static void discreteXControlTests(AbstractChartBuilder cb, int numAxisItems) {
        // Check that default range shows 100% of data.
        IAxis axis = cb.getChart().getAxisSet().getXAxis(0);
        Range range = axis.getRange();
        double scale = cb.getScale();
        double scroll = cb.getScroll();
        assertTrue(range.upper - range.lower == axis.getCategorySeries().length - 1 && range.upper - range.lower == numAxisItems - 1);
        assertTrue(scale == 1.0 && scroll == 1.0);

        // Check that scroll buttons are disabled at 100% range.
        SWTBotButton firstButton = bot.button(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphDiscreteXControl_First);
        SWTBotButton leftButton = bot.button(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphDiscreteXControl_Left);
        SWTBotButton rightButton = bot.button(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphDiscreteXControl_Right);
        SWTBotButton lastButton = bot.button(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphDiscreteXControl_Last);
        assertFalse(firstButton.isEnabled());
        assertFalse(leftButton.isEnabled());
        assertFalse(rightButton.isEnabled());
        assertFalse(lastButton.isEnabled());

        // Test zooming in. The amount of zoom is arbitrary for this test--just make sure zooming happened.
        SWTBotButton zoomInButton = bot.button(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphDiscreteXControl_ZoomIn);
        SWTBotButton zoomOutButton = bot.button(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphDiscreteXControl_ZoomOut);
        SWTBotButton allButton = bot.button(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphDiscreteXControl_All);
        assertTrue(zoomInButton.isEnabled());
        assertFalse(zoomOutButton.isEnabled());
        assertFalse(allButton.isEnabled());
        zoomInButton.click();
        assertTrue(zoomOutButton.isEnabled());
        assertTrue(allButton.isEnabled());

        // By default, zooming in should zoom in on the end of the axis (newest data).
        range = axis.getRange();
        assertTrue(range.upper == numAxisItems - 1 && range.lower > 0 && cb.getScale() < scale && cb.getScroll() == 1.0);

        // Left scrolling should now be enabled.
        assertTrue(firstButton.isEnabled());
        assertTrue(leftButton.isEnabled());
        assertFalse(rightButton.isEnabled());
        assertFalse(lastButton.isEnabled());

        // Test scrolling left. Again, the specific amount is arbitrary, just make sure scrolling happened.
        leftButton.click();
        range = axis.getRange();
        assertTrue(range.upper < numAxisItems - 1 && cb.getScroll() < scroll);
        int rstore = (int) range.lower;
        assertTrue(rightButton.isEnabled());
        assertTrue(lastButton.isEnabled());

        // Zooming out should bring the range back to 100%.
        zoomOutButton.click();
        range = axis.getRange();
        assertTrue(range.upper - range.lower == numAxisItems - 1 && cb.getScale() == 1.0 && cb.getScroll() < scroll);
        assertTrue(zoomInButton.isEnabled());
        assertFalse(zoomOutButton.isEnabled());
        assertFalse(allButton.isEnabled());
        assertFalse(firstButton.isEnabled());
        assertFalse(leftButton.isEnabled());
        assertFalse(rightButton.isEnabled());
        assertFalse(lastButton.isEnabled());

        // For convenience, zooming out after having scrolled somewhere should make zooming in
        // zoom back to the area that was scrolled to.
        scroll = cb.getScroll();
        zoomInButton.click();
        assertTrue(rstore == axis.getRange().lower && scroll == cb.getScroll());

        // Scrolling right should take the range back to the end of the axis.
        rightButton.click();
        range = axis.getRange();
        assertTrue(range.upper == numAxisItems - 1 && range.lower > 0 && cb.getScroll() > scroll);
        assertTrue(firstButton.isEnabled());
        assertTrue(leftButton.isEnabled());
        assertFalse(rightButton.isEnabled());
        assertFalse(lastButton.isEnabled());

        // Zoom in as much as possible (range should show only one item),
        // and step right/left. Add a loop limit for safety.
        for (int i = 0; i < numAxisItems; i++) {
            range = axis.getRange();
            if (range.upper == range.lower) {
                break;
            }
            zoomInButton.click();
        }
        range = axis.getRange();
        assertTrue(range.upper == range.lower && range.upper == numAxisItems - 1);
        assertTrue(!zoomInButton.isEnabled());
        for (int i = 0; i < numAxisItems; i++) {
            if (axis.getRange().lower == 0) {
                break;
            }
            leftButton.click();
            assertTrue(axis.getRange().lower < range.lower);
            range = axis.getRange();
            assertEquals(range.lower, range.upper, 0.0);
        }
        assertEquals(axis.getRange().lower, 0, 0.0);
        for (int i = 0; i < numAxisItems; i++) {
            if (axis.getRange().upper == numAxisItems - 1) {
                break;
            }
            rightButton.click();
            assertTrue(axis.getRange().upper > range.upper);
            range = axis.getRange();
            assertEquals(range.lower, range.upper, 0.0);
        }
        assertEquals(axis.getRange().upper, numAxisItems - 1, 0);

        firstButton.click();
        assertEquals(axis.getRange().lower, 0, 0);
        assertFalse(firstButton.isEnabled());
        assertFalse(leftButton.isEnabled());
        assertTrue(rightButton.isEnabled());
        assertTrue(lastButton.isEnabled());

        lastButton.click();
        assertEquals(axis.getRange().upper, numAxisItems - 1, 0);
        assertTrue(firstButton.isEnabled());
        assertTrue(leftButton.isEnabled());
        assertFalse(rightButton.isEnabled());
        assertFalse(lastButton.isEnabled());
    }

    private static double getAxisScale(AbstractChartBuilder cb, boolean isXAxis) {
        return isXAxis ? cb.getScale() : cb.getScaleY();
    }

    private static double getAxisScroll(AbstractChartBuilder cb, boolean isXAxis) {
        return isXAxis ? cb.getScroll() : cb.getScrollY();
    }

    private static void continuousControlTests(AbstractChartBuilder cb, boolean isXAxis) {
        // Continuous scaling/scrolling is less strict/predictable than discrete scrolling,
        // so just check that the controls perform their intended actions.
        IAxis axis;
        SWTBotButton zoomInButton, zoomOutButton;
        SWTBotScale zoomScale;
        SWTBotSlider scrollBar;
        int flipSign;
        if (isXAxis) {
            axis = cb.getChart().getAxisSet().getXAxis(0);
            zoomInButton = bot.buttonWithTooltip(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphContinuousXControl_ZoomInTooltip);
            zoomOutButton = bot.buttonWithTooltip(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphContinuousXControl_ZoomOutTooltip);
            zoomScale = bot.scaleWithTooltip(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphContinuousXControl_ScaleMessage);
            scrollBar = bot.sliderWithTooltip(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphContinuousXControl_ScrollMessage);
            flipSign = 1;
        } else {
            axis = cb.getChart().getAxisSet().getYAxis(0);
            zoomInButton = bot.buttonWithTooltip(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphContinuousYControl_ZoomInTooltip);
            zoomOutButton = bot.buttonWithTooltip(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphContinuousYControl_ZoomOutTooltip);
            zoomScale = bot.scaleWithTooltip(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphContinuousYControl_ScaleMessage);
            scrollBar = bot.sliderWithTooltip(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphContinuousYControl_ScrollMessage);
            flipSign = -1;
        }
        double scale = getAxisScale(cb, isXAxis);
        double scroll = getAxisScroll(cb, isXAxis);
        int thumb = scrollBar.getThumb();

        // Default range should be 100%, so zooming out shouldn't have an effect yet.
        assertEquals(scale, 1.0, 0);
        int zoomValue = zoomScale.getValue();
        Range range = axis.getRange();
        zoomOutButton.click();
        Range range2 = axis.getRange();
        assertTrue(range.upper == range2.upper && range.lower == range2.lower && zoomScale.getValue() == zoomValue && getAxisScale(cb, isXAxis) == scale && scrollBar.getThumb() == thumb);

        // Zoom in & back out with the zoom buttons.
        zoomInButton.click();
        range2 = axis.getRange();
        assertTrue(range2.upper - range2.lower < range.upper - range.lower && flipSign * (zoomScale.getValue() - zoomValue) > 0 && getAxisScale(cb, isXAxis) < scale && scrollBar.getThumb() < thumb);
        zoomOutButton.click();
        range2 = axis.getRange();
        assertTrue(range.upper == range2.upper && range.lower == range2.lower && zoomScale.getValue() == zoomValue && getAxisScale(cb, isXAxis) == scale && scrollBar.getThumb() == thumb);

        // Zoom in with the Scale control.
        int controlRange = zoomScale.getMaximum() - zoomScale.getMinimum();
        zoomScale.setValue(zoomScale.getValue() + controlRange / 2 * flipSign);
        // Note: the charts need some time to be updated after using the scale/slider controls.
        // Sleeping for a brief moment is faster than using a bot wait condition.
        bot.sleep(100);
        range2 = axis.getRange();
        assertTrue(range2.upper - range2.lower < range.upper - range.lower && getAxisScale(cb, isXAxis) < scale && scrollBar.getThumb() < thumb);

        range = range2;
        thumb = scrollBar.getThumb();
        scale = getAxisScale(cb, isXAxis);
        zoomScale.setValue(zoomScale.getValue() - controlRange / 4 * flipSign);
        bot.sleep(100);
        range2 = axis.getRange();
        assertTrue(range2.upper - range2.lower > range.upper - range.lower && getAxisScale(cb, isXAxis) > scale && scrollBar.getThumb() > thumb);

        // Test scrolling. Don't assume an initial scroll position, as it may be changed
        // in future versions (it's more likely to change than default zoom, at least).
        thumb = scrollBar.getThumb();
        controlRange = scrollBar.getMaximum() - scrollBar.getThumb() - scrollBar.getMinimum();
        scrollBar.setSelection(controlRange / 2);
        bot.sleep(100);
        assertEquals(scrollBar.getThumb(), thumb);

        // Scroll towards origin.
        range = axis.getRange();
        scrollBar.setSelection(scrollBar.getSelection() - controlRange / 4 * flipSign);
        bot.sleep(100);
        range2 = axis.getRange();
        assertTrue(range2.upper - range2.lower == range.upper - range.lower && range2.upper < range.upper && getAxisScroll(cb, isXAxis) < scroll);

        // Scroll away from origin.
        range = range2;
        scroll = getAxisScroll(cb, isXAxis);
        scrollBar.setSelection(scrollBar.getSelection() + controlRange / 8 * flipSign);
        bot.sleep(100);
        range2 = axis.getRange();
        assertTrue(range2.upper - range2.lower == range.upper - range.lower && range2.upper > range.upper && getAxisScroll(cb, isXAxis) > scroll);
    }

    @Test
    public void testGraphTooltips() {
        createAndViewDummyData(new String[]{"Column 1"}, new Integer[]{1,2,3,4,5});

        // Add bar, pie, and line graphs that use the same column data.
        String title = "Info";
        setupGraphGeneral(title, 1, "org.eclipse.linuxtools.systemtap.graphing.ui.charts.linechartbuilder", true, true);
        setupGraphGeneral(title, 1, "org.eclipse.linuxtools.systemtap.graphing.ui.charts.barchartbuilder", true, true);

        // Perform mouse hover tests on graphs.
        bot.activeEditor().bot().cTabItem(title.concat(" - Bar Graph")).activate();
        final Matcher<AbstractChartBuilder> matcher = widgetOfType(AbstractChartBuilder.class);
        AbstractChartBuilder cb = bot.widget(matcher);
        String tooltipFormat = "{0}: {1}";
        checkTooltipAtDataPoint(cb, 0, MessageFormat.format(tooltipFormat, "Column 1", "1"), true);

        bot.activeEditor().bot().cTabItem(title.concat(" - Line Graph")).activate();
        cb = bot.widget(matcher);
        tooltipFormat = "Series: {0}\nx: {1}\ny: {2}";
        String lineChartTooltip = MessageFormat.format(tooltipFormat, "Column 1", "2", "2");
        checkTooltipAtDataPoint(cb, 1, lineChartTooltip, true);

        // The tooltip should disappear when a point moves away from the mouse, without need for mouse movement.
        cb.setScale(0.2);
        checkTooltipAtDataPoint(cb, -1, lineChartTooltip, false);
    }

    /**
     * May move the mouse to a desired data point on a chart and test for the tooltip that appears.
     * @param cb The AbstractChartBuilder containing the chart to test.
     * @param dataPoint The data point of the series to move the mouse to. Set this to -1
     * or less if the mouse should stay where it is.
     * @param expectedTooltip The expected contents of the tooltip.
     * @param shellShouldExist Set to <code>false</code> if the tooltip should not be found.
     */
    private static void checkTooltipAtDataPoint(final AbstractChartBuilder cb, final int dataPoint, final String expectedTooltip,
            final boolean shellShouldExist) {

        for (int retries = 5; retries > 0; retries--) {

            if (dataPoint >= 0) {
                final Event event = new Event();
                event.type = SWT.MouseMove;
                // Jitter the mouse before moving to the data point
                UIThreadRunnable.syncExec(new VoidResult() {
                    @Override
                    public void run() {
                        event.x = 0;
                        event.y = 0;
                        bot.getDisplay().post(event);
                    }
                });
                bot.sleep(100);
                UIThreadRunnable.syncExec(new VoidResult() {
                    @Override
                    public void run() {
                        Point mousePoint = cb.getChart().getPlotArea().toDisplay(
                                cb.getChart().getSeriesSet().getSeries()[0].getPixelCoordinates(dataPoint));
                        event.x = mousePoint.x;
                        event.y = mousePoint.y;
                        bot.getDisplay().post(event);
                    }
                });
            }

            bot.sleep(500); // Give some time for the tooltip to appear/change

            boolean foundTooltip = UIThreadRunnable.syncExec(new BoolResult() {
                @Override
                public Boolean run() {
                    for (SWTBotShell bshell : bot.shells()) {
                        Control[] children = bshell.widget.getChildren();
                        if (children.length == 1 && children[0] instanceof Text
                                && children[0].isVisible()
                                && expectedTooltip.equals(((Text) children[0]).getText())) {

                            return true;
                        }
                    }
                    return false;
                }
            });

            if (foundTooltip == shellShouldExist) {
                return;
            }
        }

        if (shellShouldExist) {
            throw new AssertionError("Didn't find the expected tooltip: " + expectedTooltip);
        } else {
            throw new AssertionError("Did not expect to find this tooltip, but found it: " + expectedTooltip);
        }
    }

    private static void openRunConfigurations(String scriptName) {
        // Focus on project explorer view.
        projectExplorer.setFocus();
        new SWTBotMenu(ContextMenuHelper.contextMenu(
                projectExplorer.bot().tree().select(scriptName),
                "Run As", "Run Configurations...")).click();
    }

    private static void setupGraphWithTests(String title, boolean isTab) {
        SWTBotShell firstShell = bot.activeShell();

        openGraphMenu(isTab);
        SWTBotShell shell = bot.shell("Create Graph");
        shell.setFocus();

        SWTBotText text = bot.textWithLabel("Title:");
        text.setText(title);
        assertEquals(title, text.getText());

        SWTBotCombo comboX = bot.comboBoxWithLabel("X Series:");
        assertEquals(3, comboX.itemCount()); // X Series includes "Row Num" as a selection
        SWTBotCombo comboY0 = bot.comboBoxWithLabel("Y Series 0:");
        assertEquals(2, comboY0.itemCount()); // Y Series 0 only includes series entries
        comboY0.setSelection(0);
        SWTBotCombo comboY1 = bot.comboBoxWithLabel("Y Series 1:");
        assertEquals(3, comboY1.itemCount()); // Y Series (i>0) has extra "NA" option as first entry
        comboY1.setSelection(1);
        assertFalse(bot.button("Finish").isEnabled()); // Don't allow duplicate selections
        comboY1.setSelection(2);
        bot.button("Finish").click();

        bot.waitUntil(Conditions.shellCloses(shell));
        firstShell.setFocus();
    }

    private static void setupGraphGeneral(String title, int numItems, String graphID, boolean useRowNum, boolean isTab) {
        int offset = useRowNum ? 0 : 1;
        SWTBotShell firstShell = bot.activeShell();

        openGraphMenu(isTab);
        SWTBotShell shell = bot.shell("Create Graph");
        shell.setFocus();

        deselectDefaultSelection(0);
        bot.radioWithTooltip(GraphFactory.getGraphName(graphID) + "\n\n" +
                GraphFactory.getGraphDescription(graphID)).click();

        SWTBotText text = bot.textWithLabel("Title:");
        text.setText(title);

        bot.comboBoxWithLabel("X Series:").setSelection(offset);
        bot.comboBoxWithLabel("Y Series 0:").setSelection(offset);
        for (int i = 1; i < numItems - offset; i++) {
            bot.comboBoxWithLabel(MessageFormat.format("Y Series {0}:", i)).setSelection(i + 1 + offset);
        }
        if (!useRowNum) {
            bot.comboBoxWithLabel(MessageFormat.format("Y Series {0}:", numItems - 1)).setSelection(0);
        }
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(shell));
        firstShell.setFocus();
    }

    private static void openGraphMenu(boolean isTab) {
        if (!isTab) {
            bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).click();
        } else {
            // The "Add Graph" button is actually a tab that doesn't get activated when clicked.
            // Use a background thread to supress the wait for tab activation.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bot.activeEditor().bot().cTabItem(1).activate();
                    } catch (TimeoutException e) {}
                }
            }).start();
        }
    }

    private static void createAndViewDummyData(String[] titles, Object[] data) {
        if (data.length % titles.length != 0) {
            throw new IllegalArgumentException("data.length must be a multiple of titles.length.");
        }
        final int numRows = data.length / titles.length;
        IFilteredDataSet dataset = new FilteredRowDataSet(titles);
        for (int i = 0; i < numRows; i++) {
            IDataEntry dataEntry = new RowEntry();
            Object[] values = new Object[titles.length];
            for (int v = 0; v < titles.length; v++) {
                values[v] = data[titles.length * i + v];
            }
            dataEntry.putRow(0, values);
            dataset.setData(dataEntry);
        }
        final File dataFile;
        try {
            dataFile = File.createTempFile("testSet", ".set");
            dataFile.deleteOnExit();
            if (!dataset.writeToFile(dataFile)) {
                throw new IOException();
            }
        } catch (IOException e) {
            fail("Could not create dummy data set.");
            return;
        }

        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                new ImportDataSetHandler().execute(dataFile.getPath());
            }
        });
        String editorName = dataFile.getName().concat(" Graphs");
        bot.waitUntil(new EditorIsActive(editorName));
    }

    /**
     * Deselects a radio button.
     * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=344484
     * @param currSelection The index of the radiobutton to deselect
     */
    private static void deselectDefaultSelection(final int currSelection) {
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                @SuppressWarnings("unchecked")
                Matcher<Widget> matcher = allOf(widgetOfType(Button.class), withStyle(SWT.RADIO, "SWT.RADIO"));
                Button b = (Button) bot.widget(matcher, currSelection);
                b.setSelection(false);
            }
        });
    }
}
