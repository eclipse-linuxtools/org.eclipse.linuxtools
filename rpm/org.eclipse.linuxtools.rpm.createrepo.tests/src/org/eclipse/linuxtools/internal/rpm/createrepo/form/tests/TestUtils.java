/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *     Andrew Ferrazzutti - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.form.tests;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForWidget;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.widgetIsEnabled;
import static org.junit.Assert.assertEquals;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotMultiPageEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class TestUtils {

	public static class NodeAvailableAndSelect extends DefaultCondition {

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
		public NodeAvailableAndSelect(SWTBotTree tree, String parent, String node) {
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

	/**
	 * Open the resource perspective, if it's not already opened. Also, go into the project tree.
	 */
	public static void openResourcePerspective(SWTWorkbenchBot bot) {
		try {
			// Check if the required views are already opened
			bot.viewByTitle(ICreaterepoTestConstants.NAVIGATOR);
		} catch (WidgetNotFoundException e) {
			// Not yet opened: turn on the resource perspective
			bot.menu(ICreaterepoTestConstants.WINDOW).menu(ICreaterepoTestConstants.SHOW_VIEW)
			.menu(ICreaterepoTestConstants.OTHER).click();
			SWTBotShell shell = bot.shell(ICreaterepoTestConstants.SHOW_VIEW);
			shell.activate();
			bot.text().setText(ICreaterepoTestConstants.NAVIGATOR);
			bot.waitUntil(new NodeAvailableAndSelect(bot.tree(),
					ICreaterepoTestConstants.GENERAL_NODE, ICreaterepoTestConstants.NAVIGATOR));
			bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		}
	}

	/**
	 * Enter the project folder so as to avoid expanding trees later
	 */
	public static SWTBotView enterProjectFolder(SWTWorkbenchBot bot) {
		SWTBotView navigator = bot.viewByTitle(ICreaterepoTestConstants.NAVIGATOR);
		navigator.setFocus();
		navigator.bot().tree().select(ICreaterepoTestConstants.PROJECT_NAME).
			contextMenu(ICreaterepoTestConstants.GO_INTO).click();
		bot.waitUntil(waitForWidget(WidgetMatcherFactory.withText(
				ICreaterepoTestConstants.PROJECT_NAME), navigator.getWidget()));
		return navigator;
	}

	/**
	 * Exit from the project tree.
	 */
	public static void exitProjectFolder(SWTWorkbenchBot bot, SWTBotView navigator) {
		SWTBotToolbarButton forwardButton = navigator.toolbarPushButton(ICreaterepoTestConstants.GO_FORWARD);
		navigator.toolbarPushButton(ICreaterepoTestConstants.GO_BACK).click();
		bot.waitUntil(widgetIsEnabled(forwardButton));
	}

	/**
	 * Open the property page, and activate its shell.
	 */
	public static SWTBotShell openPropertyPage(SWTWorkbenchBot bot, SWTBotView navigator) {
		navigator.show();
		// select the .repo file from the package explorer and open its properties
		SWTBotTree botTree = navigator.bot().tree();
		botTree.select(ICreaterepoTestConstants.REPO_NAME)
			.contextMenu(ICreaterepoTestConstants.PROPERTIES).click();
		// get a handle of the property shell
		SWTBotShell propertyShell = bot.shell(String.format(ICreaterepoTestConstants.PROPERTIES_SHELL,
				ICreaterepoTestConstants.REPO_NAME));
		propertyShell.activate();
		return propertyShell;
	}

	public static SWTBotMultiPageEditor openRepoFile(SWTWorkbenchBot bot, SWTBotView navigator) {
		// open the package explorer view
		SWTBotTree botTree = navigator.bot().tree();
		botTree.select(ICreaterepoTestConstants.REPO_NAME)
			.contextMenu(ICreaterepoTestConstants.OPEN).click();
		// get a handle on the multipage editor that was opened
		SWTBotMultiPageEditor editor = bot.multipageEditorByTitle(ICreaterepoTestConstants.REPO_NAME);
		editor.show();
		// 3 = repository form page, metadata form page, repo file
		assertEquals(3, editor.getPageCount());
		return editor;
	}
}
