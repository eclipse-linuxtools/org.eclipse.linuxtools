/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.massif;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.valgrind.ui.CollapseAction;
import org.eclipse.linuxtools.internal.valgrind.ui.ExpandAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

public class MassifTreeViewer extends TreeViewer {

	protected IDoubleClickListener doubleClickListener;
	protected ITreeContentProvider contentProvider;
	protected Action expandAction;
	protected Action collapseAction;

	public MassifTreeViewer(Composite parent) {
		super(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		
		contentProvider = new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) {
				return ((MassifHeapTreeNode) parentElement).getChildren();
			}

			public Object getParent(Object element) {
				return ((MassifHeapTreeNode) element).getParent();
			}

			public boolean hasChildren(Object element) {
				MassifHeapTreeNode[] children = ((MassifHeapTreeNode) element).getChildren();
				return children != null && children.length > 0;
			}

			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}

			public void dispose() {}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {}

		};
		setContentProvider(contentProvider);

		setLabelProvider(new MassifTreeLabelProvider());

		doubleClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				MassifHeapTreeNode element = (MassifHeapTreeNode) ((TreeSelection) event.getSelection()).getFirstElement();
				if (element.hasSourceFile()) {
					MassifPlugin.getDefault().openEditorForNode(element);
				} 
				if (contentProvider.hasChildren(element)) {
					expandToLevel(element, 1);
				}
			}			
		};
		addDoubleClickListener(doubleClickListener);
		
		expandAction = new ExpandAction(this);
		collapseAction = new CollapseAction(this);
		
		MenuManager manager = new MenuManager();
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ITreeSelection selection = (ITreeSelection) getSelection();
				MassifHeapTreeNode element = (MassifHeapTreeNode) selection.getFirstElement();
				if (contentProvider.hasChildren(element)) {
					manager.add(expandAction);
					manager.add(collapseAction);
				}
			}			
		});
		
		manager.setRemoveAllWhenShown(true);	
		Menu contextMenu = manager.createContextMenu(getTree());
		getControl().setMenu(contextMenu);
	}

	public IDoubleClickListener getDoubleClickListener() {
		return doubleClickListener;
	}

}
