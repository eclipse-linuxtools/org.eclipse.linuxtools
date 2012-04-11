/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.hidden;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor;
import org.eclipse.linuxtools.systemtap.ui.ide.IDESessionSettings;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;



import java.io.File;

/**
 * This <code>Action</code> is raised by <code>KernelBrowserView</code> whenever the user selects
 * an item in the view (usually by double clicking). This <code>Action</code> either passes
 * the event on to <code>TreeExpandCollapseAction</code> if the selection is not clickable, or
 * it opens a new CEditor for the file selected if the selection is clickable.
 * @author Henry Hughes
 * @author Ryan Morse
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c.CEditor
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.hidden.TreeExpandCollapseAction
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView
 */
public class KernelSourceAction extends Action implements ISelectionListener, IWorkbenchAction {
	private final IWorkbenchWindow window;
	public final static String ID = "org.eclipse.linuxtools.systemtap.ui.ide.KBAction";
	private KernelBrowserView viewer;
	private IStructuredSelection selection;
	private TreeExpandCollapseAction expandAction;

	/**
	 * The default constructor for the <code>KernelSourceAction</code>. Takes the window that it affects
	 * and the <code>KernelBrowserView</code> that will fire the event as arguments.
	 * @param window	The <code>IWorkbenchWindow</code> that the action operates on.
	 * @param browser	The <code>KernelBrowserView</code> that fires this action.
	 */
	public KernelSourceAction(IWorkbenchWindow window, KernelBrowserView browser) {
		LogManager.logDebug("Start KernelSourceAction: window-" + window + ", browser-" + browser, this);
		LogManager.logInfo("Initializing", this);
		this.window = window;
		setId(ID);
		setActionDefinitionId(ID);
		setText(Localization.getString("KernelSourceAction.Insert"));
		setToolTipText(Localization.getString("KernelSourceAction.InsertSelectedFunction"));
		window.getSelectionService().addSelectionListener(this);
		viewer = browser;
		expandAction = new TreeExpandCollapseAction(KernelBrowserView.class);
		LogManager.logDebug("End KernelSourceAction:", this);
	}
	
	/**
	 * Updates <code>selection</code> with the current selection whenever the user changes
	 * the current selection.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		if (incoming instanceof IStructuredSelection) {
			LogManager.logDebug("Changing selection", this);
			selection = (IStructuredSelection) incoming;
			setEnabled(selection.size() == 1);
		} else {
			LogManager.logDebug("Disabling, selection not IStructuredSelection", this);
			// Other selections, for example containing text or of other kinds.
			setEnabled(false);
		}
	}

	public void dispose() {
		LogManager.logInfo("Disposing", this);
		window.getSelectionService().removeSelectionListener(this);
	}

	/**
	 * Creates a <code>PathEditorInput</code> for the file specified.
	 * @param file	The <code>File</code> to create an input for.
	 * @return	A <code>PathEditorInput</code> that represents the requested file.
	 */
	private IEditorInput createEditorInput(File file) {
		IPath location= new Path(file.getAbsolutePath());
		PathEditorInput input= new PathEditorInput(location, window);
		LogManager.logDebug("createEditorInput: returnVal-" + input, this);
		return input;
	}
	
	/**
	 * Returns the ID of the editor to use for the requested file. Usually returns
	 * <code>CEditor.ID</code> in this code.
	 * @param file The file to get the ID for.
	 * @return	The ID for the editor that handles the requested file type.
	 */
	private String getEditorId(File file) {
		IWorkbench workbench= window.getWorkbench();
		IEditorRegistry editorRegistry= workbench.getEditorRegistry();
		IEditorDescriptor[] descriptors= editorRegistry.getEditors(file.getName());
		for (IEditorDescriptor d : descriptors)
			if (d.getId().startsWith("org.eclipse.linuxtools.systemtap.ui.ide.editors") ||
				d.getId().startsWith("org.eclipse.linuxtools.internal.systemtap.ui.ide.editors")) {
				LogManager.logDebug("getEditorId: returnVal-" + d.getId(), this);
				return d.getId();
			}
		LogManager.logDebug("getEditorId: returnVal-...SimpleEditor", this);
		return SimpleEditor.ID;
	}
	
	/**
	 * The main code body for this action. Causes one of the following to occur:
	 * <ul>
	 * 	<li>If the selected node is clickable, as specified in <code>TreeNode.isClickable</code>
	 * 		the browser creates an instance of <code>CEditor</code> on the file specified in the selection
	 * 		(<code>KernelBrowserView</code>'s tree only marks clickable on files, not folders) and
	 * 		opens it on the current window</li>
	 * 	<li>If the selected node is not clickable, the code runs the action specified in
	 * 		<code>TreeExpandCollapseAction</code></li>
	 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c.CEditor
	 * @see TreeNode#isClickable()
	 * @see TreeExpandCollapseAction
	 */
	public void run() {
		LogManager.logDebug("Start run", this);
		IWorkbench wb = PlatformUI.getWorkbench();
		ISelection incoming = viewer.getViewer().getSelection();
		IStructuredSelection selection = (IStructuredSelection)incoming;
		Object o  = selection.getFirstElement();
		if(o instanceof TreeNode) {
			TreeNode t = (TreeNode)o;
			if(t.isClickable()) {
				
				File file = (File)t.getData();
				if (file != null) {
					IEditorInput input= createEditorInput(file);
					String editorId= getEditorId(file);
					try {
						IEditorPart editor = wb.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
						if(editor instanceof STPEditor)
							IDESessionSettings.activeSTPEditor = (STPEditor)editor;
						wb.getActiveWorkbenchWindow().getActivePage().openEditor(input, editorId);
						LogManager.logDebug("Editor opened", this);
					} catch (PartInitException e) {
						LogManager.logCritical("PartInitException run: " + e.getMessage(), this);
					}
					
				}
			}
			else
			{
				
				expandAction.run();
			}
		}
		LogManager.logDebug("End run", this);
	}
}
