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
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher.Messages;
import org.eclipse.linuxtools.systemtap.graphing.ui.charts.AbstractChartBuilder;
import org.eclipse.linuxtools.systemtap.graphing.ui.wizards.graph.GraphFactory;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
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
import org.eclipse.swtbot.swt.finder.results.VoidResult;
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

	static SWTWorkbenchBot bot;
	static boolean stapInstalled;

	private static final String SYSTEMTAP_PROJECT_NAME = "SystemtapTest";

	private static class ShellIsClosed extends DefaultCondition {

		private SWTBotShell shell;

		public ShellIsClosed(SWTBotShell shell) {
			super();
			this.shell = shell;
		}

		@Override
		public boolean test() {
			return !shell.isOpen();
		}

		@Override
		public String getFailureMessage() {
				return "Timed out waiting for " + shell + " to close."; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class NodeAvailableAndSelect extends DefaultCondition {

		private SWTBotTree tree;
		private String parent;
		private String node;

		NodeAvailableAndSelect(SWTBotTree tree, String parent, String node){
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

	private static class TreePopulated extends DefaultCondition {

		private SWTBotTree parent;

		TreePopulated(SWTBotTree parent) {
			this.parent = parent;
		}

		@Override
		public boolean test() {
			return this.parent.getAllItems().length > 0;
		}

		@Override
		public String getFailureMessage() {
			return "Timed out waiting for tree to populate.";
		}
	}

	private static class ConsoleIsReady extends DefaultCondition {

		private String scriptName;
		public ConsoleIsReady(String scriptName) {
			this.scriptName = scriptName;
		}

		@Override
		public boolean test() {
			SWTBotView console = TestCreateSystemtapScript.bot.viewById("org.eclipse.ui.console.ConsoleView");
			console.setFocus();
			return console.bot().label().getText().contains(scriptName);
		}

		@Override
		public String getFailureMessage() {
			return "Timed out waiting for console to appear";
		}

	}

	private static class StapHasExited extends DefaultCondition {

		@Override
		public boolean test() {
			SWTBotView console = TestCreateSystemtapScript.bot.viewById("org.eclipse.ui.console.ConsoleView");
			console.setFocus();
			return (!console.toolbarButton("Stop Script").isEnabled());
		}

		@Override
		public String getFailureMessage() {
			return "Timed out waiting for stap to exit";
		}
	}

	private static class TableHasUpdated extends DefaultCondition {

		private String scriptName;
		private int graphSetNum;
		private int expectedRows;
		/**
		 * Wait for the provided GraphSelectorEditor to be fully updated. A table is considered to be updated
		 * once the number of entries in the table is equal to the expectedRows parameter of this constructor.
		 * Note that using this will set focus to the Data View tab of the specified graph set.
		 * @param graphEditor The SWTBotEditor of the GraphSelectorEditor to wait for.
		 * @param graphSetNum Which graph set to focus on & watch for updates.
		 * @param expectedRows How many entries/rows are expected to be in the table when it's fully updated.
		 */
		public TableHasUpdated(String scriptName, int graphSetNum, int expectedRows) {
			this.scriptName = scriptName;
			this.graphSetNum = graphSetNum;
			this.expectedRows = expectedRows;
		}
		@Override
		public boolean test() {
			SWTBotEditor graphEditor = TestCreateSystemtapScript.bot.editorByTitle(scriptName.concat(" Graphs"));
			graphEditor.setFocus();
			graphEditor.bot().cTabItem(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_graphSetTitleBase, graphSetNum)).activate();
			graphEditor.bot().cTabItem("Data View").activate();
			return bot.table(0).rowCount() >= expectedRows;
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
		 * Wait for the provided chart to become updated. The chart is considered
		 * to be updated once a new point is added to one of its series sets.
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

	@BeforeClass
	public static void beforeClass() {
		bot = new SWTWorkbenchBot();
		stapInstalled = true;

		// Dismiss "Systemtap not installed" dialog(s) if present.
		try {
			SWTBotShell shell = bot.shell("Cannot Run SystemTap").activate();
			stapInstalled = false;
			shell.close();

			shell = bot.shell("Cannot Run SystemTap").activate();
			shell.close();
		} catch (WidgetNotFoundException e) {
			//ignore
		}

		try {
			bot.viewByTitle("Welcome").close();
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate();
			bot.button("Cancel").click();
		} catch (WidgetNotFoundException e) {
			//ignore
		}

		// Set SystemTap IDE perspective.
		bot.perspectiveByLabel("SystemTap IDE").activate();
		bot.sleep(500);
		for (SWTBotShell sh : bot.shells()) {
			if (sh.getText().startsWith("SystemTap IDE")) {
				sh.activate();
				bot.sleep(500);
				break;
			}
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
		bot.waitUntil(new ShellIsClosed(shell));

		// Open the Debug view.
		bot.menu("Window").menu("Show View").menu("Other...").click();
		shell = bot.shell("Show View");
		shell.setFocus();
		shell.bot().text().setText("Debug");
		bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "Debug", "Debug"));
		bot.button("OK").click();
	}

	@After
	public void cleanUp() {
		bot.closeAllShells();
		bot.closeAllEditors();
	}

	@AfterClass
	public static void finalCleanUp() {
		if (ScriptConsole.anyRunning()) {
			ScriptConsole.stopAll();
			bot.waitUntil(new StapHasExited());
		}
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
		bot.waitUntil(new ShellIsClosed(shell));

		assertEquals(scriptName, bot.activeEditor().getTitle());
	}

	private SWTBotShell prepareScript(String scriptName, String scriptContents) {
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
		bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_7).activate();
		bot.checkBox(Messages.SystemTapScriptGraphOptionsTab_2).click();
		return shell;
	}

	private void clearAllTerminated(){
		SWTBotView debugView = bot.viewByTitle("Debug");
		debugView.setFocus();
		SWTBotTree debugTable = debugView.bot().tree();
		assertTrue(debugTable.getAllItems().length > 0);
		SWTBotToolbarButton remButton = debugView.toolbarPushButton("Remove All Terminated Launches");
		assertTrue(remButton.isEnabled());
		remButton.click();
		assertTrue(debugTable.getAllItems().length == 0);
		assertTrue(!remButton.isEnabled());
	}

	@Test
	public void testCreateScript(){
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

		if (stapInstalled) {
			bot.button("Run").click();
			bot.waitUntil(new ShellIsClosed(shell));

			bot.waitUntil(new ConsoleIsReady(scriptName));
			bot.waitUntil(new StapHasExited(), 10000); // The script should end on its own
		}
	}

	@Test
	public void testAddProbes(){
		// Create a blank script and add a probe to it while it's open.
		String scriptName = "probeScript.stp";
		createScript(bot, scriptName);

		SWTBotView probeView = bot.viewByTitle("Probe Alias");
		SWTBotTree probeTree = probeView.bot().tree();
		bot.waitUntil(new TreePopulated(probeTree), 10000);
		SWTBotTreeItem[] items = probeTree.getAllItems();
		items[0].doubleClick();
		SWTBotEclipseEditor editor = bot.activeEditor().toTextEditor();
		assertTrue(editor.getText().contains("probe " + items[0].getText() + "\n"));

		// Open a non-stap file and add a probe. This should bring up a dialog
		// asking if the probe should be added to the only open .stp file.
		SWTBotMenu fileMenu = bot.menu("File");
		SWTBotMenu newMenu = fileMenu.menu("New");
		SWTBotMenu projectMenu = newMenu.menu("Other...");
		projectMenu.click();
		SWTBotShell shell = bot.shell("New");
		shell.setFocus();
		shell.bot().text().setText("Untitled Text File");
		bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "General", "Untitled Text File"));
		bot.button("Finish").click();

		items[1].doubleClick();
		shell = bot.shell("Add Probe To Script");
		shell.setFocus();
		bot.button("Yes").click();
		bot.waitUntil(new ShellIsClosed(shell));

		// The editor containing the script should now be in focus.
		editor = bot.activeEditor().toTextEditor();
		assertEquals(scriptName, editor.getTitle());
		assertTrue(editor.getText().contains("probe " + items[0].getText() + "\n"));
		assertTrue(editor.getText().contains("probe " + items[1].getText() + "\n"));

		// Adding a probe while an .stp editor is in focus should always add it
		// to that editor, even if multiple .stp editors are open.
		String scriptName2 = "probeScript2.stp";
		createScript(bot, scriptName2);
		editor = bot.activeEditor().toTextEditor();
		assertEquals(scriptName2, editor.getTitle());
		items[2].doubleClick();
		assertTrue(editor.getText().contains("probe " + items[2].getText() + "\n"));
		editor = bot.editorByTitle(scriptName).toTextEditor();
		assertTrue(!editor.getText().contains("probe " + items[2].getText() + "\n"));

		// Switch to the non-stp editor, and add a probe. A dialog should appear
		// to let the user choose which of the open files to add to.
		editor = bot.editorByTitle("Untitled 1").toTextEditor();
		editor.show();
		items[3].doubleClick();
		shell = bot.shell("Add Probe To Script");
		shell.setFocus();
		SWTBotTable table = bot.table();
		assertTrue(table.containsItem(scriptName));
		assertTrue(table.containsItem(scriptName2));
		table.select(scriptName2);
		bot.button("OK").click();
		bot.waitUntil(new ShellIsClosed(shell));
		editor = bot.activeEditor().toTextEditor();
		assertTrue(!editor.getTitle().equals("Untitled 1"));
		assertTrue(editor.getText().contains("probe " + items[3].getText() + "\n"));
	}

	@Test
	public void testMissingColumns(){
		SWTBotShell shell = prepareScript("missingColumns.stp", null);

		// As soon as the Graphing tab is entered, no regular expression exists & nothing can be run.
		SWTBotCombo combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		assertEquals("", combo.getText());
		assertTrue(!bot.button("Run").isEnabled());
		assertTrue(!bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).isEnabled());
		combo.setText("(1)(2)");
		assertEquals("(1)(2)", combo.getText());
		assertTrue(bot.button("Run").isEnabled());
		assertTrue(bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).isEnabled());

		bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).click();
		setupGraphWithTests("Graph");

		shell.setFocus();
		assertTrue(bot.button("Run").isEnabled());

		// Removing groups from the regex disables graphs that rely on those groups.
		combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		combo.setText("(1)");
		assertTrue(!bot.button("Run").isEnabled());
		combo.setText("(1)(2)(3)");
		assertTrue(bot.button("Run").isEnabled());

		shell.setFocus();
		bot.button("Apply").click();
	}

	@Test
	public void testDeleteBlankRegex(){
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
		SWTBotShell shell2 = bot.shell("Create Graph");
		shell2.setFocus();
		bot.textWithLabel("Title:").setText("Test");
		bot.button("Finish").click();
		bot.waitUntil(new ShellIsClosed(shell2));
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
		SWTBotTable table = bot.table(0);
		SWTBotButton button = bot.button(Messages.SystemTapScriptGraphOptionsTab_RemoveGraphButton);
		assertTrue(!button.isEnabled());
		table.select(0);
		assertTrue(button.isEnabled());
		button.click();
		assertTrue(!button.isEnabled());
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
	public void testGraphScript(){
		String scriptName = "testGraph.stp";
		createScript(bot, scriptName);

		// Write a script
		SWTBotEclipseEditor editor = bot.editorByTitle(scriptName).toTextEditor();
		editor.setText("#!/usr/bin/env stap"
				+ "\nglobal i,j,k"
				+ "\nprobe begin{i=0;j=0;k=0}"
				+ "\nprobe timer.ms(100){printf(\"Value:%d %d\\n\",i,j);i++;j+=2}"
				+ "\nprobe timer.ms(250){printf(\"Other:%d %d\\n\",i,k);k++}"
				+ "\nprobe timer.ms(1000){exit()}");
		editor.save();

		String val0 = "i";
		String val1 = "j";
		String val2 = "k";

		openRunConfigurations(scriptName);
		SWTBotShell shell = bot.shell("Run Configurations");
		shell.setFocus();
		bot.tree().select("SystemTap").contextMenu("New").click();
		bot.textWithLabel("Name:").setText(scriptName);

		// Select the "Graphing" tab.
		SWTBotCTabItem tab = bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_7);
		tab.activate();

		// Enable output graphing.
		bot.checkBox(Messages.SystemTapScriptGraphOptionsTab_2).click();
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

		// Add a graph.
		bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).click();
		setupGraphWithTests("Values");

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

		bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).click();
		setupGraphWithTests("Others");

		// Apply the changes, then close the menu & reopen it to make sure settings were saved.
		shell.setFocus();
		bot.button("Apply").click();
		bot.button("Close").click();
		bot.waitUntil(new ShellIsClosed(shell));
		openRunConfigurations(scriptName);
		shell = bot.shell("Run Configurations");
		shell.setFocus();
		shell.bot().text().setText(scriptName); // Set the filter text to show configs of the current script
		bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "SystemTap", scriptName));
		tab = bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_7);
		tab.activate();

		combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		text = bot.textWithLabel(Messages.SystemTapScriptGraphOptionsTab_sampleOutputLabel);
		SWTBotTable table = bot.table(0);
		assertEquals(3, combo.itemCount());
		assertEquals("Value:(\\d+) (\\d+)", combo.getText());
		assertEquals("Value:1 2", text.getText());
		assertEquals(1, table.rowCount());
		String graphName = GraphFactory.getGraphName("org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.scatterchartbuilder");
		assertTrue(table.containsItem(graphName.concat(":Values")));
		combo.setSelection(1);
		assertEquals("Other:(\\d+) (\\d+)", combo.getText());
		assertEquals("", text.getText());
		assertEquals(1, table.rowCount());
		assertTrue(table.containsItem(graphName.concat(":Others")));

		// If Systemtap is not installed, don't test graph output. Otherwise, do.
		if (!stapInstalled) {
			return;
		}

		bot.button("Run").click();
		bot.waitUntil(new ShellIsClosed(shell));
		SWTBotView console = bot.viewById("org.eclipse.ui.console.ConsoleView");
		console.setFocus();
		bot.waitUntil(new StapHasExited()); // The script should end on its own

		// Give time for the table to be fully constructed
		SWTBotEditor graphEditor = bot.activeEditor();
		bot.waitUntil(new TableHasUpdated(scriptName, 1, 10));
		bot.waitUntil(new TableHasUpdated(scriptName, 2, 4));

		graphEditor.setFocus();
		graphEditor.bot().cTabItem(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_graphSetTitleBase, 1)).activate();
		graphEditor.bot().cTabItem("Data View").activate();
		SWTBotTable dataTable = bot.table(0);
		List<String> colNames = dataTable.columns();
		assertEquals(3, colNames.size());
		assertEquals(val0, colNames.get(1));
		assertEquals(val1, colNames.get(2));
		assertEquals("3", dataTable.cell(3, 1));
		assertEquals("6", dataTable.cell(3, 2));

		graphEditor.bot().cTabItem(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_graphSetTitleBase, 2)).activate();
		graphEditor.bot().cTabItem("Data View").activate();
		dataTable = bot.table(0);
		colNames = dataTable.columns();
		assertEquals(3, colNames.size());
		assertEquals(val0, colNames.get(1));
		assertEquals(val2, colNames.get(2));
		assertEquals("10", dataTable.cell(3, 1));
		assertEquals("3", dataTable.cell(3, 2));

		clearAllTerminated();
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

		SWTBotShell shell2 = bot.shell(Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsTitle);
		shell2.setFocus();
		bot.button("Yes").click();
		bot.waitUntil(new ShellIsClosed(shell2));
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
	public void testLabelledGraphScript(){
		// If Systemtap is not installed, nothing can be graphed, so don't bother performing this test.
		// Once the ability to read in pre-saved chart data is restored, this test can be run with sample data.
		if (!stapInstalled) {
			return;
		}

		String scriptName = "testLabels.stp";
		SWTBotShell shell = prepareScript(scriptName, "#!/usr/bin/env stap"
				 + "\nprobe begin{"
				 + "\nprintf(\"Apples: 2 14 16\\n\");"
				 + "\nprintf(\"1: 1 2 3\\n\");"
				 + "\nprintf(\"Bananas (2): 10 10 10\\n\");"
				 + "\nprintf(\"Cherries: 10 20 30\\n\");"
				 + "\nprintf(\"2: 2 4 6\\n\");"
				 + "\nprintf(\"Apples: 12 5 16\\n\");"
				 + "\nprintf(\"Bananas: 0 1 0\\n\");"
				 + "\nprintf(\"3: 3 6 9\\n\");"
				 + "\nprintf(\"Dates: 12 5 16\\n\");"
				 + "\nprintf(\"Bananas: 2 1 2\\n\");"
				 + "\nprintf(\"4: 4 8 12\\n\");"
				 + "\nprintf(\"Apples: 12 5 16\\n\");"
				 + "\nprintf(\"Bananas (2): 3 1 3\\n\");"
				 + "\nexit();}");
		int numItems = 13;
		int numNumberItems = 4;
		int numCategories = 3;

		// Enter a regex.
		SWTBotCombo combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		SWTBotButton button = bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton);
		assertTrue(!button.isEnabled());
		combo.setText("(.*): (\\d+) (\\d+) (\\d+)");

		// Add bar, pie, and line graphs that use the same column data.
		assertTrue(button.isEnabled());
		button.click();
		String title = "Fruit Info";
		setupGraphGeneral(title, 4, "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.barchartbuilder", false);
		shell.setFocus();
		button.click();
		setupGraphGeneral(title, 4, "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.piechartbuilder", false);
		shell.setFocus();
		button.click();
		setupGraphGeneral(title, 4, "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.linechartbuilder", false);
		shell.setFocus();

		bot.button("Run").click();
		bot.waitUntil(new ShellIsClosed(shell));
		SWTBotView console = bot.viewById("org.eclipse.ui.console.ConsoleView");
		console.setFocus();
		bot.waitUntil(new StapHasExited()); // The script should end on its own

		// Give time for the table to be fully constructed
		SWTBotEditor graphEditor = bot.activeEditor();
		bot.waitUntil(new TableHasUpdated(scriptName, 1, numItems));

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

		clearAllTerminated();
	}

	private void discreteXControlTests(AbstractChartBuilder cb, int numAxisItems) {
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
		assertTrue(!firstButton.isEnabled());
		assertTrue(!leftButton.isEnabled());
		assertTrue(!rightButton.isEnabled());
		assertTrue(!lastButton.isEnabled());

		// Test zooming in. The amount of zoom is arbitrary for this test--just make sure zooming happened.
		SWTBotButton zoomInButton = bot.button(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphDiscreteXControl_ZoomIn);
		SWTBotButton zoomOutButton = bot.button(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphDiscreteXControl_ZoomOut);
		SWTBotButton allButton = bot.button(org.eclipse.linuxtools.systemtap.graphing.ui.widgets.Messages.GraphDiscreteXControl_All);
		assertTrue(zoomInButton.isEnabled());
		assertTrue(!zoomOutButton.isEnabled());
		assertTrue(!allButton.isEnabled());
		zoomInButton.click();
		assertTrue(zoomOutButton.isEnabled());
		assertTrue(allButton.isEnabled());

		// By default, zooming in should zoom in on the end of the axis (newest data).
		range = axis.getRange();
		assertTrue(range.upper == numAxisItems - 1 && range.lower > 0 && cb.getScale() < scale && cb.getScroll() == 1.0);

		// Left scrolling should now be enabled.
		assertTrue(firstButton.isEnabled());
		assertTrue(leftButton.isEnabled());
		assertTrue(!rightButton.isEnabled());
		assertTrue(!lastButton.isEnabled());

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
		assertTrue(!zoomOutButton.isEnabled());
		assertTrue(!allButton.isEnabled());
		assertTrue(!firstButton.isEnabled());
		assertTrue(!leftButton.isEnabled());
		assertTrue(!rightButton.isEnabled());
		assertTrue(!lastButton.isEnabled());

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
		assertTrue(!rightButton.isEnabled());
		assertTrue(!lastButton.isEnabled());

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
			assertTrue(range.lower == range.upper);
		}
		assertTrue(axis.getRange().lower == 0);
		for (int i = 0; i < numAxisItems; i++) {
			if (axis.getRange().upper == numAxisItems - 1) {
				break;
			}
			rightButton.click();
			assertTrue(axis.getRange().upper > range.upper);
			range = axis.getRange();
			assertTrue(range.lower == range.upper);
		}
		assertTrue(axis.getRange().upper == numAxisItems - 1);

		firstButton.click();
		assertTrue(axis.getRange().lower == 0);
		assertTrue(!firstButton.isEnabled());
		assertTrue(!leftButton.isEnabled());
		assertTrue(rightButton.isEnabled());
		assertTrue(lastButton.isEnabled());

		lastButton.click();
		assertTrue(axis.getRange().upper == numAxisItems - 1);
		assertTrue(firstButton.isEnabled());
		assertTrue(leftButton.isEnabled());
		assertTrue(!rightButton.isEnabled());
		assertTrue(!lastButton.isEnabled());
	}

	private double getAxisScale(AbstractChartBuilder cb, boolean isXAxis) {
		return isXAxis ? cb.getScale() : cb.getScaleY();
	}

	private double getAxisScroll(AbstractChartBuilder cb, boolean isXAxis) {
		return isXAxis ? cb.getScroll() : cb.getScrollY();
	}

	private void continuousControlTests(AbstractChartBuilder cb, boolean isXAxis) {
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
		assertTrue(scale == 1.0);
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
		assertTrue(scrollBar.getThumb() == thumb);

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
	public void testGraphTooltips(){
		// If Systemtap is not installed, nothing can be graphed, so don't bother performing this test.
		// Once the ability to read in pre-saved chart data is restored, this test can be run with sample data.
		if (!stapInstalled) {
			return;
		}

		String scriptName = "testGraphTooltips.stp";
		SWTBotShell shell = prepareScript(scriptName, "#!/usr/bin/env stap"
				 + "\nglobal y"
				 + "\nprobe begin{y=5}"
				 + "\nprobe timer.ms(1000){printf(\"%d\\n\",y);y++}"
				 + "\nprobe timer.ms(5000){exit()}");

		// Enter a regex.
		SWTBotCombo combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		SWTBotButton button = bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton);
		assertTrue(!button.isEnabled());
		combo.setText("(\\d+)");

		// Add bar, pie, and line graphs that use the same column data.
		assertTrue(button.isEnabled());
		button.click();
		String title = "Info";
		setupGraphGeneral(title, 1, "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.linechartbuilder", true);
		shell.setFocus();
		button.click();
		setupGraphGeneral(title, 1, "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.barchartbuilder", true);
		shell.setFocus();

		bot.button("Run").click();
		bot.waitUntil(new ShellIsClosed(shell));
		SWTBotView console = bot.viewById("org.eclipse.ui.console.ConsoleView");
		console.setFocus();

		// Perform mouse hover tests on graphs as they are being updated
		SWTBotEditor graphEditor = TestCreateSystemtapScript.bot.editorByTitle(scriptName.concat(" Graphs"));
		graphEditor.setFocus();
		graphEditor.bot().cTabItem("Info - Bar Graph").activate();
		final Matcher<AbstractChartBuilder> matcher = widgetOfType(AbstractChartBuilder.class);
		AbstractChartBuilder cb = bot.widget(matcher);
		bot.waitUntil(new ChartHasUpdated(cb.getChart(), 1));
		String tooltipFormat = "{0}: {1}";
		checkTooltipAtDataPoint(cb, 0, 0, new Point(0, 20), MessageFormat.format(tooltipFormat, "Column 1", "5"), true);

		graphEditor.bot().cTabItem("Info - Line Graph").activate();
		cb = bot.widget(matcher);
		bot.waitUntil(new ChartHasUpdated(cb.getChart(), 2));
		tooltipFormat = "Series: {0}\nx: {1}\ny: {2}";
		checkTooltipAtDataPoint(cb, 0, 1, null, MessageFormat.format(tooltipFormat,	"Column 1", "2", "6"), true);

		// The tooltip should disappear when a point moves away from the mouse, without need for mouse movement.
		bot.waitUntil(new ChartHasUpdated(cb.getChart(), -1));
		checkTooltipAtDataPoint(cb, 0, -1, null, MessageFormat.format(tooltipFormat, "Column 1", "2", "6"), false);

		ScriptConsole.stopAll();
		bot.waitUntil(new StapHasExited());
		clearAllTerminated();
	}

	/**
	 * May move the mouse to a desired data point on a chart and test for the tooltip that appears.
	 * @param cb The AbstractChartBuilder containing the chart to test.
	 * @param series The index of the data series to hover over.
	 * @param dataPoint The data point of the series to move the mouse to. Set this to -1
	 * or less if the mouse should stay where it is.
	 * @param adjustment Move the mouse's x & y coordinates by the values of this Point,
	 * or set this to <code>null</code> to make no adjustment.
	 * @param expectedTooltip The expected contents of the tooltip.
	 * @param shellShouldExist Set to <code>false</code> if the tooltip should not be found.
	 */
	private void checkTooltipAtDataPoint(final AbstractChartBuilder cb, final int series,
			final int dataPoint, final Point adjustment, final String expectedTooltip,
			final boolean shellShouldExist) {
		if (dataPoint >= 0) {
			UIThreadRunnable.syncExec(new VoidResult() {

				@Override
				public void run() {
					Event event = new Event();
					event.type = SWT.MouseMove;
					Point mousePoint = cb.getChart().getPlotArea().toDisplay(
							cb.getChart().getSeriesSet().getSeries()[0].getPixelCoordinates(dataPoint));
					event.x = mousePoint.x + (adjustment != null ? adjustment.x : 0);
					event.y = mousePoint.y + (adjustment != null ? adjustment.y : 0);
					bot.getDisplay().post(event);
				}
			});
		}

		bot.sleep(100); // Give some time for the tooltip to appear/change
		UIThreadRunnable.syncExec(new VoidResult() {

			@Override
			public void run() {
				for (SWTBotShell bshell : bot.shells()) {
					Control[] children = bshell.widget.getChildren();
					if (children.length == 1 && children[0] instanceof Text && expectedTooltip.equals(((Text) children[0]).getText())) {
						if (!shellShouldExist) {
							throw new AssertionError("Did not expect to find this tooltip, but found it: " + expectedTooltip);
						}
						return;
					}
				}
				if (shellShouldExist) {
					throw new AssertionError("Didn't find the expected tooltip: " + expectedTooltip);
				}
			}
		});
	}

	private void openRunConfigurations(String scriptName) {
		// Focus on project explorer view.
		bot.viewByTitle("Project Explorer").setFocus();
		SWTBotTree treeBot = bot.tree();
		treeBot.expandNode(SYSTEMTAP_PROJECT_NAME).expand().select(scriptName);
		MenuItem menu = ContextMenuHelper.contextMenu(treeBot, "Run As", "Run Configurations...");
		click(menu);
	}

	private void setupGraphWithTests(String title) {
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
		assertTrue(!bot.button("Finish").isEnabled()); // Don't allow duplicate selections
		comboY1.setSelection(2);
		bot.button("Finish").click();

		bot.waitUntil(new ShellIsClosed(shell));
	}

	private void setupGraphGeneral(String title, int numItems, String graphID, boolean useRowNum) {
		int offset = useRowNum ? 0 : 1;
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
		bot.waitUntil(new ShellIsClosed(shell));
	}

	/**
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=344484
	 * @param currSelection The index of the radiobutton to deselect
	 */
	private void deselectDefaultSelection(final int currSelection) {
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
}
