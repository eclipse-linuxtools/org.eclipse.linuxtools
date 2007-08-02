/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.changelog.core.ChangelogPlugin;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IContributorResourceAdapter2;


/**
 * 
 * @author klee
 *
 */
public class PrepareCommitAction extends ChangeLogAction {

	
	protected void doRun() {
		

		IRunnableWithProgress code = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
			//	monitor.beginTask("Loading Clipboard", 1000);
				loadClipboard(monitor);
				//monitor.done();
			}
		};

		ProgressMonitorDialog pd = new ProgressMonitorDialog(getWorkbench()
				.getActiveWorkbenchWindow().getShell());

		try {
			pd.run(false /* fork */, false /* cancelable */, code);
		} catch (InvocationTargetException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR, e
							.getMessage(), e));
			return;
		} catch (InterruptedException e) {
			ChangelogPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, "Changelog", IStatus.ERROR, e
							.getMessage(), e));
			return;
		}
		
	//	loadClipboard();
		
	}
	
	
	private ResourceMapping getResourceMapping(Object o) {
		if (o instanceof ResourceMapping) {
			return (ResourceMapping) o;
		}
		if (o instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) o;
			Object adapted = adaptable.getAdapter(ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return (ResourceMapping) adapted;
			}
			adapted = adaptable.getAdapter(IContributorResourceAdapter.class);
			if (adapted instanceof IContributorResourceAdapter2) {
				IContributorResourceAdapter2 cra = (IContributorResourceAdapter2) adapted;
				return cra.getAdaptedResourceMapping(adaptable);
			}
		} else {
			Object adapted = Platform.getAdapterManager().getAdapter(o,
					ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return (ResourceMapping) adapted;
			}
		}
		
	
		return null;
	}
	
	private void loadClipboard(IProgressMonitor monitor) {
		
		IEditorPart currentEditor;
		
		try {
			currentEditor = getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().getActiveEditor();
		} catch (Exception e) {
			// no editor is active now so do nothing
			
			return;
		}
		
		if (currentEditor == null)
			return;
			
		
		
	//	System.out.println(currentEditor.getTitle());
		String diffResult;

		
		try {

			StringDiffOperation sdo = new StringDiffOperation(getWorkbench()
					.getActiveWorkbenchWindow().getPartService()
					.getActivePart(), new ResourceMapping[] {getResourceMapping(currentEditor.getEditorInput())},
					 false, true,
					ResourcesPlugin.getWorkspace().getRoot().getFullPath());
			//NullProgressMonitor np = new NullProgressMonitor();
			sdo.execute(monitor);
			
			diffResult = sdo.getResult();

		} catch (Exception e) {

			e.printStackTrace();
			return;
		}
		
		
		populateClipboardBuffer(extractNewLines(diffResult));
		
		
		
	}
	
	private String extractNewLines(String input) {
		
	//	System.out.println(input);
		String output = "";
		StringTokenizer st = new StringTokenizer(input, "\n");
		while(st.hasMoreTokens()) {
			String cr;
			if ((cr = st.nextToken()).startsWith("> "))
				output+=cr.substring(2,cr.length()) + "\n";
		}
	//	System.out.println("\n\n" + output);
		return output;
	}
	
	
	private void populateClipboardBuffer(String input) {
		
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		Clipboard clipboard = new Clipboard(getWorkbench().getDisplay());		
		clipboard.setContents(
			new String[]{input}, 
			new Transfer[]{plainTextTransfer});	
		clipboard.dispose();
	}
	
	
	
}
