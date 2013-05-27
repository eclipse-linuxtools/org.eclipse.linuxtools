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

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindError;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindStackFrame;
import org.eclipse.linuxtools.profiling.ui.ProfileUIUtils;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class CoreMessagesViewer {

	static ImageRegistry imageRegistry = new ImageRegistry();

	public static final String VALGRIND_ERROR = "Valgrind_Error"; //$NON-NLS-1$
	/**
	 * @since 0.10
	 */
	public static final String VALGRIND_INFO = "Valgrind_Info"; //$NON-NLS-1$
	public static final String VALGRIND_ERROR_IMAGE = "icons/valgrind-error.png"; //$NON-NLS-1$
	/**
	 * @since 0.10
	 */
	public static final String VALGRIND_INFO_IMAGE = "icons/valgrind-info.png"; //$NON-NLS-1$
	public IDoubleClickListener doubleClickListener;
	public ITreeContentProvider contentProvider;
	public IAction expandAction;
	public IAction collapseAction;

	private TreeViewer viewer;

	public CoreMessagesViewer(Composite parent, int style) {
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | style);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		if (imageRegistry.getDescriptor(VALGRIND_ERROR) == null) {
			ImageDescriptor d = AbstractUIPlugin.imageDescriptorFromPlugin(ValgrindUIPlugin.PLUGIN_ID, VALGRIND_ERROR_IMAGE);
			if (d != null) {
				imageRegistry.put(VALGRIND_ERROR, d);
			}
		}
		if (imageRegistry.getDescriptor(VALGRIND_INFO) == null) {
			ImageDescriptor d = AbstractUIPlugin.imageDescriptorFromPlugin(ValgrindUIPlugin.PLUGIN_ID, VALGRIND_INFO_IMAGE);
			if (d != null) {
				imageRegistry.put(VALGRIND_INFO, d);
			}
		}
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
		viewer.setContentProvider(contentProvider);

		viewer.setLabelProvider(new LabelProvider() {
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
				else if (element instanceof ValgrindError)  {
					image = imageRegistry.get(VALGRIND_ERROR);
				}
				else {
					image = imageRegistry.get(VALGRIND_INFO);
				}
				return image;
			}

		});

		doubleClickListener = new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				Object element = ((TreeSelection) event.getSelection()).getFirstElement();
				if (element instanceof ValgrindStackFrame) {
					ValgrindStackFrame frame = (ValgrindStackFrame) element;
					ILaunch launch = frame.getLaunch();
					ISourceLocator locator = launch.getSourceLocator();
					if (locator instanceof AbstractSourceLookupDirector) {
						AbstractSourceLookupDirector director = (AbstractSourceLookupDirector) locator;
						ISourceLookupParticipant[] participants = director.getParticipants();
						if (participants.length == 0) {
							// source locator likely disposed, try recreating it
							IPersistableSourceLocator sourceLocator;
							ILaunchConfiguration config = launch.getLaunchConfiguration();
							if (config != null) {
								try {
									String id = config.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String) null);
									if (id == null) {
										sourceLocator = CDebugUIPlugin.createDefaultSourceLocator();
										sourceLocator.initializeDefaults(config);
									} else {
										sourceLocator = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(id);
										String memento = config.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String) null);
										if (memento == null) {
											sourceLocator.initializeDefaults(config);
										} else {
											sourceLocator.initializeFromMemento(memento);
										}
									}

									// replace old source locator
									locator = sourceLocator;
									launch.setSourceLocator(sourceLocator);
								} catch (CoreException e) {
									e.printStackTrace();
								}
							}
						}
					}
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
					if (viewer.getExpandedState(element)) {
						viewer.collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
					}
					else {
						viewer.expandToLevel(element, 1);
					}
				}
			}
		};
		viewer.addDoubleClickListener(doubleClickListener);

		expandAction = new ExpandAction(viewer);
		collapseAction = new CollapseAction(viewer);

		MenuManager manager = new MenuManager();
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ITreeSelection selection = (ITreeSelection) viewer.getSelection();
				Object element = selection.getFirstElement();
				if (contentProvider.hasChildren(element)) {
					manager.add(expandAction);
					manager.add(collapseAction);
				}
			}
		});

		manager.setRemoveAllWhenShown(true);
		Menu contextMenu = manager.createContextMenu(viewer.getTree());
		viewer.getControl().setMenu(contextMenu);
	}

	public IDoubleClickListener getDoubleClickListener() {
		return doubleClickListener;
	}

	public TreeViewer getTreeViewer() {
		return viewer;
	}
}