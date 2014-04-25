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
package org.eclipse.linuxtools.internal.valgrind.cachegrind;

import java.util.Arrays;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFunction;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindLine;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.ICachegrindElement;
import org.eclipse.linuxtools.profiling.ui.ProfileUIUtils;
import org.eclipse.linuxtools.valgrind.ui.CollapseAction;
import org.eclipse.linuxtools.valgrind.ui.ExpandAction;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class CachegrindViewPart extends ViewPart implements IValgrindToolView {

    private CachegrindOutput[] outputs;
    private TreeViewer viewer;

    private static final int COLUMN_SIZE = 75;
    private CachegrindLabelProvider labelProvider;
    private CachegrindTreeContentProvider contentProvider;
    private IDoubleClickListener doubleClickListener;
    private ExpandAction expandAction;
    private CollapseAction collapseAction;

    // Events - Cache
    private static final String IR = "Ir"; //$NON-NLS-1$
    private static final String I1MR = "I1mr"; //$NON-NLS-1$
    private static final String I2MR = "I2mr"; //$NON-NLS-1$
    private static final String DR = "Dr"; //$NON-NLS-1$
    private static final String D1MR = "D1mr"; //$NON-NLS-1$
    private static final String D2MR = "D2mr"; //$NON-NLS-1$
    private static final String DW = "Dw"; //$NON-NLS-1$
    private static final String D1MW = "D1mw"; //$NON-NLS-1$
    private static final String D2MW = "D2mw"; //$NON-NLS-1$

    // Events - Branch
    private static final String BC = "Bc"; //$NON-NLS-1$
    private static final String BCM = "Bcm"; //$NON-NLS-1$
    private static final String BI = "Bi"; //$NON-NLS-1$
    private static final String BIM = "Bim"; //$NON-NLS-1$

    @Override
    public void createPartControl(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout();
        topLayout.marginHeight = topLayout.marginWidth = 0;
        top.setLayout(topLayout);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));

        viewer = new TreeViewer(top, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.FULL_SELECTION);

        labelProvider = new CachegrindLabelProvider();
        ColumnViewerToolTipSupport.enableFor(viewer);

        Tree tree = viewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));

        TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
        column.getColumn().setText(Messages.getString("CachegrindViewPart.Location")); //$NON-NLS-1$
        column.getColumn().setWidth(COLUMN_SIZE * 4);
        column.getColumn().setResizable(true);
        column.getColumn().addSelectionListener(getHeaderListener());
        column.setLabelProvider(labelProvider);

        contentProvider = new CachegrindTreeContentProvider();
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(labelProvider);
        viewer.setAutoExpandLevel(2);
        doubleClickListener = new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                Object selection = ((StructuredSelection) event.getSelection()).getFirstElement();
                String path = null;
                int line = 0;
                if (selection instanceof CachegrindFile) {
                    path = ((CachegrindFile) selection).getPath();
                } else if (selection instanceof CachegrindLine) {
                    CachegrindLine element = (CachegrindLine) selection;
                    CachegrindFile file = (CachegrindFile) element.getParent().getParent();
                    path = file.getPath();
                    line = element.getLine();
                } else if (selection instanceof CachegrindFunction) {
                    CachegrindFunction function = (CachegrindFunction) selection;
                    path = ((CachegrindFile) function.getParent()).getPath();
                    if (function.getModel() instanceof ISourceReference) {
                        ISourceReference model = (ISourceReference) function.getModel();
                        try {
                            ISourceRange sr = model.getSourceRange();
                            if (sr != null) {
                                line = sr.getStartLine();
                            }
                        } catch (CModelException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (path != null) {
                    try {
                        ProfileUIUtils.openEditorAndSelect(path, line);
                    } catch (PartInitException|BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        viewer.addDoubleClickListener(doubleClickListener);

        expandAction = new ExpandAction(viewer);
        collapseAction = new CollapseAction(viewer);

        MenuManager manager = new MenuManager();
        manager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                ITreeSelection selection = (ITreeSelection) viewer.getSelection();
                ICachegrindElement element = (ICachegrindElement) selection.getFirstElement();
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

    @Override
    public void setFocus() {
        viewer.getTree().setFocus();
    }

    @Override
    public IAction[] getToolbarActions() {
        return null;
    }

    @Override
    public void refreshView() {
        if (outputs != null && outputs.length > 0) {
            String[] events = outputs[0].getEvents();
            for (int i = 0; i < events.length; i++) {
                TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
                column.getColumn().setText(events[i]);
                column.getColumn().setWidth(COLUMN_SIZE);
                column.getColumn().setToolTipText(getFullEventName(events[i]));
                column.getColumn().setResizable(true);
                column.getColumn().addSelectionListener(getHeaderListener());
                column.setLabelProvider(labelProvider);
            }
            viewer.setInput(outputs);
            viewer.getTree().layout(true);
        }
    }

    public void setOutputs(CachegrindOutput[] outputs) {
        this.outputs = outputs;
    }

    public CachegrindOutput[] getOutputs() {
        return outputs;
    }

    public TreeViewer getViewer() {
        return viewer;
    }

    public IDoubleClickListener getDoubleClickListener() {
        return doubleClickListener;
    }

    private SelectionListener getHeaderListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeColumn column = (TreeColumn) e.widget;
                Tree tree = viewer.getTree();
                if (column.equals(tree.getSortColumn())) {
                    int direction = tree.getSortDirection() == SWT.UP ? SWT.DOWN
                            : SWT.UP;
                    tree.setSortDirection(direction);
                } else {
                    tree.setSortDirection(SWT.UP);
                }
                tree.setSortColumn(column);
                viewer.setComparator(new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        Tree tree = ((TreeViewer) viewer).getTree();
                        int direction = tree.getSortDirection();
                        ICachegrindElement o1 = (ICachegrindElement) e1;
                        ICachegrindElement o2 = (ICachegrindElement) e2;
                        long result = 0;

                        int sortIndex = Arrays.asList(tree.getColumns()).indexOf(tree.getSortColumn());
                        if (sortIndex == 0) { // use compareTo
                            result = o1.compareTo(o2);
                        }
                        else {
                            long[] v1 = null;
                            long[] v2 = null;
                            if (o1 instanceof CachegrindFunction && o2 instanceof CachegrindFunction) {
                                v1 = ((CachegrindFunction) o1).getTotals();
                                v2 = ((CachegrindFunction) o2).getTotals();
                            }
                            else if (o1 instanceof CachegrindLine && o2 instanceof CachegrindLine) {
                                v1 = ((CachegrindLine) o1).getValues();
                                v2 = ((CachegrindLine) o2).getValues();
                            }
                            else if (o1 instanceof CachegrindOutput && o2 instanceof CachegrindOutput) {
                                v1 = ((CachegrindOutput) o1).getSummary();
                                v2 = ((CachegrindOutput) o2).getSummary();
                            }

                            if (v1 != null && v2 != null) {
                                result = v1[sortIndex - 1] - v2[sortIndex - 1];
                            }
                        }

                        // ascending or descending
                        result = direction == SWT.UP ? result : -result;

                        // overflow check
                        if (result > Integer.MAX_VALUE) {
                            result = Integer.MAX_VALUE;
                        } else if (result < Integer.MIN_VALUE) {
                            result = Integer.MIN_VALUE;
                        }

                        return (int) result;
                    }
                });
            }
        };
    }

    private String getFullEventName(String event) {
        String result = event;
        if (event.equals(IR)) {
            result = Messages.getString("CachegrindViewPart.Ir_long"); //$NON-NLS-1$
        } else if (event.equals(I1MR)) {
            result = Messages.getString("CachegrindViewPart.I1mr_long"); //$NON-NLS-1$
        } else if (event.equals(I2MR)) {
            result = Messages.getString("CachegrindViewPart.I2mr_long"); //$NON-NLS-1$
        } else if (event.equals(DR)) {
            result = Messages.getString("CachegrindViewPart.Dr_long"); //$NON-NLS-1$
        } else if (event.equals(D1MR)) {
            result = Messages.getString("CachegrindViewPart.D1mr_long"); //$NON-NLS-1$
        } else if (event.equals(D2MR)) {
            result = Messages.getString("CachegrindViewPart.D2mr_long"); //$NON-NLS-1$
        } else if (event.equals(DW)) {
            result = Messages.getString("CachegrindViewPart.Dw_long"); //$NON-NLS-1$
        } else if (event.equals(D1MW)) {
            result = Messages.getString("CachegrindViewPart.D1mw_long"); //$NON-NLS-1$
        } else if (event.equals(D2MW)) {
            result = Messages.getString("CachegrindViewPart.D2mw_long"); //$NON-NLS-1$
        } else if (event.equals(BC)) {
            result = Messages.getString("CachegrindViewPart.Bc_long"); //$NON-NLS-1$
        } else if (event.equals(BCM)) {
            result = Messages.getString("CachegrindViewPart.Bcm_long"); //$NON-NLS-1$
        } else if (event.equals(BI)) {
            result = Messages.getString("CachegrindViewPart.Bi_long"); //$NON-NLS-1$
        } else if (event.equals(BIM)) {
            result = Messages.getString("CachegrindViewPart.Bim_long"); //$NON-NLS-1$
        }
        return result;
    }

    protected static class CachegrindTreeContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getChildren(Object parentElement) {
            Object[] result = null;
            if (parentElement instanceof CachegrindOutput[]) {
                result = (CachegrindOutput[]) parentElement;
            }
            else if (parentElement instanceof ICachegrindElement) {
                result = ((ICachegrindElement) parentElement).getChildren();
            }
            return result;
        }

        @Override
        public Object getParent(Object element) {
            return ((ICachegrindElement) element).getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            ICachegrindElement[] children = (ICachegrindElement[]) getChildren(element);
            return children != null && children.length > 0;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }
}
