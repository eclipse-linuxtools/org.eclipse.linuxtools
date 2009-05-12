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
package org.eclipse.linuxtools.valgrind.ui;

import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.profiling.ui.ProfileUIUtils;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.core.ValgrindStackFrame;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class CoreMessagesViewer extends TreeViewer {
	public IDoubleClickListener doubleClickListener;
	public ITreeContentProvider contentProvider;
	public IAction expandAction;
	public IAction collapseAction;

	public CoreMessagesViewer(Composite parent, int style) {
		super(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | style);
		getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		contentProvider = new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Object[]) {
					return (Object[]) parentElement;
				}
				return ((IValgrindMessage) parentElement).getChildren();
			}

			public Object getParent(Object element) {
				return ((IValgrindMessage) element).getParent();
			}

			public boolean hasChildren(Object element) {
				return getChildren(element).length > 0;
			}

			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			public void dispose() {}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {}

		};
		setContentProvider(contentProvider);

		setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IValgrindMessage) element).getText();
			}

			@Override
			public Image getImage(Object element) {
				Image image;
				if (element instanceof ValgrindStackFrame) {
					image = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_STACKFRAME);
				}
				else {
					image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
				}
				return image;
			}

		});

		doubleClickListener = new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				Object element = ((TreeSelection) event.getSelection()).getFirstElement();
				if (element instanceof ValgrindStackFrame) {
					ValgrindStackFrame frame = (ValgrindStackFrame) element;
					ISourceLocator locator = frame.getLaunch().getSourceLocator();					
					ISourceLookupResult result = DebugUITools.lookupSource(frame.getFile(), locator);				
					
					try {
						ProfileUIUtils.openEditorAndSelect(result, frame.getLine());
					} catch (PartInitException e) {
						e.printStackTrace();
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
				else {
					if (getExpandedState(element)) {
						collapseToLevel(element, TreeViewer.ALL_LEVELS);
					}
					else {
						expandToLevel(element, 1);
					}
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
				Object element = selection.getFirstElement();
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