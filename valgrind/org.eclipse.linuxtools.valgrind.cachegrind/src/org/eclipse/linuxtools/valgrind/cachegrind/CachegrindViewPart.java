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

import java.text.DecimalFormat;
import java.util.Arrays;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.profiling.ui.ProfileUIUtils;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFunction;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindLine;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.cachegrind.model.ICachegrindElement;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class CachegrindViewPart extends ViewPart implements IValgrindToolView {

	protected CachegrindOutput[] outputs;
	protected TreeViewer viewer;

	protected static final int COLUMN_SIZE = 75;
	protected CellLabelProvider labelProvider;
	
	// Events - Cache
	protected static final String IR = "Ir"; //$NON-NLS-1$
	protected static final String I1MR = "I1mr"; //$NON-NLS-1$
	protected static final String I2MR = "I2mr"; //$NON-NLS-1$
	protected static final String DR = "Dr"; //$NON-NLS-1$
	protected static final String D1MR = "D1mr"; //$NON-NLS-1$
	protected static final String D2MR = "D2mr"; //$NON-NLS-1$
	protected static final String DW = "Dw"; //$NON-NLS-1$
	protected static final String D1MW = "D1mw"; //$NON-NLS-1$
	protected static final String D2MW = "D2mw"; //$NON-NLS-1$
	
	// Events - Branch
	protected static final String BC = "Bc"; //$NON-NLS-1$
	protected static final String BCM = "Bcm"; //$NON-NLS-1$
	protected static final String BI = "Bi"; //$NON-NLS-1$
	protected static final String BIM = "Bim"; //$NON-NLS-1$
	
	protected static final Image FUNC_IMG = CachegrindPlugin.imageDescriptorFromPlugin(CachegrindPlugin.PLUGIN_ID, "icons/function_obj.gif").createImage(); //$NON-NLS-1$

	@Override
	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.marginHeight = topLayout.marginWidth = 0;
		top.setLayout(topLayout);
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
		column.getColumn().addSelectionListener(getHeaderListener());
		column.setLabelProvider(labelProvider);

		viewer.setContentProvider(new CachegrindTreeContentProvider());
		viewer.setLabelProvider(labelProvider);
		viewer.setAutoExpandLevel(2);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object selection = ((StructuredSelection) event.getSelection()).getFirstElement();
				String path = null;
				int line = 0;
				if (selection instanceof CachegrindFile) {
					path = ((CachegrindFile) selection).getPath();
				}
				else if (selection instanceof CachegrindLine) {
					CachegrindLine element = (CachegrindLine) selection;
					CachegrindFile file = (CachegrindFile) element.getParent().getParent();
					path = file.getPath();
					line = element.getLine();
				}
				else if (selection instanceof CachegrindFunction) {
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
					} catch (PartInitException e) {
						e.printStackTrace();
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}			
		});
	}

	@Override
	public void setFocus() {
		viewer.getTree().setFocus();
	}

	public IAction[] getToolbarActions() {
		return null;
	}

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
					tree.setSortDirection(SWT.DOWN);
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
		}
		else if (event.equals(I1MR)) {
			result = Messages.getString("CachegrindViewPart.I1mr_long"); //$NON-NLS-1$
		}
		else if (event.equals(I2MR)) {
			result = Messages.getString("CachegrindViewPart.I2mr_long"); //$NON-NLS-1$
		}
		else if (event.equals(DR)) {
			result = Messages.getString("CachegrindViewPart.Dr_long"); //$NON-NLS-1$
		}
		else if (event.equals(D1MR)) {
			result = Messages.getString("CachegrindViewPart.D1mr_long"); //$NON-NLS-1$
		}
		else if (event.equals(D2MR)) {
			result = Messages.getString("CachegrindViewPart.D2mr_long"); //$NON-NLS-1$
		}
		else if (event.equals(DW)) {
			result = Messages.getString("CachegrindViewPart.Dw_long"); //$NON-NLS-1$
		}
		else if (event.equals(D1MW)) {
			result = Messages.getString("CachegrindViewPart.D1mw_long"); //$NON-NLS-1$
		}
		else if (event.equals(D2MW)) {
			result = Messages.getString("CachegrindViewPart.D2mw_long"); //$NON-NLS-1$
		}
		else if (event.equals(BC)) {
			result = Messages.getString("CachegrindViewPart.Bc_long"); //$NON-NLS-1$
		}
		else if (event.equals(BCM)) {
			result = Messages.getString("CachegrindViewPart.Bcm_long"); //$NON-NLS-1$
		}
		else if (event.equals(BI)) {
			result = Messages.getString("CachegrindViewPart.Bi_long"); //$NON-NLS-1$
		}
		else if (event.equals(BIM)) {
			result = Messages.getString("CachegrindViewPart.Bim_long"); //$NON-NLS-1$
		}
		return result;
	}

//	private String getShortEventName(String event) {
//		String result = event;
//		if (event.equals(IR)) {
//			result = Messages.getString("CachegrindViewPart.Ir_short"); //$NON-NLS-1$
//		}
//		else if (event.equals(I1MR)) {
//			result = Messages.getString("CachegrindViewPart.I1mr_short"); //$NON-NLS-1$
//		}
//		else if (event.equals(I2MR)) {
//			result = Messages.getString("CachegrindViewPart.I2mr_short"); //$NON-NLS-1$
//		}
//		else if (event.equals(DR)) {
//			result = Messages.getString("CachegrindViewPart.Dr_short"); //$NON-NLS-1$
//		}
//		else if (event.equals(D1MR)) {
//			result = Messages.getString("CachegrindViewPart.D1mr_short"); //$NON-NLS-1$
//		}
//		else if (event.equals(D2MR)) {
//			result = Messages.getString("CachegrindViewPart.D2mr_short"); //$NON-NLS-1$
//		}
//		else if (event.equals(DW)) {
//			result = Messages.getString("CachegrindViewPart.Dw_short"); //$NON-NLS-1$
//		}
//		else if (event.equals(D1MW)) {
//			result = Messages.getString("CachegrindViewPart.D1mw_short"); //$NON-NLS-1$
//		}
//		else if (event.equals(D2MW)) {
//			result = Messages.getString("CachegrindViewPart.D2mw_short"); //$NON-NLS-1$
//		}
//		return result;
//	}

	public void setOutputs(CachegrindOutput[] outputs) {
		this.outputs = outputs;
	}

	public CachegrindOutput[] getOutputs() {
		return outputs;
	}

	protected class CachegrindTreeContentProvider implements ITreeContentProvider {

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

		protected CElementLabelProvider cLabelProvider = new CElementLabelProvider(CElementLabelProvider.SHOW_SMALL_ICONS | CElementLabelProvider.SHOW_PARAMETERS | CElementLabelProvider.SHOW_RETURN_TYPE) {
			@Override
			public int getTextFlags() {
				int flags = super.getTextFlags();
				return flags |= CElementBaseLabels.M_FULLY_QUALIFIED;
			}
		};
		
		protected DecimalFormat df = new DecimalFormat("#,##0"); //$NON-NLS-1$

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
						String name = ((CachegrindFile) element).getName();
						cell.setText(name);
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
					cell.setImage(DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER_TOP));
				}
				else if (element instanceof CachegrindOutput) {
					cell.setText(NLS.bind(Messages.getString("CachegrindViewPart.Total_PID"), ((CachegrindOutput) element).getPid())); //$NON-NLS-1$
					cell.setImage(DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_REGISTER));
				}
			}
			else if (element instanceof CachegrindFunction) {
				cell.setText(df.format(((CachegrindFunction) element).getTotals()[index - 1]));
			}
			else if (element instanceof CachegrindLine) {
				cell.setText(df.format(((CachegrindLine) element).getValues()[index - 1]));
			}
			else if (element instanceof CachegrindOutput) {
				cell.setText(df.format(((CachegrindOutput) element).getSummary()[index - 1]));
			}
		}

	}

}
