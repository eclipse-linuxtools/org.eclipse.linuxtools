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
package org.eclipse.linuxtools.internal.rpm.ui.editor.outline;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

public class SpecfileQuickOutlineDialog extends PopupDialog {

	TreeViewer treeViewer;
	private Text filterText;
	private SpecfileEditor editor;
	private ContainsFilter treeViewerFilter;

	public SpecfileQuickOutlineDialog(Shell parent, int shellStyle,
			SpecfileEditor editor) {
		super(parent, shellStyle, true, true, true, true, true, null, null);
		this.editor = editor;
		create();
	}

	public void setSize(int width, int height) {
		getShell().setSize(width, height);
	}

	public void setVisible(boolean visible) {
		if (visible) {
			open();
			filterText.setFocus();
		} else {
			saveDialogBounds(getShell());
			getShell().setVisible(false);
		}
	}

	public void dispose() {
		close();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		createUIWidgetTreeViewer(parent);
		createUIListenersTreeViewer();
		return treeViewer.getControl();
	}

	@Override
	protected Control createTitleControl(Composite parent) {
		filterText = new Text(parent, SWT.NONE);
		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true,
				false).hint(SWT.DEFAULT,
				Dialog.convertHeightInCharsToPixels(fontMetrics, 1)).applyTo(
				filterText);

		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 0x0D) { // Enter pressed
					gotoSelectedElement();
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					treeViewer.getTree().setFocus();
				} else if (e.keyCode == SWT.ARROW_UP) {
					treeViewer.getTree().setFocus();
				} else if (e.character == 0x1B) { // Escape pressed
					dispose();
				}
			}
		});
		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String filterTextInput = ((Text) e.widget).getText()
						.toLowerCase();
				treeViewerFilter.setLookFor(filterTextInput);
				stringMatcherUpdated();
			}
		});
		return filterText;
	}

	private void stringMatcherUpdated() {
		treeViewer.getControl().setRedraw(false);
		treeViewer.refresh();
		treeViewer.expandAll();
		if(treeViewer.getTree().getTopItem() != null && treeViewer.getTree().getTopItem().getItemCount() > 0) {
			treeViewer.getTree().select(treeViewer.getTree().getTopItem().getItem(0));
		} else if(treeViewer.getTree().getItemCount()>0) {
			treeViewer.getTree().select(treeViewer.getTree().getItem(0));
		}
		treeViewer.getControl().setRedraw(true);
	}

	private void createUIWidgetTreeViewer(Composite parent) {
		final int style = SWT.H_SCROLL | SWT.V_SCROLL;
		final Tree widget = new Tree(parent, style);
		final GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = widget.getItemHeight() * 12;
		widget.setLayoutData(data);
		treeViewer = new TreeViewer(widget);
		treeViewerFilter = new ContainsFilter();
		treeViewer.addFilter(treeViewerFilter);
		SpecfileContentProvider fOutlineContentProvider = new SpecfileContentProvider(
				editor);
		treeViewer.setContentProvider(fOutlineContentProvider);
		SpecfileLabelProvider fTreeLabelProvider = new SpecfileLabelProvider();
		treeViewer.setLabelProvider(fTreeLabelProvider);
		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		treeViewer.setUseHashlookup(true);
		treeViewer.setInput(fOutlineContentProvider);
	}

	private void createUIListenersTreeViewer() {
		final Tree tree = treeViewer.getTree();
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				gotoSelectedElement();
			}
		});
	}

	private void gotoSelectedElement() {
		final SpecfileElement curElement = (SpecfileElement) getSelectedElement();
		if (curElement == null) {
			return;
		}
		dispose();
		editor.setHighlightRange(curElement.getLineStartPosition(), 1, true);
	}

	
	private Object getSelectedElement() {
		if (treeViewer == null) {
			return null;
		}
		return ((IStructuredSelection) treeViewer.getSelection())
				.getFirstElement();
	}
}
