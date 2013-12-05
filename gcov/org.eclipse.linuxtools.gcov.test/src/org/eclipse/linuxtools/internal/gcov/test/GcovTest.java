package org.eclipse.linuxtools.internal.gcov.test;

import static org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper.contextMenu;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.dataviewers.actions.STExportToCSVAction;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.STAnnotatedSourceEditorActivator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.osgi.framework.FrameworkUtil;

public abstract class GcovTest {
	private static final String PROJECT_EXPLORER = "Project Explorer";
	private static SWTBotView projectExplorer;
	private static SWTBotShell mainShell;

	private static final class UnCheckTest implements ICondition {
		SWTBotCheckBox checkBox;

		public UnCheckTest(SWTBotCheckBox bot) {
			checkBox = bot;
		}

		@Override
		public boolean test() {
			return !checkBox.isChecked();
		}

		@Override
		public void init(SWTBot bot) {
		}

		@Override
		public String getFailureMessage() {
			return null;
		}
	}

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

	public static SWTWorkbenchBot init(String PROJECT_NAME, String PROJECT_TYPE)
			throws Exception {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		bot.captureScreenshot(PROJECT_NAME + ".beforeClass.1.jpg");
		try {
			bot.viewByTitle("Welcome").close();
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate();
			bot.button("Cancel").click();
		} catch (WidgetNotFoundException e) {
			// ignore
		}

		bot.perspectiveByLabel("C/C++").activate();
		for (SWTBotShell sh : bot.shells()) {
			if (sh.getText().startsWith("C/C++")) {
				sh.activate();
				bot.waitUntil(Conditions.shellIsActive(sh.getText()));
				mainShell = sh;
				break;
			}
		}

		bot.captureScreenshot(PROJECT_NAME + ".beforeClass.2.jpg");
		// Turn off automatic building by default
		SWTBotMenu windowsMenu = bot.menu("Window");
		windowsMenu.menu("Preferences").click();
		SWTBotShell shell = bot.shell("Preferences");
		shell.activate();
		bot.text().setText("Workspace");
		bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "General", "Workspace"));
		SWTBotCheckBox buildAuto = bot.checkBox("Build automatically");
		if (buildAuto != null && buildAuto.isChecked()) {
			buildAuto.click();
		}
		bot.waitUntil(new UnCheckTest(buildAuto));
		bot.button("Apply").click();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));


		// define & repopulate project explorer
		projectExplorer = bot.viewByTitle(PROJECT_EXPLORER);
		GcovTest.createProject(bot, PROJECT_NAME, PROJECT_TYPE);
		GcovTest.populateProject(bot, PROJECT_NAME);
		GcovTest.compileProject(bot, PROJECT_NAME);
		return bot;
	}

	public static void cleanup(SWTWorkbenchBot bot) {
		// clear project explorer
		SWTBotTree treeBot = projectExplorer.bot().tree();
		for (SWTBotTreeItem treeItem : treeBot.getAllItems()) {
			removeTreeItem(bot, treeItem);
		}
	}

	public static void createProject(SWTWorkbenchBot bot, String projectName, String projectType) {
		mainShell.activate();
		SWTBotMenu fileMenu = bot.menu("File");
		SWTBotMenu newMenu = fileMenu.menu("New");
		SWTBotMenu projectMenu = newMenu.menu(projectType);
		projectMenu.click();

		SWTBotShell shell = bot.shell(projectType);
		shell.activate();

		bot.tree().expandNode("Makefile project").select("Empty Project");
		bot.textWithLabel("Project name:").setText(projectName);
		bot.table().select("Linux GCC");

		bot.button("Next >").click();
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(shell));
	}

	public static void populateProject(SWTWorkbenchBot bot, String projectName) throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		try (InputStream is = FileLocator.openStream(
				FrameworkUtil.getBundle(GcovTest.class), new Path("resource/"
						+ projectName + "/content"), false);
				LineNumberReader lnr = new LineNumberReader(
						new InputStreamReader(is))) {
			String filename;
			while (null != (filename = lnr.readLine())) {
				final ProgressMonitor pm = new ProgressMonitor();
				final IFile ifile = project.getFile(filename);
				InputStream fis = FileLocator.openStream(FrameworkUtil
						.getBundle(GcovTest.class), new Path("resource/"
						+ projectName + "/" + filename), false);
				ifile.create(fis, true, pm);
				bot.waitUntil(new DefaultCondition() {

					@Override
					public boolean test() {
						return pm.isDone();
					}

					@Override
					public String getFailureMessage() {
						return ifile + " not yet created after 6000ms";
					}
				}, 6000);
			}
		}
	}

	public static void compileProject(SWTWorkbenchBot bot, String projectName) {
		SWTBotTree treeBot = projectExplorer.bot().tree();
		treeBot.setFocus();
		treeBot = treeBot.select(projectName);
		bot.waitUntil(Conditions.treeHasRows(treeBot, 1));
		mainShell.activate();
		SWTBotMenu menu = bot.menu("Build Project");
		menu.click();
		bot.waitUntil(new JobsRunning(ResourcesPlugin.FAMILY_MANUAL_BUILD), 30000);
	}

	private static void removeTreeItem(SWTWorkbenchBot bot,
			SWTBotTreeItem treeItem) {
		String shellTitle = "Delete Resources";
		treeItem.contextMenu("Delete").click();
		SWTBotShell deleteShell = bot.shell(shellTitle);
		deleteShell.activate();
		bot.button("OK").click();
		// Another shell (with the same name!) may appear if resources aren't synced.
		// If it does appear, it will be a child of the first shell.
		try {
			bot.waitUntil(Conditions.shellCloses(deleteShell), 1000);
		} catch (TimeoutException e) {
			SWTBotShell deleteShell2;
			try {
				deleteShell2 = bot.shell(shellTitle, deleteShell.widget);
			} catch (WidgetNotFoundException e2) {
				// If the other shell isn't found, that means the first one just didn't close.
				throw e;
			}
			System.out.println("Deleting out-of-sync resources - new \"Delete Resources\" shell found");
			deleteShell2.activate();
			bot.button("Continue").click();
			bot.waitUntil(Conditions.shellCloses(deleteShell2));
			bot.waitUntil(Conditions.shellCloses(deleteShell));
		}
	}

	private static TreeSet<String> getGcovFiles(SWTWorkbenchBot bot, String projectName) throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		TreeSet<String> ret = new TreeSet<>();
		for (IResource r : project.members()) {
			if (r.getType() == IResource.FILE && r.exists()) {
				if (r.getName().endsWith(".gcda") || r.getName().endsWith(".gcno")) {
					ret.add(r.getFullPath().toOSString());
				}
			}
		}
		return ret;
	}

	private static void testGcovSummary(SWTWorkbenchBot bot, String projectName, String filename, String binName,
			boolean testProducedReference) throws Exception {
		IPath filePath = new Path(filename);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
		String binPath = file.getProject().getFile(binName).getLocation().toOSString();

		openResource(bot, file.getName());
		Matcher<Shell> withText = withText("Gcov - Open coverage results...");
		bot.waitUntil(Conditions.waitForShell(withText));

		SWTBotShell shell = bot.shell("Gcov - Open coverage results...");
		shell.activate();
		bot.textInGroup("Binary File", 0).setText(binPath);
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));

		SWTBotView botView = bot.viewByTitle("gcov");
		// The following cannot be tested on 4.2 because the SWTBot implementation of toolbarButton()
		// is broken there because it relies PartPane having a method getPane() which is no longer true.
		botView.toolbarButton("Sort coverage per function").click();
		dumpCSV(bot, botView, projectName, "function", testProducedReference);
		botView.toolbarButton("Sort coverage per file").click();
		dumpCSV(bot, botView, projectName, "file", testProducedReference);
		botView.toolbarButton("Sort coverage per folder").click();
		dumpCSV(bot, botView, projectName, "folder", testProducedReference);
		botView.close();
	}

	private static void openResource(SWTWorkbenchBot bot, String fileName) {
		mainShell.activate();
		bot.menu("Navigate").menu("Open Resource...").click();
		bot.waitUntil(Conditions.shellIsActive("Open Resource"));
		SWTBotShell shell = bot.shell("Open Resource");
		shell.activate();
		bot.text().setText(fileName);
		bot.button("Open").click();
		bot.waitUntil(Conditions.shellCloses(shell));
	}

	private static void testGcovFileDetails(SWTWorkbenchBot bot, String projectName, String filename, String binName) throws Exception {
		IPath filePath = new Path(filename);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
		String binPath = file.getProject().getFile(binName).getLocation().toOSString();

		openResource(bot, file.getName());
		Matcher<Shell> withText = withText("Gcov - Open coverage results...");
		bot.waitUntil(Conditions.waitForShell(withText));

		SWTBotShell shell = bot.shell("Gcov - Open coverage results...");
		shell.activate();
		bot.textInGroup("Binary File", 0).setText(binPath);
		SWTBotRadio button = bot.radioInGroup("Coverage result", 0);
		button.click();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));

		SWTBotEditor editor = bot.editorById(STAnnotatedSourceEditorActivator.EDITOR_ID);
		SWTBotEclipseEditor edt = editor.toTextEditor(); /* just to verify that the correct file was found */
		edt.close();
	}

	private static void testGcovLaunchSummary(SWTWorkbenchBot bot, String projectName, String binName) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		String binLocation = project.getFile(binName).getLocation().toOSString();
		IPath binPath = new Path(binLocation);
		IFile binFile = ResourcesPlugin.getWorkspace().getRoot().getFile(binPath);

		SWTBot viewBot = projectExplorer.bot();

		SWTBotTree treeBot = viewBot.tree();
		treeBot.setFocus();
		// We need to select the binary, but in the tree, it may have additional info appended to the
		// name such as [x86_64/le].  So, we look at all nodes of the project and look for the one that
		// starts with our binary file name.  We can then select the node.
		projectExplorer.bot().tree().select(projectName).contextMenu("Go Into").click();
		bot.waitUntil(Conditions.waitForWidget(WidgetMatcherFactory.withText(projectName), projectExplorer.getWidget()));

		SWTBotTreeItem[] nodes = treeBot.getAllItems();
		String binNodeName = binFile.getName();
		for (SWTBotTreeItem node : nodes) {
			if (node.getText().startsWith(binNodeName)) {
				node.select();
				break;
			}
		}
		Assert.assertTrue(treeBot.selectionCount() != 0);
		String menuItem = "Profiling Tools";
		String subMenuItem = "1 Profile Code Coverage";
		click(contextMenu(treeBot, menuItem, subMenuItem));

		final boolean result[] = new boolean[1];
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage()
							.showView("org.eclipse.linuxtools.gcov.view");
					result[0] = true;
				} catch (PartInitException e) {
					result[0] = false;
				}
			}
		});
		if (!result[0]) {
			Assert.fail();
		}
		SWTBotView botView = bot.viewByTitle("gcov");

		botView.close();
		SWTBotToolbarButton forwardButton = projectExplorer.toolbarPushButton("Forward");
		projectExplorer.toolbarPushButton("Back to Workspace").click();
		bot.waitUntil(Conditions.widgetIsEnabled(forwardButton));
	}

	private static void dumpCSV(SWTWorkbenchBot bot, SWTBotView botView, String projectName, String type,
			boolean testProducedReference) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		botView.toolbarButton("Export to CSV").click();
		SWTBotShell shell = bot.shell("Export to CSV");
		shell.activate();
		String s = project.getLocation() + "/" + type + "-dump.csv";
		new File(s).delete();
		bot.text().setText(s);
		bot.button("OK").click();
		bot.waitUntil(new JobsRunning(STExportToCSVAction.EXPORT_TO_CSV_JOB_FAMILY), 3000);
		if (testProducedReference) {
			String ref = STJunitUtils.getAbsolutePath(FrameworkUtil.getBundle(GcovTest.class).getSymbolicName(), "resource/" + projectName + "/" + type + ".csv");
			STJunitUtils.compareIgnoreEOL(project.getLocation() + "/" + type + "-dump.csv", ref, false);
		}
	}

	public static void openGcovFileDetails(SWTWorkbenchBot bot, String projectName) throws Exception {
		openGcovFileDetails(bot, projectName, "a.out");
	}

	public static void openGcovSummary(SWTWorkbenchBot bot, String projectName, boolean testProducedReference)
			throws Exception {
		openGcovSummary(bot, projectName, "a.out", testProducedReference);
	}

	public static void openGcovSummary(SWTWorkbenchBot bot, String projectName, String binName,
			boolean testProducedReference) throws Exception {
		TreeSet<String> ts = getGcovFiles(bot, projectName);
		for (String string : ts) {
			testGcovSummary(bot, projectName, string, binName, testProducedReference);
		}
	}

	public static void openGcovFileDetails(SWTWorkbenchBot bot,
			String projectName, String binName) throws Exception {
		TreeSet<String> ts = getGcovFiles(bot, projectName);
		for (String string : ts) {
			testGcovFileDetails(bot, projectName, string, binName);
		}
	}

	public static void openGcovSummaryByLaunch(SWTWorkbenchBot bot,
			String projectName) {
		testGcovLaunchSummary(bot, projectName, "a.out");
	}

	/**
	 * Click on the specified MenuItem.
	 * @param menuItem MenuItem item to click
	 */
	private static void click(final MenuItem menuItem) {
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
