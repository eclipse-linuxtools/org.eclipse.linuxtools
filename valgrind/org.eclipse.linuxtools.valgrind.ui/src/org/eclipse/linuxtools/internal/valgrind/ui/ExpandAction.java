/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;

public class ExpandAction extends Action {
	
	protected TreeViewer viewer;

	public ExpandAction(TreeViewer viewer) {
		super(Messages.getString("ExpandAction.Text")); //$NON-NLS-1$
		this.viewer = viewer;
	}
	
	@Override
	public void run() {
		ITreeSelection selection = (ITreeSelection) viewer.getSelection();
		Object element = selection.getFirstElement();
		viewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
	}
	
}
