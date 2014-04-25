/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.utils;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * Condition to wait until a certain item appears in the
 * Project Explorer tree.
 *
 */
public class ProjectExplorerTreeItemAppearsCondition implements ICondition {

    private SWTBotTreeItem treeItem;
    private String itemName;
    private String parent;
    private String teamDirt;
    private SWTBotTree projectExplorerTree;

    public ProjectExplorerTreeItemAppearsCondition(SWTBotTree projectExplorerTree, String parent, String teamDirt, String treeItemName) {
        this.itemName = treeItemName;
        this.parent = parent;
        this.teamDirt = teamDirt;
        this.projectExplorerTree = projectExplorerTree;
    }

    @Override
    public boolean test() {
        treeItem = ProjectExplorer.expandProject(projectExplorerTree, parent, teamDirt);
        for (SWTBotTreeItem i : treeItem.getItems()) {
            if (i.getText().contains(itemName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(SWTBot bot) {
        // no initialization
    }

    @Override
    public String getFailureMessage() {
        return null;
    }

}
