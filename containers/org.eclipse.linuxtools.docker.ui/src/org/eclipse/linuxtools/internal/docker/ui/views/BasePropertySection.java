package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.Collection;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public abstract class BasePropertySection extends AbstractPropertySection {

	private TreeViewer treeViewer;
	
	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage propertySheetPage) {
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(parent);
		final Composite container = new Composite(parent, SWT.NONE);
		container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridLayoutFactory.fillDefaults().numColumns(1).margins(5, 5).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(400, 180).applyTo(container);
		this.treeViewer = createTableTreeViewer(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(treeViewer.getControl());
	}

	private TreeViewer createTableTreeViewer(final Composite container) {
		final TreeViewer treeViewer = new TreeViewer(container,  SWT.V_SCROLL | SWT.H_SCROLL);
		final Tree tree = treeViewer.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		final TreeViewerColumn propertyColumn = new TreeViewerColumn(treeViewer, SWT.BORDER);
		propertyColumn.getColumn().setWidth(150);
		propertyColumn.getColumn().setText("Property");
		propertyColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if(element instanceof Object[]) {
					final Object property = ((Object[])element)[0];
					return property.toString();
				}
				return super.getText(element);
			}
		});
		final TreeViewerColumn valueColumn = new TreeViewerColumn(treeViewer, SWT.BORDER);
		valueColumn.getColumn().setWidth(500);
		valueColumn.getColumn().setText("Value");
		valueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if(element instanceof Object[]) {
					final Object value = ((Object[])element)[1];
					// do not show values of a collection. There will be nested elements in the treeview for them.
					if(value instanceof Collection) {
						return "";
					} else if(value instanceof String || value instanceof Boolean || value instanceof Integer) {
						return value.toString();
					}
					return "";
				}
				return super.getText(element);
			}
		});
		return treeViewer;
	}
	
	TreeViewer getTreeViewer() {
		return treeViewer;
	}

}