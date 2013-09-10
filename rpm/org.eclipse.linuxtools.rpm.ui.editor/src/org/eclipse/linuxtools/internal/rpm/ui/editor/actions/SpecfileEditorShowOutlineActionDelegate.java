/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.linuxtools.internal.rpm.ui.editor.outline.SpecfileQuickOutlineDialog;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.handlers.HandlerUtil;

public class SpecfileEditorShowOutlineActionDelegate extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		SpecfileEditor editor = (SpecfileEditor) HandlerUtil
				.getActiveEditor(event);
		SpecfileQuickOutlineDialog quickOutlinePopupDialog = new SpecfileQuickOutlineDialog(
				editor.getSite().getShell(), SWT.NONE, editor);
		quickOutlinePopupDialog.setSize(400, 200);
		quickOutlinePopupDialog.setVisible(true);
		return null;
	}

}
