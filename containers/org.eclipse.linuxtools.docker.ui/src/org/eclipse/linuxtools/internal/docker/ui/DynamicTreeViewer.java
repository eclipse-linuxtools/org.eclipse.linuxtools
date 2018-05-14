/*******************************************************************************
 * Copyright (c) 2016,2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.widgets.Tree;

public class DynamicTreeViewer extends TreeViewer {

	private final ITreeContentProvider dynamicProvider;
	private boolean useDynamic;

	public DynamicTreeViewer(Tree tree,
			ITreeContentProvider dynamicProvider) {
		super(tree);
		this.dynamicProvider = dynamicProvider;
	}

	public void useDynamic(boolean value) {
		this.useDynamic = value;
	}

	@Override
	public IContentProvider getContentProvider() {
		if (useDynamic) {
			return dynamicProvider;
		}
		return super.getContentProvider();
	}

	@Override
	protected void handleTreeExpand(TreeEvent event) {
		useDynamic(true);
		super.handleTreeExpand(event);
		useDynamic(false);
	}

}
