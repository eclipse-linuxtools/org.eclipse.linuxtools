package org.eclipse.linuxtools.callgraph.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public abstract class SystemTapView extends ViewPart {
	
	private final String NEW_LINE = Messages.getString("SystemTapView.1"); //$NON-NLS-1$
	
	public Composite masterComposite;
	private SystemTapView stapview;
	private Action error_errorLog;
	private Action error_deleteError;
	private IMenuManager errors;
	private IMenuManager help;
	private Action kill;
	public SystemTapParser parser;

	protected String viewID;
	@SuppressWarnings("unused")
	private Action help_about;
	private Action help_version;

	/**
	 * The constructor.
	 * 
	 * @return
	 */
	public SystemTapView() {
	}

	public abstract IStatus initialize(Display targetDisplay,
			IProgressMonitor monitor);

	public SystemTapView getSingleInstance() {
		return stapview;
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
			IWorkbenchAction action = ActionFactory.MAXIMIZE.create(this
					.getSingleInstance().getViewSite().getWorkbenchWindow());
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
			IWorkbenchAction action = ActionFactory.MAXIMIZE.create(this
					.getSingleInstance().getViewSite().getWorkbenchWindow());
			action.run();
		}
	}

	/**
	 * Schedules the updateMethod job in a UI Thread. Does not return until
	 * updateMethod is complete.
	 * 
	 * @throws InterruptedException
	 */
	public void update() throws InterruptedException {
		ViewUIUpdater updater = new ViewUIUpdater("UIUpdater"); //$NON-NLS-1$
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
			IViewPart view = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().showView(viewID);
			if (!(view instanceof SystemTapView))
				throw new Exception("Miscast type: " + view.getClass().toString()); 
			setView((SystemTapView) view); 
			stapview.setFocus();
		} catch (PartInitException e2) {
			e2.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Force the view to be created, but will not necessarily display the view.
	 */
	public void forceCreate() {
		this.setViewID();
		try {
			IViewPart view = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().showView(viewID, null, 
						IWorkbenchPage.VIEW_CREATE);
			if (!(view instanceof SystemTapView))
				return; 
			setView((SystemTapView) view); 
			stapview.setFocus();
		} catch (PartInitException e2) {
			e2.printStackTrace();
		}
		
	}
	
	protected void setView(SystemTapView view) {
		stapview = view;
	}

	/**
	 * Method for setting the parser object of the view. Make this method return
	 * true if the parser is of the expected class, false if it is null or
	 * unexpected.
	 * 
	 * @param parser
	 * @return
	 */
	public abstract boolean setParser(SystemTapParser parser);

	/**
	 * Perform whatever actions are necessary to 'update' this viewer. It is
	 * recommended that the update function be called after the setParser method
	 * is called.
	 */
	public abstract void updateMethod();

	/**
	 * Implement this method to set the viewID variable to the id of the view
	 * that extends SystemTapView and uses the core.systemtapview extension
	 * point.
	 */
	public abstract void setViewID();

	/**
	 * Appends the error menu, containing options for opening and clearing the
	 * error log.
	 */
	public void addErrorMenu() {
		IMenuManager menu = getViewSite().getActionBars().getMenuManager();
		errors = new MenuManager(Messages.getString("SystemTapView.2")); //$NON-NLS-1$
		help = new MenuManager(Messages.getString("SystemTapView.3")); //$NON-NLS-1$
		menu.add(errors);
		menu.add(help);
		createErrorActions();
		createHelpActions();
		
		help.add(help_version);
		errors.add(error_errorLog);
		errors.add(error_deleteError);
	}

	
	public void createHelpActions() {
		help_version = new Action(Messages.getString("SystemTapView.4")) { //$NON-NLS-1$
			public void run() {
				Runtime rt = Runtime.getRuntime();
				try {
					Process pr = rt.exec("stap -V"); //$NON-NLS-1$
					BufferedReader buf = new BufferedReader(
							new InputStreamReader(pr.getErrorStream()));
					String line = ""; //$NON-NLS-1$
					String message = ""; //$NON-NLS-1$

					while ((line = buf.readLine()) != null) {
						message += line + NEW_LINE; //$NON-NLS-1$
					}

					try {
						pr.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					Shell sh = new Shell();

					MessageDialog.openInformation(sh, Messages
							.getString("SystemTapView.5"), message); //$NON-NLS-1$

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		
		help_about = new Action(Messages.getString("SystemTapView.6")) { //$NON-NLS-1$
			public void run() {
				Display disp = Display.getCurrent();
				if (disp == null){
					disp = Display.getDefault();
				}

				
				Shell sh = new Shell(disp, SWT.MIN | SWT.MAX);
				sh.setSize(425, 540);
				GridLayout gl = new GridLayout(1, true);
				sh.setLayout(gl);

				sh.setText(""); //$NON-NLS-1$
				
				Image img = new Image(disp, PluginConstants.PLUGIN_LOCATION+"systemtap.png"); //$NON-NLS-1$
				Composite cmp = new Composite(sh, sh.getStyle());
				cmp.setLayout(gl);
				GridData data = new GridData(415,100);
				cmp.setLayoutData(data);
				cmp.setBackgroundImage(img);

				Composite c = new Composite(sh, sh.getStyle());
				c.setLayout(gl);
				GridData gd = new GridData(415,400);
				c.setLayoutData(gd);
				c.setLocation(0,300);
				StyledText viewer = new StyledText(c, SWT.READ_ONLY | SWT.MULTI
						| SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);		
				
				GridData viewerGD = new GridData(SWT.FILL, SWT.FILL, true, true);
				viewer.setLayoutData(viewerGD);
				Font font = new Font(sh.getDisplay(), "Monospace", 11, SWT.NORMAL); //$NON-NLS-1$
				viewer.setFont(font);
				viewer.setText(
						 "" + //$NON-NLS-1$
						 "" + //$NON-NLS-1$
						 "" + //$NON-NLS-1$
						 "" +  //$NON-NLS-1$
						 "" + //$NON-NLS-1$
						 "" + //$NON-NLS-1$
						 
						 "" + //$NON-NLS-1$
//						 
//						 Messages.getString("LaunchAbout.9") + //$NON-NLS-1$
//						 Messages.getString("LaunchAbout.10") + //$NON-NLS-1$
						 
						 "" + //$NON-NLS-1$
						 "" + //$NON-NLS-1$
						 "" + //$NON-NLS-1$
						 
//						 Messages.getString("LaunchAbout.14") + //$NON-NLS-1$
//						 Messages.getString("LaunchAbout.15") + //$NON-NLS-1$
//						 Messages.getString("LaunchAbout.16") + //$NON-NLS-1$
						 
						 "" + //$NON-NLS-1$
						 
//						 Messages.getString("LaunchAbout.18") + //$NON-NLS-1$
//						 Messages.getString("LaunchAbout.19") + //$NON-NLS-1$
						 
						 "" + //$NON-NLS-1$
						 "" //$NON-NLS-1$
						);


				
				sh.open();		
			}
		};
	}
	
	
	/**
	 * Populates the Errors menu
	 */
	public void createErrorActions() {

		error_errorLog = new Action(Messages.getString("SystemTapView.0")) { //$NON-NLS-1$
			public void run() {
				boolean error = false;
				File log = new File(PluginConstants.DEFAULT_OUTPUT
						+ "Error.log"); //$NON-NLS-1$
				BufferedReader buff;
				try {
					buff = new BufferedReader(new FileReader(log));
					String logText = ""; //$NON-NLS-1$
					String line;

					while ((line = buff.readLine()) != null) {
						logText += line + PluginConstants.NEW_LINE;
					}

					Shell sh = new Shell(SWT.BORDER | SWT.TITLE);

					sh.setText(Messages.getString("SystemTapView.15")); //$NON-NLS-1$
					sh.setLayout(new FillLayout());
					sh.setSize(600, 600);

					StyledText txt = new StyledText(sh, SWT.MULTI
							| SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);

					txt.setText(logText);

					sh.setText(Messages.getString("SystemTapView.21")); //$NON-NLS-1$

					sh.open();
					txt.setTopIndex(txt.getLineCount());

				} catch (FileNotFoundException e) {
					error = true;
				} catch (IOException e) {
					error = true;
				} finally {
					if (error) {
						SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
								Messages
										.getString("SystemTapView.ErrorMessageName"), //$NON-NLS-1$
								Messages
										.getString("SystemTapView.ErrorMessageTitle"), //$NON-NLS-1$
								Messages
										.getString("SystemTapView.ErrorMessageBody") //$NON-NLS-1$
										+ Messages
												.getString("SystemTapView.ErrorMessageBody2")); //$NON-NLS-1$
						mess.schedule();
					}
				}

			}
		};

		error_deleteError = new Action(Messages
				.getString("SystemTapView.ClearLog")) { //$NON-NLS-1$
			public void run() {
				if (!MessageDialog.openConfirm(new Shell(), Messages
						.getString("SystemTapView.DeleteLogsTitle"), Messages //$NON-NLS-1$
						.getString("SystemTapView.DeleteLogsMessage") //$NON-NLS-1$
						+ Messages
								.getString("SystemTapView.DeleteLogsMessage2"))) //$NON-NLS-1$
					return;

				SystemTapErrorHandler.delete();
			}
		};
	}

	protected void addKillButton() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		kill = new Action(Messages.getString("SystemTapView.16"), ImageDescriptor //$NON-NLS-1$
				.createFromImage(new Image(Display.getCurrent(),
						PluginConstants.PLUGIN_LOCATION
								+ "icons/progress_stop.gif"))) { //$NON-NLS-1$
			public void run() {
				Runtime run = Runtime.getRuntime();
				try {
					parser.setDone(true);
					run.exec("kill stap"); //$NON-NLS-1$
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		mgr.add(kill);
	}
	
	public void setKillButtonEnabled(boolean val) {
		if (kill != null)
			kill.setEnabled(val);
	}
	
	
	public Action getKillButton() {
		return kill;
	}
	
	public Action getError_errorLog() {
		return error_errorLog;
	}

	public void setError_errorLog(Action errorErrorLog) {
		error_errorLog = errorErrorLog;
	}

	public Action getError_deleteError() {
		return error_deleteError;
	}

	public void setError_deleteError(Action errorDeleteError) {
		error_deleteError = errorDeleteError;
	}

	public  Action getHelp_version() {
		return help_version;
	}

	public  void setHelp_version(Action helpVersion) {
		help_version = helpVersion;
	}


}
