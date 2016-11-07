/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * Workbench-level composite that shows a dynamic tree. All viewer
 * selection-driven interactions are handled within this object
 */
@SuppressWarnings("rawtypes")
public class ContainerTreeGroup extends EventManager implements
		ISelectionChangedListener, ITreeViewerListener {
	private Object root;

	private Object currentTreeSelection;

	private List expandedTreeNodes = new ArrayList();

	private ITreeContentProvider treeContentProvider;

	private ITreeContentProvider dynamicTreeContentProvider;

	private ILabelProvider treeLabelProvider;

	// widgets
	private TreeViewer treeViewer;

	/**
	 * Create an instance of this class. Use this constructor if you wish to
	 * specify the width and/or height of the combined widget (to only hardcode
	 * one of the sizing dimensions, specify the other dimension's value as -1)
	 * 
	 * @param parent
	 * @param rootObject
	 * @param treeContentProvider
	 * @param treeLabelProvider
	 * @param listContentProvider
	 * @param listLabelProvider
	 * @param style
	 * @param width
	 * @param height
	 */
	public ContainerTreeGroup(Composite parent, Object rootObject,
			ITreeContentProvider treeContentProvider,
			ITreeContentProvider dynamicTreeContentProvider,
			ILabelProvider treeLabelProvider,
			int style, int width,
			int height) {

		root = rootObject;
		this.treeContentProvider = treeContentProvider;
		this.dynamicTreeContentProvider = dynamicTreeContentProvider;
		this.treeLabelProvider = treeLabelProvider;
		createContents(parent, width, height, style);
	}

	/**
	 * This method must be called just before this window becomes visible.
	 */
	public void aboutToOpen() {
		currentTreeSelection = null;

		// select the first element in the list
		Object[] elements = treeContentProvider.getElements(root);
		Object primary = elements.length > 0 ? elements[0] : null;
		if (primary != null) {
			treeViewer.setSelection(new StructuredSelection(primary));
		}
		treeViewer.getControl().setFocus();
	}

	/**
	 * Lay out and initialize self's visual components.
	 *
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 * @param width
	 *            int
	 * @param height
	 *            int
	 */
	protected void createContents(Composite parent, int width, int height,
			int style) {
		// group pane
		Composite composite = new Composite(parent, style);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());

		createTreeViewer(composite, width, height);
		initialize();
	}

	/**
	 * Create this group's tree viewer.
	 */
	protected void createTreeViewer(Composite parent, int width, int height) {
		Tree tree = new Tree(parent, SWT.BORDER | SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = width;
		data.heightHint = height;
		tree.setLayoutData(data);
		tree.setFont(parent.getFont());

		treeViewer = new DynamicTreeViewer(tree,
				dynamicTreeContentProvider);
		treeViewer.setContentProvider(treeContentProvider);
		treeViewer.setLabelProvider(treeLabelProvider);
		treeViewer.addTreeListener(this);
		treeViewer.addSelectionChangedListener(this);
	}

	/**
	 * Cause the tree viewer to expand all its items
	 */
	public void expandAll() {
		treeViewer.expandAll();
	}

	/**
	 * Initialize this group's viewers after they have been laid out.
	 */
	protected void initialize() {
		treeViewer.setInput(root);
	}


	/**
	 * Notify all selection state listeners that a selection has been made
	 */
	protected void notifySelectionChangeListeners(
			final SelectionChangedEvent event) {
		Object[] array = getListeners();
		for (int i = 0; i < array.length; i++) {
			final ISelectionChangedListener l = (ISelectionChangedListener) array[i];
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					l.selectionChanged(event);
				}
			});
		}
	}

	/**
	 * Add the passed listener to self's collection of clients that listen for
	 * changes to selection
	 *
	 * @param listener
	 *            ISelectionChangedListener
	 */
	public void addSelectionChangedListener(
			ISelectionChangedListener listener) {
		addListenerObject(listener);
	}

	/**
	 * Remove the passed listener from self's collection of clients that listen
	 * for changes to selection
	 *
	 * @param listener
	 *            ISelectionChangedListener
	 */
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		removeListenerObject(listener);
	}

	/**
	 * Handle the selection of an item in the tree viewer
	 *
	 * @param event
	 *            SelectionChangedEvent
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event
				.getSelection();
		Object selectedElement = selection.getFirstElement();
		currentTreeSelection = selectedElement;
		notifySelectionChangeListeners(event);
	}

	public Object getCurrentSelection() {
		return currentTreeSelection;
	}

	/**
	 * Set the root of the widget to be new Root. Regenerate all of the tables
	 * and lists from this value.
	 * 
	 * @param newRoot
	 */
	public void setRoot(Object newRoot) {
		this.root = newRoot;
		initialize();
	}

	/**
	 * Set the tree viewer's providers to those passed
	 *
	 * @param contentProvider
	 *            ITreeContentProvider
	 * @param labelProvider
	 *            ILabelProvider
	 */
	public void setTreeProviders(ITreeContentProvider contentProvider,
			ILabelProvider labelProvider) {
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);
	}

	/**
	 * Set the comparator that is to be applied to self's tree viewer
	 *
	 * @param comparator
	 *            the comparator for the tree
	 */
	public void setTreeComparator(ViewerComparator comparator) {
		treeViewer.setComparator(comparator);
	}


	/**
	 * Handle the collapsing of an element in a tree viewer
	 */
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		// We don't need to do anything with this
	}

	/**
	 * Handle the expansionsion of an element in a tree viewer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void treeExpanded(TreeExpansionEvent event) {

		Object item = event.getElement();

		// First see if the children need to be given their checked state at
		// all. If they've
		// already been realized then this won't be necessary
		if (!expandedTreeNodes.contains(item)) {
			expandedTreeNodes.add(item);
			dynamicTreeContentProvider.getChildren(item);
			Object[] children = treeContentProvider.getElements(item);
			for (int i = 0; i < children.length; ++i) {
				dynamicTreeContentProvider.getElements(children[i]);
			}
		}
	}

}

