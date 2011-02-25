/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.utils;

import java.util.StringTokenizer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * Project Explorer abstraction for SWTBot tests.
 *
 */
public class ProjectExplorer {
	
	private static SWTWorkbenchBot bot = new SWTWorkbenchBot();
	
	/**
	 * Opens the Project Explorer view.
	 */
	public static void openView() throws Exception {
		bot.menu("Window").menu("Show View").menu("Project Explorer").click();
	}
	
	/**
	 * Assumes Project Explorer view is shown.
	 * 
	 * @return The tree of the Project Explorer view
	 */
	public static SWTBotTree getTree() {
		SWTBotView projectExplorer = bot.viewByTitle("Project Explorer");
		projectExplorer.show();
		Composite projectExplorerComposite = (Composite) projectExplorer.getWidget();
		Tree swtTree = (Tree) bot.widget(WidgetMatcherFactory.widgetOfType(Tree.class), projectExplorerComposite);
		return new SWTBotTree(swtTree);
	}
	
	/**
	 * @param projectItem The tree item corresponding to the project.
	 * @param name
	 * 			name of an item
	 * @return the project item pertaining to the project
	 */
	public static SWTBotTreeItem getProjectItem(SWTBotTreeItem projectItem,
			String file) {
		for (SWTBotTreeItem item : projectItem.getItems()) {
			String itemText = item.getText();
			if (itemText.contains(file)) {
				if (itemText.contains(" ")) {
					StringTokenizer tok = new StringTokenizer(itemText, " ");
					String name = tok.nextToken();
					if (file.equals(name))
						return item;
				} else if (itemText.equals(file)) {
					return item;
				}
			}	
		}
		return null;
	}
	
	/**
	 * Expand the given project (optionally stripping off the team provider bits)
	 * 
	 * @param projectName
	 * @param teamProviderString
	 * @return
	 */
	public static SWTBotTreeItem expandProject(SWTBotTree projectExplorerTree, String projectName, String teamProviderString) {
		String itemName;
		for (SWTBotTreeItem item: projectExplorerTree.getAllItems()) {
			itemName = item.getText();
			if (itemName.contains(projectName)) { // may also contain repo info
				if (itemName.contains(teamProviderString)) {
					return projectExplorerTree.expandNode(projectName + " "
							+ teamProviderString);
				} else {
					return projectExplorerTree.expandNode(projectName);
				}
			}
		}
		// nothing appropriate found
		return null;
	}
}
