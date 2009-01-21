/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    IBM Corporation - painting text over multiple cells
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.cachegrind;

import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFunction;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindLine;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.cachegrind.model.ICachegrindElement;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class CachegrindViewPart extends ViewPart implements IValgrindToolView {

	protected CachegrindOutput output;
	protected TreeViewer viewer;

	protected static final int COLUMN_SIZE = 50;
	protected CellLabelProvider labelProvider;
	
	protected static Image FUNC_IMG = CachegrindPlugin.imageDescriptorFromPlugin(CachegrindPlugin.PLUGIN_ID, "icons/function_obj.gif").createImage(); //$NON-NLS-1$
	protected static Image LINE_IMG = CachegrindPlugin.imageDescriptorFromPlugin(CachegrindPlugin.PLUGIN_ID, "icons/line.gif").createImage(); //$NON-NLS-1$

	@Override
	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer = new TreeViewer(top, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION);

		labelProvider = new CachegrindLabelProvider();

		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));

		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(Messages.getString("CachegrindViewPart.Location")); //$NON-NLS-1$
		column.getColumn().setWidth(COLUMN_SIZE * 4);
		column.getColumn().setResizable(true);
		column.setLabelProvider(labelProvider);

		viewer.setContentProvider(new CachegrindTreeContentProvider());
		viewer.setLabelProvider(labelProvider);
	}

	@Override
	public void setFocus() {
		viewer.getTree().setFocus();
	}

	public IAction[] getToolbarActions() {
		return null;
	}

	public void refreshView() {
		if (output != null) {
			viewer.setInput(output);
			String[] events = output.getEvents();
			for (int i = 0; i < events.length; i++) {
				TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(events[i]);
				column.getColumn().setWidth(COLUMN_SIZE);
				column.getColumn().setResizable(true);
				column.setLabelProvider(labelProvider);
			}
			viewer.getTree().layout(true);
		}
	}

	public void setOutput(CachegrindOutput output) {
		this.output = output;
	}

	public CachegrindOutput getOutput() {
		return output;
	}

	protected class CachegrindTreeContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			return ((ICachegrindElement) parentElement).getChildren();
		}

		public Object getParent(Object element) {
			return ((ICachegrindElement) element).getParent();
		}

		public boolean hasChildren(Object element) {
			ICachegrindElement[] children = (ICachegrindElement[]) getChildren(element);
			return children != null && children.length > 0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	protected class CachegrindLabelProvider extends CellLabelProvider {

		protected CElementLabelProvider cLabelProvider = new CElementLabelProvider(CElementLabelProvider.SHOW_SMALL_ICONS | CElementLabelProvider.SHOW_POST_QUALIFIED | CElementLabelProvider.SHOW_PARAMETERS | CElementLabelProvider.SHOW_RETURN_TYPE);

		@Override
		public void update(ViewerCell cell) {
			ICachegrindElement element = ((ICachegrindElement) cell.getElement());
			int index = cell.getColumnIndex();

			if (index == 0) {
				if (element instanceof CachegrindFile) {
					// Try to use the CElementLabelProvider
					IAdaptable model = ((CachegrindFile) element).getModel();
					if (model != null) {
						cell.setText(cLabelProvider.getText(model));
						cell.setImage(cLabelProvider.getImage(model));
					}
					else { // Fall back
						String path = ((CachegrindFile) element).getPath();
						cell.setText(path);
						cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE));
					}
				}
				else if (element instanceof CachegrindFunction) {
					// Try to use the CElementLabelProvider
					IAdaptable model = ((CachegrindFunction) element).getModel();
					if (model != null) {
						cell.setText(cLabelProvider.getText(model));
						cell.setImage(cLabelProvider.getImage(model));
					}
					else { // Fall back
						String name = ((CachegrindFunction) element).getName();
						cell.setText(name);
						cell.setImage(FUNC_IMG);
					}
				}
				else if (element instanceof CachegrindLine) {
					cell.setText(NLS.bind(Messages.getString("CachegrindViewPart.line"), ((CachegrindLine) element).getLine())); //$NON-NLS-1$
					cell.setImage(LINE_IMG);
				}
			}
			else if (element instanceof CachegrindLine) {
				cell.setText(String.valueOf(((CachegrindLine) element).getValues()[index - 1]));
			}
		}

	}

}
