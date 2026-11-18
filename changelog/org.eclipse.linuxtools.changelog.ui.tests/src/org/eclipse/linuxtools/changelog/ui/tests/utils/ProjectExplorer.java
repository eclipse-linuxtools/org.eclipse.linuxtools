/*******************************************************************************
 * Copyright (c) 2010, 2026 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.utils;

import java.util.StringTokenizer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * Project Explorer abstraction for SWTBot tests.
 */
public class ProjectExplorer {

    private static SWTWorkbenchBot bot = new SWTWorkbenchBot();

    /**
     * Opens the Project Explorer view.
     */
	public static void openView() {
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
        Tree swtTree = bot.widget(WidgetMatcherFactory.widgetOfType(Tree.class), projectExplorerComposite);
        return new SWTBotTree(swtTree);
    }

    /**
     * @param projectItem The tree item corresponding to the project.
     * @param name
     *             name of an item
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
     * Expand the given project regardless of any label decoration (e.g. the
     * repository and branch EGit appends to shared projects).
     *
     * @param projectExplorerTree The Project Explorer tree.
     * @param projectName The plain project name.
     * @return The expanded project item, or {@code null} if there is none.
     */
    public static SWTBotTreeItem expandProject(SWTBotTree projectExplorerTree, String projectName) {
        for (SWTBotTreeItem item : projectExplorerTree.getAllItems()) {
            String itemName = item.getText();
            if (itemName.equals(projectName) || itemName.startsWith(projectName + " ")) {
                return expandIfCollapsed(item);
            }
        }
        return null;
    }

    /**
     * Expands the given tree item unless it is already expanded, in which case
     * {@link SWTBotTreeItem#expand()} would do nothing but log a warning.
     *
     * @param item The tree item to expand.
     * @return The given tree item.
     */
    private static SWTBotTreeItem expandIfCollapsed(SWTBotTreeItem item) {
        if (!item.isExpanded()) {
            item.expand();
        }
        return item;
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
                    return expandIfCollapsed(projectExplorerTree.getTreeItem(projectName + " "
                            + teamProviderString));
                } else {
                    return expandIfCollapsed(projectExplorerTree.getTreeItem(projectName));
                }
            }
        }
        // nothing appropriate found
        return null;
    }
}
