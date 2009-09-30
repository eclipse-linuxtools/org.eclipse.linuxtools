package org.eclipse.linuxtools.callgraph.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public abstract class SystemTapView extends ViewPart {
	public Composite masterComposite;
	private SystemTapView stapview;
	private boolean isInitialized;
	protected String viewID;


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

	public SystemTapView getSingleInstance() {
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
	public void maximizeOrRefresh(boolean doMaximize) {
		IWorkbenchPage page = this.getSingleInstance().getViewSite()
				.getWorkbenchWindow().getActivePage();

		if (doMaximize
				&& page.getPartState(page.getActivePartReference()) != IWorkbenchPage.STATE_MAXIMIZED) {
			IWorkbenchAction action = ActionFactory.MAXIMIZE
					.create(this.getSingleInstance().getViewSite()
							.getWorkbenchWindow());
			action.run();
		} else {
			this.layout();
		}
	}

	public void layout() {
		masterComposite.layout();
	}

	/**
	 * If view is not maximized it will be maximized
	 */
	public void maximizeIfUnmaximized() {
		IWorkbenchPage page = this.getSingleInstance().getViewSite()
				.getWorkbenchWindow().getActivePage();

		if (page.getPartState(page.getActivePartReference()) != IWorkbenchPage.STATE_MAXIMIZED) {
			IWorkbenchAction action = ActionFactory.MAXIMIZE
					.create(this.getSingleInstance().getViewSite()
							.getWorkbenchWindow());
			action.run();
		}
	}
	
	/**
	 * Schedules the updateMethod job in a UI Thread. Does not return until
	 * updateMethod is complete.
	 * @throws InterruptedException
	 */
	public void update() throws InterruptedException {
		ViewUIUpdater updater = new ViewUIUpdater("UIUpdater");
		updater.schedule();
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
	

	/**
	 * Force the view to initialize
	 */
	public void forceDisplay() {
		this.setViewID();
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			stapview = (SystemTapView) window
					.getActivePage()
					.showView(
							viewID);
			stapview.setFocus(); //$NON-NLS-1$
		} catch (PartInitException e2) {
			e2.printStackTrace();
		}
	}

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

	/**
	 * Implement this method to set the viewID variable to the id of the view that
	 * extends SystemTapView and uses the core.systemtapview extension point.
	 */
	public abstract void setViewID();

}
