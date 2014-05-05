/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.abstractviewers;

import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * This wrapper extends AbstractSTViewer {@link AbstractSTViewer} It is designed to be instantiated with a TreeViewer
 * JFace control
 *
 */
public abstract class AbstractSTTreeViewer extends AbstractSTViewer {

    public AbstractSTTreeViewer(Composite parent) {
        super(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
    }

    public AbstractSTTreeViewer(Composite parent, int style) {
        super(parent, style, true);
    }

    /**
     * It creates the wrapped TreeViewer
     *
     * @param parent
     *            - the parent Composite
     * @param style
     *            - the table style
     * @return a TreeViewer
     * @since 5.0
     */
    @Override
    protected TreeViewer createViewer(Composite parent, int style) {
        Tree t = createTree(parent, style);
        return new TreeViewer(t);
    }

    /**
     * Create the main tree control
     *
     * @param parent
     * @param style
     * @return Tree
     */
    private Tree createTree(Composite parent, int style) {
        Tree tree = new Tree(parent, style);
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);

        return tree;
    }

    /**
     * Create the columns in the tree.
     *
     */
    @Override
    protected void createColumns() {
        Tree tree = getViewer().getTree();
        TableLayout layout = new TableLayout();
        tree.setLayout(layout);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        for (int i = 0; i < getAllFields().length; i++) {
            ISTDataViewersField field = getAllFields()[i];
            TreeColumn tc = new TreeColumn(tree, field.getAlignment(), i);
            tc.setText(field.getColumnHeaderText());
            tc.setToolTipText(field.getColumnHeaderTooltip());
            tc.setImage(field.getColumnHeaderImage());
            tc.setWidth(field.getPreferredWidth());
            tc.setResizable(true);
            tc.setMoveable(true);

            tc.addSelectionListener(createHeaderListener());
            tc.setData(field);

            // defining the column label provider.
            // this has to be done after setting the column's data.
            TreeViewerColumn viewerColumn = new TreeViewerColumn(getViewer(), tc);
            viewerColumn.setLabelProvider(createColumnLabelProvider(tc));
        }

        tree.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                Tree tree = (Tree) e.widget;
                TreeItem item = tree.getItem(new Point(e.x, e.y));
                if (item == null) {
                    return;
                }
                tree.setCursor(e.display.getSystemCursor(SWT.CURSOR_ARROW));
            }

        });
    }

    /**
     * @see org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer#getColumns()
     * @since 5.0
     */
    @Override
    public TreeColumn[] getColumns() {
        return getViewer().getTree().getColumns();
    }

    @Override
    public void updateDirectionIndicator(Item column) {
        getViewer().getTree().setSortColumn((TreeColumn) column);
        if (getTableSorter().getTopPriorityDirection() == STDataViewersComparator.ASCENDING) {
            getViewer().getTree().setSortDirection(SWT.UP);
        } else {
            getViewer().getTree().setSortDirection(SWT.DOWN);
        }
    }

    @Override
    public int[] getColumnOrder() {
        return getViewer().getTree().getColumnOrder();
    }

    @Override
    protected void setColumnOrder(int[] order) {
        getViewer().getTree().setColumnOrder(order);
    }

    @Override
    public int getColumnIndex(Item column) {
        return getViewer().getTree().indexOf((TreeColumn) column);
    }

    @Override
    public int getColumnWidth(Item column) {
        return ((TreeColumn) column).getWidth();
    }

    @Override
    public void setColumnResizable(Item column, boolean resizable) {
        ((TreeColumn) column).setResizable(resizable);
    }

    @Override
    public void setColumnWidth(Item column, int width) {
        ((TreeColumn) column).setWidth(width);
    }

    @Override
    public TreeViewer getViewer() {
        return (TreeViewer) super.getViewer();
    }

}
