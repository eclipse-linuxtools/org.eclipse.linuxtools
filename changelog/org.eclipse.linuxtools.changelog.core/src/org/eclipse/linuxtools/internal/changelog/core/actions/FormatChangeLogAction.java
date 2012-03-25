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
package org.eclipse.linuxtools.internal.changelog.core.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.linuxtools.internal.changelog.core.ChangelogPlugin;
import org.eclipse.linuxtools.internal.changelog.core.editors.ChangeLogEditor;
import org.eclipse.ui.IWorkbench;



public class FormatChangeLogAction extends Action implements IHandler {


	ChangeLogEditor editor = null;

	public FormatChangeLogAction(ChangeLogEditor te) {
		super("Format ChangeLog");
		editor = te;
	}

	public FormatChangeLogAction() {
		super("Format ChangeLog");
		//editor = te;
		try {
			editor = (ChangeLogEditor)getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().getActiveEditor();
		} catch (Exception e) {
			// no editor is active now so do nothing
		return;
		}
	}
	protected IWorkbench getWorkbench() {
		return ChangelogPlugin.getDefault().getWorkbench();
	}
	@Override
	public void run() {

		if (editor == null)
			return;

		SourceViewer srcViewer = (SourceViewer)editor.getMySourceViewer();
		if (srcViewer != null) {
			srcViewer.doOperation(ISourceViewer.FORMAT);

		}

	}

	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		run();
		
		return null;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
		
	}

}
