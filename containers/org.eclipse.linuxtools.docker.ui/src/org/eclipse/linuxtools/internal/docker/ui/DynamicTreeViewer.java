/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
