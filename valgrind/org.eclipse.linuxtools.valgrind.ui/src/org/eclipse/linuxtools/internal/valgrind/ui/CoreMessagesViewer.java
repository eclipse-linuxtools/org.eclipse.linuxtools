/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.AbstractTreeViewer;
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
import org.eclipse.linuxtools.valgrind.ui.CollapseAction;
import org.eclipse.linuxtools.valgrind.ui.ExpandAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

/**
 * The inner tree view that holds the output messages to be displayed. Also hold
 * double click listeners.
 */
public class CoreMessagesViewer {

    static ImageRegistry imageRegistry = new ImageRegistry();

    private static final String VALGRIND_ERROR = "Valgrind_Error"; //$NON-NLS-1$
    /**
     * @since 0.10
     */
    private static final String VALGRIND_INFO = "Valgrind_Info"; //$NON-NLS-1$
    private static final String VALGRIND_ERROR_IMAGE = "icons/valgrind-error.png"; //$NON-NLS-1$
    /**
     * @since 0.10
     */
    public static final String VALGRIND_INFO_IMAGE = "icons/valgrind-info.png"; //$NON-NLS-1$
    private IDoubleClickListener doubleClickListener;
    private ITreeContentProvider contentProvider;

    private TreeViewer viewer;

    /**
     * @param parent  the parent control
     * @param style   an SWT style
     */
    public CoreMessagesViewer(Composite parent, int style) {
        viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | style);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        if (imageRegistry.getDescriptor(VALGRIND_ERROR) == null) {
            ImageDescriptor d = ResourceLocator.imageDescriptorFromBundle(ValgrindUIPlugin.PLUGIN_ID, VALGRIND_ERROR_IMAGE).get();
            if (d != null) {
                imageRegistry.put(VALGRIND_ERROR, d);
            }
        }
        if (imageRegistry.getDescriptor(VALGRIND_INFO) == null) {
            ImageDescriptor d = ResourceLocator.imageDescriptorFromBundle(ValgrindUIPlugin.PLUGIN_ID, VALGRIND_INFO_IMAGE).get();
            if (d != null) {
                imageRegistry.put(VALGRIND_INFO, d);
            }
        }
        contentProvider = new ITreeContentProvider() {

            @Override
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof Object[]) {
                    return (Object[]) parentElement;
                }
                return ((IValgrindMessage) parentElement).getChildren();
            }

            @Override
            public Object getParent(Object element) {
                return ((IValgrindMessage) element).getParent();
            }

            @Override
            public boolean hasChildren(Object element) {
                return getChildren(element).length > 0;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return getChildren(inputElement);
            }

            @Override
            public void dispose() {}

            @Override
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
                } else if (element instanceof ValgrindError)  {
                    image = imageRegistry.get(VALGRIND_ERROR);
                } else {
                    image = imageRegistry.get(VALGRIND_INFO);
                }
                return image;
            }

        });

		doubleClickListener = event -> {
			Object element = ((TreeSelection) event.getSelection()).getFirstElement();
			if (element instanceof ValgrindStackFrame frame) {
				// locator stored in the frame should be valid for the lifespan of the frame object
				ISourceLocator locator = frame.getSourceLocator();
				ISourceLookupResult result = DebugUITools.lookupSource(frame.getFile(), locator);
				try {
					if (result.getSourceElement() != null)
						ProfileUIUtils.openEditorAndSelect(result, frame.getLine());
					else // if lookup failed there is good chance we can just open the file by name
						ProfileUIUtils.openEditorAndSelect(frame.getFile(), frame.getLine(),
								ValgrindUIPlugin.getDefault().getProfiledProject());
				} catch (BadLocationException | CoreException e) {
					ValgrindUIPlugin.log(e);
				}
			} else {
				if (viewer.getExpandedState(element)) {
					viewer.collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
				} else {
					viewer.expandToLevel(element, 1);
				}
			 }
		};
        viewer.addDoubleClickListener(doubleClickListener);

        final ExpandAction expandAction = new ExpandAction(viewer);
        final CollapseAction collapseAction = new CollapseAction(viewer);

        MenuManager manager = new MenuManager();
        manager.addMenuListener(manager1 -> {
		    ITreeSelection selection = viewer.getStructuredSelection();
		    Object element = selection.getFirstElement();
		    if (contentProvider.hasChildren(element)) {
		        manager1.add(expandAction);
		        manager1.add(collapseAction);
		    }
		});

        manager.setRemoveAllWhenShown(true);
        Menu contextMenu = manager.createContextMenu(viewer.getTree());
        viewer.getControl().setMenu(contextMenu);
    }

    /**
     * @return the double click listener
     */
    public IDoubleClickListener getDoubleClickListener() {
        return doubleClickListener;
    }

    /**
     * @return the tree viewer
     */
    public TreeViewer getTreeViewer() {
        return viewer;
    }
}