package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public abstract class BasePropertySection extends AbstractPropertySection {

	private TreeViewer treeViewer;
	private CopyValueAction copyAction;
	private Clipboard clipboard;
	private IPageSite pageSite;
	
	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage propertySheetPage) {
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(parent);
		final Composite container = new Composite(parent, SWT.NONE);
		container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridLayoutFactory.fillDefaults().numColumns(1).margins(5, 5).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(400, 180).applyTo(container);
		this.treeViewer = createTableTreeViewer(container);
		if (this.clipboard != null)
			this.clipboard.dispose();
		this.clipboard = new Clipboard(Display.getCurrent());
		this.pageSite = propertySheetPage.getSite();
		initContextMenu(pageSite, clipboard);
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

	/**
	 * Initializes the viewer context menu.
	 * 
	 * @param pageSite
	 *            page
	 * @param clipboard
	 *            clipboard
	 */
	private void initContextMenu(IPageSite pageSite, Clipboard clipboard) {
		TreeViewer treeViewer = getTreeViewer();
		copyAction = new CopyValueAction(getTreeViewer(), clipboard);

		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				handleMenuAboutToShow(manager);
			}
		});
		pageSite.registerContextMenu(
				"org.eclipse.linuxtools.docker.ui.BaseProperySection.menuid", //$NON-NLS-1$
				menuMgr, treeViewer);
		Menu menu = menuMgr.createContextMenu(treeViewer.getTree());
		treeViewer.getTree().setMenu(menu);

		menuMgr.add(copyAction);
		configureCopy();
	}

	/**
	 * Configures the view copy action which should be run on CTRL+C. We have to
	 * track widget focus to select the actual action because we have a few
	 * widgets that should provide copy action (at least tests hierarchy viewer
	 * and messages viewer).
	 */
	private void configureCopy() {
		getTreeViewer().getTree().addFocusListener(new FocusListener() {
			IAction viewCopyHandler;

			@Override
			public void focusLost(FocusEvent e) {
				if (viewCopyHandler != null) {
					switchTo(viewCopyHandler);
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				switchTo(copyAction);
			}

			private void switchTo(IAction copyAction) {
				IActionBars actionBars = pageSite.getActionBars();
				viewCopyHandler = actionBars
						.getGlobalActionHandler(ActionFactory.COPY.getId());
				actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
						copyAction);
				actionBars.updateActionBars();
			}
		});
	}

	/**
	 * Handles the context menu showing.
	 * 
	 * @param manager
	 *            context menu manager
	 */
	private void handleMenuAboutToShow(
			@SuppressWarnings("unused") IMenuManager manager) {
		ISelection selection = treeViewer.getSelection();
		copyAction.setEnabled(!selection.isEmpty());
	}

	@Override
	public void dispose() {
		super.dispose();
		if (this.clipboard != null) {
			this.clipboard.dispose();
		}
	}
}