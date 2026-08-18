/*******************************************************************************
 * Copyright (c) 2016, 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.testutils.swt;

import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * Utility class for SWT
 */
public class SWTUtils {

	/** the label of the stub shown by the Docker Explorer while a node loads. */
	public static final String LOADING_STUB_TEXT = "Loading..."; //$NON-NLS-1$

	/**
	 * Executes <strong>synchronously</strong> the given {@link Runnable} in the
	 * default Display. The given {@link Runnable} is ran into a rapping
	 * {@link Runnable} that will catch the {@link AssertionError} that may
	 * be raised during an assertion.
	 *
	 * @param runnable
	 *            the {@link Runnable} to execute
	 * @throws AssertionError
	 *             if an assertion failed.
	 * @throws SWTException
	 *             if an {@link SWTException} occurred
	 */
	public static void syncAssert(final Runnable runnable) throws SWTException {
		final Queue<AssertionError> failure = new ArrayBlockingQueue<>(1);
		final Queue<SWTException> swtException = new ArrayBlockingQueue<>(1);
		Display.getDefault().syncExec(() -> {
			try {
				runnable.run();
			} catch (AssertionError e1) {
				failure.add(e1);
			} catch (SWTException e2) {
				swtException.add(e2);
			}
		});
		if (!failure.isEmpty()) {
			throw failure.poll();
		}
		if (!swtException.isEmpty()) {
			throw swtException.poll();
		}
	}

	/**
	 * Executes the given {@link Runnable} <strong>asynchronously</strong> in
	 * the default {@link Display} and waits until all jobs are done before
	 * completing.
	 *
	 * @param runnable
	 *            the {@link Runnable} to execute
	 * @param waitForJobsToComplete
	 *            boolean flag to indicate if the method should wait for all
	 *            jobs to complete before finishing
	 * @throws InterruptedException
	 */
	public static void asyncExec(final Runnable runnable, final boolean waitForJobsToComplete) {
		final Queue<AssertionError> failure = new ArrayBlockingQueue<>(1);
		final Queue<SWTException> swtException = new ArrayBlockingQueue<>(1);
		Display.getDefault().asyncExec(() -> {
			try {
				runnable.run();
			} catch (AssertionError e1) {
				failure.add(e1);
			} catch (SWTException e2) {
				swtException.add(e2);
			}
		});
		if (waitForJobsToComplete) {
			waitForJobsToComplete();
		}
		if (!failure.isEmpty()) {
			throw failure.poll();
		}
		if (!swtException.isEmpty()) {
			throw swtException.poll();
		}
	}

	/**
	 * Waits for all {@link Job} to complete.
	 *
	 * @throws InterruptedException
	 */
	public static void waitForJobsToComplete() {
		wait(200, TimeUnit.MILLISECONDS);
		while (!Job.getJobManager().isIdle()) {
			wait(200, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Waits for the shell with the given title (eg: a wizard dialog) to show up
	 * and returns a bot scoped to it, so that widget lookups do not depend on
	 * which shell happens to be active.
	 *
	 * @param bot
	 *            the workbench bot
	 * @param title
	 *            the title of the shell to wait for
	 * @return a {@link SWTBot} restricted to that shell
	 */
	public static SWTBot waitForShell(final SWTWorkbenchBot bot, final String title) {
		final SWTBotShell shell = bot.shell(title);
		shell.activate();
		return shell.bot();
	}

	/**
	 * @param viewBot
	 *            the {@link SWTBotView} containing the {@link Tree} to traverse
	 * @param paths
	 *            the node path in the {@link SWTBotTree} associated with the
	 *            given {@link SWTBotView}
	 * @return the first {@link SWTBotTreeItem} matching the given node names
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotView viewBot, final String... paths) {
		final SWTBotTree tree = viewBot.bot().tree();
		return getTreeItem(tree.getAllItems(), paths);
	}

	/**
	 *
	 * @param parentTreeItem
	 *            the parent tree item from which to start
	 * @param paths
	 *            the relative path to the item to return
	 * @return the {@link SWTBotTreeItem} that matches the given path from the
	 *         given parent tree item
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotTreeItem parentTreeItem, final String... paths) {
		if (paths.length == 1) {
			return getTreeItem(parentTreeItem, paths[0]);
		}
		final String[] remainingPaths = new String[paths.length - 1];
		System.arraycopy(paths, 1, remainingPaths, 0, paths.length - 1);
		return getTreeItem(getTreeItem(parentTreeItem, paths[0]), remainingPaths);
	}

	/**
	 * Returns the first child node in the given parent tree item whose text
	 * matches (ie, begins with) the given path argument.
	 *
	 * @param parentTree
	 *            the parent tree item
	 * @param path
	 *            the text of the node that should match
	 * @return the first matching node or <code>null</code> if none could be
	 *         found
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotTree parentTree, final String path) {
		for (SWTBotTreeItem child : parentTree.getAllItems()) {
			if (child.getText().startsWith(path)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Returns the first child node in the given parent tree item whose text
	 * matches (ie, begins with) the given path argument.
	 *
	 * @param parentTreeItem
	 *            the parent tree item
	 * @param path
	 *            the text of the node that should match
	 * @return the first matching node or <code>null</code> if none could be
	 *         found
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotTreeItem parentTreeItem, final String path) {
		for (SWTBotTreeItem child : parentTreeItem.getItems()) {
			if (child.getText().startsWith(path)) {
				return child;
			}
		}
		return null;
	}

	private static SWTBotTreeItem getTreeItem(final SWTBotTreeItem[] treeItems, final String[] paths) {
		final SWTBotTreeItem swtBotTreeItem = Stream.of(treeItems).filter(item -> item.getText().startsWith(paths[0]))
				.findFirst().orElseThrow(() -> new RuntimeException("No item matching '" + paths[0]
						+ "', only available items: " + Stream.of(treeItems).map(item -> "'" + item.getText() + "'")
								.collect(Collectors.joining(", "))));
		if (paths.length > 1) {
			// let SWTBot drive the expansion from the test thread: it posts the
			// Expand notification asynchronously to the UI thread, so calling it
			// from within a syncExec would mark the item expanded before the viewer
			// had a chance to create its children.
			swtBotTreeItem.expand();
			final String[] remainingPath = new String[paths.length - 1];
			System.arraycopy(paths, 1, remainingPath, 0, remainingPath.length);
			return getTreeItem(getLoadedItems(swtBotTreeItem), remainingPath);
		}
		return swtBotTreeItem;
	}

	/**
	 * Returns the children of the given tree item once they are loaded. The
	 * Docker Explorer content provider answers a "Loading..." stub for a node
	 * whose children are still being fetched by a background job, and replaces
	 * it once the job is done, so wait for that to happen before looking at the
	 * children.
	 *
	 * @param parentTreeItem
	 *            the expanded parent tree item
	 * @return its children, without any "Loading..." stub
	 */
	public static SWTBotTreeItem[] getLoadedItems(final SWTBotTreeItem parentTreeItem) {
		final SWTBot bot = new SWTBot();
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() {
				return Stream.of(parentTreeItem.getItems())
						.noneMatch(item -> item.getText().startsWith(LOADING_STUB_TEXT));
			}

			@Override
			public String getFailureMessage() {
				return "Children of '" + parentTreeItem.getText() + "' were still loading";
			}
		}, TimeUnit.SECONDS.toMillis(30));
		return parentTreeItem.getItems();
	}

	public static SWTBotTableItem getListItem(final SWTBotTable table, final String name) {
		return Stream.iterate(0, i -> i + 1).limit(table.rowCount()).map(rowNumber -> table.getTableItem(rowNumber))
				.filter(rowItem -> Stream.iterate(0, j -> j + 1).limit(table.columnCount())
						.map(colNum -> rowItem.getText(colNum)).anyMatch(colValue -> colValue.contains(name))).findFirst().orElse(null);
	}

	/**
	 * Waits for the given duration
	 *
	 * @param duration
	 *            the duration
	 * @param unit
	 *            the duration unit
	 */
	public static void wait(final int duration, final TimeUnit unit) {
		try {
			Thread.sleep(unit.toMillis(duration));
		} catch (InterruptedException e) {
			fail("Failed to wait for a " + unit.toMillis(duration) + "ms", e);
		}
	}

	/**
	 * Selects <strong> all child items</strong> in the given
	 * <code>parentTreeItem</code> whose labels match the given
	 * <code>items</code>.
	 *
	 * @param parentTreeItem
	 *            the parent tree item
	 * @param matchItems
	 *            the items to select
	 * @return
	 */
	public static SWTBotTreeItem select(final SWTBotTreeItem parentTreeItem, final String... matchItems) {
		final List<String> fullyQualifiedItems = Stream.of(parentTreeItem.getItems()).filter(
				treeItem -> Stream.of(matchItems).anyMatch(matchItem -> treeItem.getText().startsWith(matchItem)))
				.map(SWTBotTreeItem::getText).toList();
		return parentTreeItem.select(fullyQualifiedItems.toArray(new String[0]));
	}

	/**
	 * Selects <strong> all child items</strong> in the given
	 * <code>parentTreeItem</code> whose labels match the given
	 * <code>items</code>.
	 *
	 * @param parentTree
	 *            the parent tree
	 * @param matchItems
	 *            the items to select
	 * @return
	 */
	public static SWTBotTree select(final SWTBotTree parentTree, final String... matchItems) {
		final List<String> fullyQualifiedItems = Stream.of(parentTree.getAllItems()).filter(
				treeItem -> Stream.of(matchItems).anyMatch(matchItem -> treeItem.getText().startsWith(matchItem)))
				.map(SWTBotTreeItem::getText).toList();
		return parentTree.select(fullyQualifiedItems.toArray(new String[0]));
	}

	/**
	 * Hides the context menu left open on the given <code>tree</code>, if any.
	 * The menu is taken from the widget rather than through
	 * {@link SWTBotTree#contextMenu()}, which would open a new one.
	 *
	 * @param tree
	 *            the tree whose {@link Menu} should be hidden
	 */
	public static void hideMenu(final SWTBotTree tree) {
		final Menu menu = UIThreadRunnable.syncExec(() -> tree.widget.getMenu());
		if (menu != null) {
			new SWTBotRootMenu(menu).hide();
		}
	}

	public static SWTBotTreeItem expand(final SWTBotTree tree, final String... paths) {
		final SWTBotTreeItem rootItem = getTreeItem(tree, paths[0]);
		expandTreeItem(rootItem);
		if (paths.length > 1) {
			final String[] remainingPath = new String[paths.length - 1];
			System.arraycopy(paths, 1, remainingPath, 0, remainingPath.length);
			return expand(rootItem, remainingPath);
		}
		return rootItem;
	}

	public static SWTBotTreeItem expand(final SWTBotTreeItem treeItem, final String... paths) {
		final SWTBotTreeItem childItem = getTreeItem(treeItem, paths[0]);
		expandTreeItem(childItem);
		if (paths.length > 1) {
			final String[] remainingPath = new String[paths.length - 1];
			System.arraycopy(paths, 1, remainingPath, 0, remainingPath.length);
			return expand(childItem, remainingPath);
		}
		return getTreeItem(treeItem, paths[0]);
	}

	private static SWTBotTreeItem expandTreeItem(final SWTBotTreeItem treeItem) {
		final UIJob expandJob = new UIJob("expanding tree") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				treeItem.expand();
				return Status.OK_STATUS;
			}
		};
		expandJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				final int maxAttempts = 30;
				int currentAttempt = 0;
				while (currentAttempt < maxAttempts && treeItem.getItems().length == 1
						&& treeItem.getItems()[0].getText().isEmpty()) {
					SWTUtils.wait(1, TimeUnit.SECONDS);
					currentAttempt++;
				}

			}
		});
		expandJob.schedule();
		SWTUtils.wait(1, TimeUnit.SECONDS);
		return treeItem;
	}

	public static SWTBotView getSWTBotView(final SWTWorkbenchBot bot, final String viewId) {
		return bot.views().stream().filter(v -> v.getViewReference().getId().equals(viewId)).findFirst().orElse(null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getView(final SWTWorkbenchBot bot, final String viewId) {
		return (T) getView(bot, viewId, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getView(final SWTWorkbenchBot bot, final String viewId, final boolean restore) {
		final SWTBotView viewBot = bot.viewById(viewId);
		viewBot.setFocus();
		return (T) viewBot.getReference().getView(restore);
	}

	/**
	 * @return <code>true</code> if the Console view is visible in the active
	 *         page, <code>false</code> otherwise.
	 * @throws InterruptedException
	 */
	public static boolean isConsoleViewVisible(final SWTWorkbenchBot bot) {
		return bot.views().stream()
				.anyMatch(v -> v.getViewReference().getId().equals(IConsoleConstants.ID_CONSOLE_VIEW));
	}

	/**
	 * Waits until the Console view is visible: the console manager reveals it
	 * from its own (delayed) job, so it may not be there yet right after the
	 * launch job completed.
	 *
	 * @param workbenchBot
	 *            the {@link SWTWorkbenchBot}
	 */
	public static void waitForConsoleView(final SWTWorkbenchBot workbenchBot) {
		workbenchBot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() {
				return isConsoleViewVisible(workbenchBot);
			}

			@Override
			public String getFailureMessage() {
				return "The Console view did not show up";
			}
		});
	}

	/**
	 * Returns the button with the given tooltip in the toolbar of the Console
	 * view, waiting for it to show up: the console page contributing it is
	 * usually opened asynchronously by a background job or thread.
	 */
	public static SWTBotToolbarButton getConsoleToolbarButtonWithTooltipText(final SWTWorkbenchBot bot, final String tooltipText) {
		final SWTBotView consoleView = bot.viewById(IConsoleConstants.ID_CONSOLE_VIEW);
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() {
				return findToolbarButton(consoleView, tooltipText).isPresent();
			}

			@Override
			public String getFailureMessage() {
				return "No button with tooltip '" + tooltipText + "' in the Console view toolbar";
			}
		});
		return findToolbarButton(consoleView, tooltipText).get();
	}

	private static Optional<SWTBotToolbarButton> findToolbarButton(final SWTBotView view,
			final String tooltipText) {
		return view.getToolbarButtons().stream().filter(button -> tooltipText.equals(button.getToolTipText()))
				.findFirst();
	}

	/**
	 * Waits until the Properties view shows a tabbed page whose selected tab has
	 * the given id. The Properties view follows the selection of the active part
	 * asynchronously, so the tab cannot be checked right after selecting an
	 * element.
	 *
	 * @param workbenchBot
	 *            the {@link SWTWorkbenchBot}
	 * @param tabId
	 *            the id of the tab that is expected to be selected
	 * @return the selected {@link ITabDescriptor}
	 */
	public static ITabDescriptor waitForSelectedPropertiesTab(final SWTWorkbenchBot workbenchBot,
			final String tabId) {
		final ITabDescriptor[] selectedTab = new ITabDescriptor[1];
		workbenchBot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() {
				final PropertySheet propertySheet = UIThreadRunnable.syncExec(
						() -> SWTUtils.<PropertySheet>getView(workbenchBot, "org.eclipse.ui.views.PropertySheet", //$NON-NLS-1$
								true));
				if (propertySheet != null
						&& propertySheet.getCurrentPage() instanceof TabbedPropertySheetPage tabbedPage) {
					selectedTab[0] = tabbedPage.getSelectedTab();
				}
				return selectedTab[0] != null && tabId.equals(selectedTab[0].getId());
			}

			@Override
			public String getFailureMessage() {
				return "Expected tab section with id '" + tabId + "' to be selected but it was "
						+ (selectedTab[0] != null ? "'" + selectedTab[0].getId() + "'" : "none");
			}
		}, TimeUnit.SECONDS.toMillis(10));
		return selectedTab[0];
	}

	public static void closeView(final SWTWorkbenchBot bot, final String viewId) {
		bot.views().stream().filter(v -> v.getReference().getId().equals(viewId)).forEach(SWTBotView::close);
	}

}
