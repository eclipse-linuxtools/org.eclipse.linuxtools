/*******************************************************************************
 * Copyright (c) 2009, 2019 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.callgraph.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public abstract class SystemTapView extends ViewPart {

    private final String NEW_LINE = Messages.getString("SystemTapView.1"); //$NON-NLS-1$

    public Composite masterComposite;
    private Action kill;

    protected String viewID;
    private Action helpVersion;
    protected Action saveFile;
    protected Action openFile;
    protected Action openDefault;
    protected String sourcePath;
    protected IMenuManager file;
    private SystemTapParser parser;


    /**
     * This method will be called from GraphUIJob to load the view
     * @param targetDisplay
     * @param monitor
     * @return Status.OK_STATUS to continue, Status.CANCEL_STATUS to abort
     */
    public abstract IStatus initializeView(Display targetDisplay,
            IProgressMonitor monitor);

    public void layout() {
        masterComposite.layout();
    }

    /**
     * If view is not maximized it will be maximized
     */
	public void maximizeIfUnmaximized() {
		IWorkbenchPage page = this.getViewSite().getWorkbenchWindow().getActivePage();

		if (page.getPartState(page.getActivePartReference()) != IWorkbenchPage.STATE_MAXIMIZED) {
			IHandlerService handlerService = getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand("org.eclipse.ui.window.maximizePart", null); //$NON-NLS-1$
			} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

    /**
     * Schedules the updateMethod job in a UI Thread. Does not return until
     * updateMethod is complete.
     *
     * @throws InterruptedException
     */
    public void update() throws InterruptedException {
        ViewUIUpdater updater = new ViewUIUpdater("SystemTapView.update"); //$NON-NLS-1$
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

    }

    /**
     * Method for fetching a parser object. This method should return
     * the running parser, or else some features may not work. Create
     * your own parser parameter, but please ensure that it extends
     * SystemTapParser.
     *
     * @return
     */
    public SystemTapParser getParser() {
        return parser;
    }

    /**
     * Method for setting the parser object of the view. Make this method return
     * true if the parser is of the expected class, false if it is null or
     * unexpected.
     *
     * @param parser
     * @return
     */
    public boolean setParser(SystemTapParser parser) {
        this.parser = parser;
        if (this.parser == null) {
            return false;
        }
        return true;
    }

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
     * Implement this method so that the Open button in the file menu created
     * by <code>addFileMenu()</code> is able to actually open files. User will
     * be prompted for a file to open.
     *
     * @return True if an open action should be created, false otherwise.
     */
    protected abstract boolean createOpenAction();

    /**
     * Implement this method so that the Open default button in the file menu created
     * by <code>addFileMenu()</code> is able to actually open default. The Open
     * default button should open from a fixed location, usually the default output
     * path if that is accessible..
     *
     *  @return True if an open default action should be created, false otherwise.
     */
    protected abstract boolean createOpenDefaultAction();


    /**
     * Create File menu -- calls the abstract protected methods
     * <code>createOpenAction()</code> and <code>createOpenDefaultAction()</code>. Have
     * these methods return false if you do not wish to create an Open or Open Default
     * option in the File menu of your view.
     */
    public void addFileMenu() {
        IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        if (file == null) {
            file = new MenuManager(Messages.getString("SystemTapView.FileMenu")); //$NON-NLS-1$
            menu.add(file);
        }

        if (createOpenAction()) {
            file.add(openFile);
        }
        if (createOpenDefaultAction()) {
            file.add(openDefault);
        }

        createSaveAction();
        file.add(saveFile);
    }


    public void addHelpMenu() {
        IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        IMenuManager help = new MenuManager(Messages.getString("SystemTapView.Help")); //$NON-NLS-1$
        menu.add(help);
        createHelpActions();

        help.add(helpVersion);
    }


    private void createHelpActions() {
        helpVersion = new Action(Messages.getString("SystemTapView.Version")) { //$NON-NLS-1$
            @Override
            public void run() {
                try {
                    Process pr = RuntimeProcessFactory.getFactory().exec("stap -V", null); //$NON-NLS-1$
					BufferedReader buf = pr.errorReader();
                    String line = ""; //$NON-NLS-1$
                    String message = ""; //$NON-NLS-1$

                    while ((line = buf.readLine()) != null) {
                        message += line + NEW_LINE;
                    }

                    try {
                        pr.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Shell sh = new Shell();

                    MessageDialog.openInformation(sh, Messages
                            .getString("SystemTapView.StapVersion"), message); //$NON-NLS-1$

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void createSaveAction() {
        //Save callgraph.out
        saveFile = new Action(Messages.getString("SystemTapView.SaveMenu")){ //$NON-NLS-1$
            @Override
            public void run(){
                Shell sh = new Shell();
                FileDialog dialog = new FileDialog(sh, SWT.SAVE);
                String filePath = dialog.open();

                if (filePath != null) {
                    saveData(filePath);
                }
            }
        };
    }


    protected void addKillButton() {
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		kill = new Action(Messages.getString("SystemTapView.StopScript"), //$NON-NLS-1$
				ResourceLocator.imageDescriptorFromBundle(CallgraphCorePlugin.PLUGIN_ID, "icons/progress_stop.gif") //$NON-NLS-1$
						.get()) {
			@Override
			public void run() {
				getParser().cancelJob();
			}
		};
        mgr.add(kill);
        setKillButtonEnabled(false);
    }

    public void setKillButtonEnabled(boolean val) {
        if (kill != null) {
            kill.setEnabled(val);
        }
    }


    /**
     * Implement this method to save data in whichever format your program
     * needs. Keep in mind that the filePath variable should contain the
     * filePath of the most recently opened file.
     *
     * @param sourcePath
     */
    public void saveData(String targetFile) {
        try {
            File file = new File(targetFile);
            file.delete();
            file.createNewFile();

            File sFile = new File(sourcePath);
            if (!sFile.exists()) {
                return;
            }

            try  (FileInputStream fileIn = new FileInputStream(sFile); FileOutputStream fileOut = new FileOutputStream(file);
                    FileChannel channelIn = fileIn.getChannel(); FileChannel channelOut = fileOut.getChannel()){

                if (channelIn == null || channelOut == null) {
                    return;
                }

                long size = channelIn.size();
                MappedByteBuffer buf = channelIn.map(
                        FileChannel.MapMode.READ_ONLY, 0, size);

                channelOut.write(buf);
            }
        } catch (IOException e) {
            CallgraphCorePlugin.logException(e);
        }
    }

    public void setSourcePath(String file) {
        sourcePath = file;
    }

}
