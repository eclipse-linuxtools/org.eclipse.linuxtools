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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.linuxtools.internal.changelog.core.editors.ChangeLogEditor;
import org.eclipse.ui.handlers.HandlerUtil;



public class FormatChangeLogAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		ChangeLogEditor editor = (ChangeLogEditor) HandlerUtil.getActiveEditor(event);
		if (editor == null)
			return null;

		SourceViewer srcViewer = (SourceViewer)editor.getMySourceViewer();
		if (srcViewer != null) {
			srcViewer.doOperation(ISourceViewer.FORMAT);

		}

		return null;
	}

}
