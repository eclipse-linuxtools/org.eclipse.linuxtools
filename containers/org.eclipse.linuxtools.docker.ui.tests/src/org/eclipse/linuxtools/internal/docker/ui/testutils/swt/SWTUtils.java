package org.eclipse.linuxtools.internal.docker.ui.testutils.swt;

import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.ComparisonFailure;

public class SWTUtils {

	/**
	 * Calls <strong>synchronously</strong> the given {@link Runnable} in the
	 * default Display and returns the result
	 * 
	 * @param supplier
	 *            the Supplier to call
	 * @return the supplier's result
	 */
	public static <V> V syncExec(final Supplier<V> supplier) {
		final Queue<V> result = new ArrayBlockingQueue<>(1);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				result.add(supplier.get());
			}
		});
		return result.poll();
	}

	/**
	 * Executes <strong>synchronously</strong> the given {@link Runnable} in the
	 * default Display
	 * 
	 * @param runnable
	 *            the {@link Runnable} to execute
	 */
	public static void syncExec(final Runnable runnable) {
		Display.getDefault().syncExec(runnable);
	}

	/**
	 * Executes <strong>synchronously</strong> the given {@link Runnable} in the
	 * default Display. The given {@link Runnable} is ran into a rapping
	 * {@link Runnable} that will catch the {@link ComparisonFailure} that may
	 * be raised during an assertion.
	 * 
	 * @param runnable
	 *            the {@link Runnable} to execute
	 * @throws ComparisonFailure
	 *             if an assertion failed.
	 * @throws SWTException if an {@link SWTException} occurred             
	 */
	public static void syncAssert(final Runnable runnable) throws SWTException, ComparisonFailure {
		final Queue<ComparisonFailure> failure = new ArrayBlockingQueue<>(1);
		final Queue<SWTException> swtException = new ArrayBlockingQueue<>(1);
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					runnable.run();
				} catch (ComparisonFailure e) {
					failure.add(e);
				} catch (SWTException e) {
					swtException.add(e);
				}
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
	 * @throws InterruptedException
	 */
	public static void asyncExec(final Runnable runnable) {
		final Queue<ComparisonFailure> failure = new ArrayBlockingQueue<>(1);
		final Queue<SWTException> swtException = new ArrayBlockingQueue<>(1);
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					runnable.run();
				} catch (ComparisonFailure e) {
					failure.add(e);
				} catch (SWTException e) {
					swtException.add(e);
				}
			}
		});
		waitForJobsToComplete();
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
		wait(1, TimeUnit.SECONDS);
		while (!Job.getJobManager().isIdle()) {
			wait(1, TimeUnit.SECONDS);
		}
	}

	/**
	 * @param viewBot the {@link SWTBotView} containing the {@link Tree} to traverse
	 * @param paths the node path in the {@link SWTBotTree} associated with the given {@link SWTBotView}
	 * @return the first {@link SWTBotTreeItem} matching the given node names
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotView viewBot, final String... paths) {
		final SWTBotTree tree = viewBot.bot().tree();
		return getTreeItem(tree.getAllItems(), paths);
	}
	
	public static SWTBotTreeItem getTreeItem(final SWTBotTreeItem parentTreeItem, final String... paths) {
		if(paths.length == 1) {
			return getTreeItem(parentTreeItem, paths[0]);
		}
		final String[] remainingPaths = new String[paths.length-1];
		System.arraycopy(paths, 1, remainingPaths, 0, paths.length-1);
		return getTreeItem(getTreeItem(parentTreeItem, paths[0]), remainingPaths);
	}

	/**
	 * Returns the first child node in the given parent tree item whose text matches (ie, begins with) the given path argument.
	 * @param parentTreeItem the parent tree item
	 * @param path the text of the node that should match
	 * @return the first matching node or <code>null</code> if none could be found
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotTreeItem parentTreeItem, final String path) {
		for (SWTBotTreeItem child : parentTreeItem.getItems()) {
			if(child.getText().startsWith(path)) {
				return child;
			}
		}
		return null;
	}

	private static SWTBotTreeItem getTreeItem(final SWTBotTreeItem[] treeItems, final String[] paths) {
		final SWTBotTreeItem swtBotTreeItem = Stream.of(treeItems).filter(item -> item.getText().startsWith(paths[0])).findFirst().get();
		if(paths.length > 1) {
			final String[] remainingPath = new String[paths.length -1];
			System.arraycopy(paths, 1, remainingPath, 0, remainingPath.length);
			return getTreeItem(swtBotTreeItem.getItems(), remainingPath);
		}
		return swtBotTreeItem;
	}

	public static void wait(final int duration, final TimeUnit unit) {
		try {
			Thread.sleep(unit.toMillis(duration));
		} catch (InterruptedException e) {
			fail("Failed to wait for a " + unit.toMillis(duration) + "ms", e);
		}
	}

	/**
	 * Selects all child items in the given <code>parentTreeItm</code> whose labels match the given <code>items</code>.
	 * @param parentTreeItem the parent tree item
	 * @param matchItems the items to select
	 */
	public static void select(SWTBotTreeItem parentTreeItem, String... matchItems) {
		final List<String> fullyQualifiedItems = Stream.of(parentTreeItem.getItems())
				.filter(treeItem -> Stream.of(matchItems)
						.anyMatch(matchItem -> treeItem.getText().startsWith(matchItem)))
				.map(item -> item.getText()).collect(Collectors.toList());
		parentTreeItem.select(fullyQualifiedItems.toArray(new String[0]));
	}
	
}
