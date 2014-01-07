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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher.Messages;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.ScatterChartBuilder;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.GraphFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

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

	private static class NodeAvaiable extends DefaultCondition {

		private String node;
		private SWTBotTreeItem parent;

		NodeAvaiable(SWTBotTreeItem parent, String node){
			this.node = node;
			this.parent = parent;
		}

		@Override
		public boolean test() {
			return this.parent.getNodes().contains(node);
		}

		@Override
		public String getFailureMessage() {
			return "Timed out waiting for " + node; //$NON-NLS-1$
		}
	}

	private static class StapHasExited extends DefaultCondition{

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

	@BeforeClass
	public static void beforeClass() {
		bot = new SWTWorkbenchBot();
		stapInstalled = true;

		// Dismiss "Systemtap not installed" dialog(s) if present.
		try {
			SWTBotShell shell = bot.shell("Cannot Run Systemtap").activate();
			stapInstalled = false;
			shell.close();

			shell = bot.shell("Cannot Run Systemtap").activate();
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
		shell.activate();

		SWTBotTreeItem node = bot.tree().expandNode("General").select("Project");
		assertNotNull(node);

		bot.button("Next >").click();

		bot.textWithLabel("Project name:").setText(SYSTEMTAP_PROJECT_NAME);
		bot.button("Finish").click();
		bot.waitUntil(new ShellIsClosed(shell));
	}

	public static void createScript(SWTWorkbenchBot bot, String scriptName) {
		SWTBotMenu fileMenu = bot.menu("File");
		SWTBotMenu newMenu = fileMenu.menu("New");
		SWTBotMenu projectMenu = newMenu.menu("Other...");
		projectMenu.click();

		SWTBotShell shell = bot.shell("New");
		shell.activate();

		SWTBotTreeItem node = bot.tree().expandNode("Systemtap");
		assertNotNull(node);
		bot.waitUntil(new NodeAvaiable(node, "Systemtap Script"));
		node.select("Systemtap Script");

		bot.button("Next >").click();

		SWTBotText text = bot.textWithLabel("Script Name:").setText(scriptName);
		assertEquals(scriptName, text.getText());

		text = bot.textWithLabel("Project:").setText(SYSTEMTAP_PROJECT_NAME);
		assertEquals(SYSTEMTAP_PROJECT_NAME, text.getText());

		bot.button("Finish").click();
		bot.waitUntil(new ShellIsClosed(shell));

		assertEquals(scriptName, bot.activeEditor().getTitle());
	}

	@Test
	public void testCreateScript(){
		String scriptName = "testScript.stp";
		createScript(bot, scriptName);

		// Write a script
		SWTBotEclipseEditor editor = bot.editorByTitle(scriptName).toTextEditor();
		editor.typeText(0, editor.getText().length(), "\nprobe begin{log(\"began");
		editor.typeText(0, editor.getText().length() - 1, "); exit(");
		editor.typeText(0, editor.getText().length(), "}");
		editor.save();

		openRunConfigurations(scriptName);
		SWTBotShell shell = bot.shell("Run Configurations");
		shell.setFocus();

		SWTBotTree runConfigurationsTree = bot.tree();
		runConfigurationsTree.select("SystemTap").contextMenu("New").click();

		if (stapInstalled) {
			bot.button("Run").click();
			bot.waitUntil(new ShellIsClosed(shell));

			SWTBotView console = bot.viewById("org.eclipse.ui.console.ConsoleView");
			console.setFocus();
			assertTrue(console.bot().label().getText().contains(scriptName));
			bot.waitUntil(new StapHasExited(), 10000); // The script should end on its own
		} else {
			bot.button("Close").click();
			bot.waitUntil(new ShellIsClosed(shell));
		}
	}

	@Test
	public void testMissingColumns(){
		String scriptName = "missingColumns.stp";
		createScript(bot, scriptName);

		openRunConfigurations(scriptName);
		SWTBotShell shell = bot.shell("Run Configurations");
		shell.setFocus();

		SWTBotTree runConfigurationsTree = bot.tree();
		runConfigurationsTree.select("SystemTap").contextMenu("New").click();

		// Select the "Graphing" tab.
		SWTBotCTabItem tab = bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_7);
		tab.activate();

		// Enable output graphing.
		bot.checkBox(Messages.SystemTapScriptGraphOptionsTab_2).click();

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
		setupGraph("Graph");

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
		bot.button("Close").click();
		bot.waitUntil(new ShellIsClosed(shell));
	}

	@Test
	public void testDeleteBlankRegex(){
		createScript(bot, "blank.stp");
		openRunConfigurations("blank.stp");
		SWTBotShell shell = bot.shell("Run Configurations");
		shell.setFocus();

		SWTBotTree runConfigurationsTree = bot.tree();
		runConfigurationsTree.select("SystemTap").contextMenu("New").click();

		// Select the "Graphing" tab.
		SWTBotCTabItem tab = bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_7);
		tab.activate();

		// Enable output graphing.
		bot.checkBox(Messages.SystemTapScriptGraphOptionsTab_2).click();

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

		bot.button("Close").click();
		shell2 = bot.shell("Save Changes");
		shell2.setFocus();
		bot.button("No").click();
		bot.waitUntil(new ShellIsClosed(shell));
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

		SWTBotTree runConfigurationsTree = bot.tree();
		runConfigurationsTree.select("SystemTap").contextMenu("New").click();
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
		setupGraph("Values");

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
		setupGraph("Others");

		// Apply the changes, then close the menu & reopen it to make sure settings were saved.
		shell.setFocus();
		bot.button("Apply").click();
		bot.button("Close").click();
		bot.waitUntil(new ShellIsClosed(shell));
		openRunConfigurations(scriptName);
		shell = bot.shell("Run Configurations");
		shell.setFocus();
		bot.tree().expandNode("SystemTap").select(scriptName);
		tab = bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_7);
		tab.activate();

		combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		text = bot.textWithLabel(Messages.SystemTapScriptGraphOptionsTab_sampleOutputLabel);
		SWTBotTable table = bot.table(0);
		assertEquals(3, combo.itemCount());
		assertEquals("Value:(\\d+) (\\d+)", combo.getText());
		assertEquals("Value:1 2", text.getText());
		assertEquals(1, table.rowCount());
		assertTrue(table.containsItem(GraphFactory.getGraphName(ScatterChartBuilder.ID) + ":Values"));
		combo.setSelection(1);
		assertEquals("Other:(\\d+) (\\d+)", combo.getText());
		assertEquals("", text.getText());
		assertEquals(1, table.rowCount());
		assertTrue(table.containsItem(GraphFactory.getGraphName(ScatterChartBuilder.ID) + ":Others"));

		// If Systemtap is not installed, don't test graph output. Otherwise, do.
		if (!stapInstalled) {
			bot.button("Close").click();
			bot.waitUntil(new ShellIsClosed(shell));
			return;
		}

		bot.button("Run").click();
		bot.waitUntil(new ShellIsClosed(shell));
		bot.sleep(2500); // Let the script run for a moment
		SWTBotView console = bot.viewById("org.eclipse.ui.console.ConsoleView");
		console.setFocus();
		bot.waitUntil(new StapHasExited()); // The script should end on its own

		bot.sleep(1000); // Give time for the table to be fully constructed
		SWTBotEditor graphDisplay = bot.activeEditor();
		graphDisplay.setFocus();
		graphDisplay.bot().cTabItem(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_graphSetTitleBase, 1)).activate();
		graphDisplay.bot().cTabItem("Data View").activate();
		SWTBotTable dataTable = bot.table(0);
		List<String> colNames = dataTable.columns();
		assertEquals(3, colNames.size());
		assertEquals(val0, colNames.get(1));
		assertEquals(val1, colNames.get(2));
		assertEquals("3", dataTable.cell(3, 1));
		assertEquals("6", dataTable.cell(3, 2));

		graphDisplay.bot().cTabItem(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_graphSetTitleBase, 2)).activate();
		graphDisplay.bot().cTabItem("Data View").activate();
		dataTable = bot.table(0);
		colNames = dataTable.columns();
		assertEquals(3, colNames.size());
		assertEquals(val0, colNames.get(1));
		assertEquals(val2, colNames.get(2));
		assertEquals("10", dataTable.cell(3, 1));
		assertEquals("3", dataTable.cell(3, 2));
	}

	@Test
	public void testGenerateFromPrintf() {
		String scriptName = "testGenerates.stp";
		createScript(bot, scriptName);

		// Write a script
		SWTBotEclipseEditor editor = bot.editorByTitle(scriptName).toTextEditor();
		editor.setText("#!/usr/bin/env stap"
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
		editor.save();

		openRunConfigurations(scriptName);
		SWTBotShell shell = bot.shell("Run Configurations");
		shell.setFocus();
		SWTBotTree runConfigurationsTree = bot.tree();
		runConfigurationsTree.select("SystemTap").contextMenu("New").click();

		// Select the "Graphing" tab.
		SWTBotCTabItem tab = bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_7);
		tab.activate();

		// Generate regexs.
		bot.checkBox(Messages.SystemTapScriptGraphOptionsTab_2).click();
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
		bot.button("Close").click();
	}

	private void openRunConfigurations(String scriptName) {
		// Focus on project explorer view.
		bot.viewByTitle("Project Explorer").setFocus();
		bot.activeShell();
		SWTBotTree treeBot = bot.tree();
		treeBot.setFocus();
		SWTBotTreeItem node = treeBot.expandNode(SYSTEMTAP_PROJECT_NAME);
		bot.waitUntil(new NodeAvaiable(node, scriptName));

		treeBot.expandNode(SYSTEMTAP_PROJECT_NAME).expand().select(scriptName);

		MenuItem menu = ContextMenuHelper.contextMenu(treeBot, "Run As", "Run Configurations...");
		click(menu);
	}

	private void setupGraph(String title) {
		SWTBotShell shell = bot.shell("Create Graph");
		shell.setFocus();

		SWTBotText text = bot.textWithLabel("Title:");
		text.setText(title);
		assertEquals(title, text.getText());

		SWTBotCombo combo_x = bot.comboBoxWithLabel("X Series:");
		assertEquals(3, combo_x.itemCount()); // X Series includes "Row Num" as a selection
		SWTBotCombo combo_y0 = bot.comboBoxWithLabel("Y Series 0:");
		assertEquals(2, combo_y0.itemCount()); // Y Series 0 only includes series entries
		combo_y0.setSelection(0);
		SWTBotCombo combo_y1 = bot.comboBoxWithLabel("Y Series 1:");
		assertEquals(3, combo_y1.itemCount()); // Y Series (i>0) has extra "NA" option as first entry
		combo_y1.setSelection(1);
		assertTrue(!bot.button("Finish").isEnabled()); // Don't allow duplicate selections
		combo_y1.setSelection(2);
		bot.button("Finish").click();

		bot.waitUntil(new ShellIsClosed(shell));
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
