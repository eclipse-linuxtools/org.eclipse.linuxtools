/*******************************************************************************
 * Copyright (c) 2010, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.utils;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * Expands the <code>parent</code> node, and returns true if
 * and only if tree items of the expanded parent node contains
 * the item given by <code>treeItemName</code>.
 *
 */
public class TreeItemAppearsCondition implements ICondition {

    private SWTBot bot;
    private SWTBotTreeItem treeItem;
    private String itemName;
    private String parent;

    public TreeItemAppearsCondition(String parent, String treeItemName) {
        this.itemName = treeItemName;
        this.parent = parent;
    }

    @Override
    public boolean test() {
        for (SWTBotTreeItem i : treeItem.getItems()) {
            if (i.getText().contains(itemName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(SWTBot bot) {
        this.bot = bot;
        treeItem = this.bot.tree().expandNode(parent);
    }

    @Override
    public String getFailureMessage() {
        return null;
    }

}
