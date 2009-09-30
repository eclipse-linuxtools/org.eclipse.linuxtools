package org.eclipse.linuxtools.callgraph.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public abstract class SystemTapView extends ViewPart {
	public static Composite masterComposite;
	private static SystemTapView stapview;
	private static boolean isInitialized;


	/**
	 * The constructor.
	 * 
	 * @return
	 */
	public SystemTapView() {
		isInitialized = true;
	}

	public abstract IStatus initialize(Display targetDisplay,
			IProgressMonitor monitor);

	public static SystemTapView getSingleInstance() {
		if (isInitialized) {
			return stapview;
		}
		return null;
	}

	/**
	 * @param doMaximize
	 *            : true && view minimized will maximize the view, otherwise it
	 *            will just 'refresh'
	 */
	public static void maximizeOrRefresh(boolean doMaximize) {
		IWorkbenchPage page = SystemTapView.getSingleInstance().getViewSite()
				.getWorkbenchWindow().getActivePage();

		if (doMaximize
				&& page.getPartState(page.getActivePartReference()) != IWorkbenchPage.STATE_MAXIMIZED) {
			IWorkbenchAction action = ActionFactory.MAXIMIZE
					.create(SystemTapView.getSingleInstance().getViewSite()
							.getWorkbenchWindow());
			action.run();
		} else {
			SystemTapView.layout();
		}
	}

	public static void layout() {
		masterComposite.layout();
	}

	/**
	 * If view is not maximized it will be maximized
	 */
	public static void maximizeIfUnmaximized() {
		IWorkbenchPage page = SystemTapView.getSingleInstance().getViewSite()
				.getWorkbenchWindow().getActivePage();

		if (page.getPartState(page.getActivePartReference()) != IWorkbenchPage.STATE_MAXIMIZED) {
			IWorkbenchAction action = ActionFactory.MAXIMIZE
					.create(SystemTapView.getSingleInstance().getViewSite()
							.getWorkbenchWindow());
			action.run();
		}
	}
	
	ViewUIUpdater updater;
	
	public void update() {
		updater = new ViewUIUpdater("UIUpdater");
		updater.schedule();
	}
	
	public void join() throws InterruptedException {
		updater.join();
	}
	
	private class ViewUIUpdater extends UIJob {

		public ViewUIUpdater(String name) {
			super(name);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			updateMethod();
			return Status.OK_STATUS;
		}
		
	};

	protected void setView(SystemTapView view) {
		stapview = view;
	}

	/**
	 * Method for setting the parser object of the view. Make this method return true if 
	 * the parser is of the expected class, false if it is null or unexpected.
	 * @param parser
	 * @return
	 */
	public abstract boolean setParser(SystemTapParser parser);

	/**
	 * Perform whatever actions are necessary to 'update' this viewer. It is recommended that 
	 * the update function be called after the setParser method is called.
	 */
	public abstract void updateMethod();
	

}
