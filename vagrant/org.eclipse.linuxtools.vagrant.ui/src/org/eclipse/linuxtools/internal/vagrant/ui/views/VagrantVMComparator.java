/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.vagrant.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public class VagrantVMComparator extends ViewerComparator {

	private final TableViewer tableViewer;
	private int sortColumnIndex;
	private int sortDirection;

	public VagrantVMComparator(final TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}

	public void setColumn(final TableColumn sortColumn) {
		final TableColumn[] tableColumns = tableViewer.getTable().getColumns();
		int newSortColumnIndex = 0;
		for (int i = 0; i < tableColumns.length; i++) {
			if (tableViewer.getTable().getColumns()[i] == sortColumn) {
				newSortColumnIndex = i;
				break;
			}
		}
		// set direction UP when selecting a new column
		if (this.sortColumnIndex != newSortColumnIndex) {
			sortDirection = SWT.UP;
		}
		// reverse the current sort order
		else {
			sortDirection = (sortDirection == SWT.UP) ? SWT.DOWN : SWT.UP;
		}
		this.sortColumnIndex = newSortColumnIndex;
		tableViewer.getTable().setSortColumn(sortColumn);
		tableViewer.getTable().setSortDirection(sortDirection);

	}

	public int getDirection() {
		return sortDirection;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e1 instanceof IVagrantVM) || !(e2 instanceof IVagrantVM)) {
			return 0;
		}
		final int tmp = compareByColumn((IVagrantVM) e1, (IVagrantVM) e2);
		return tmp * sortDirection;
	}

	private int compareByColumn(final IVagrantVM container1,
			final IVagrantVM container2) {
		final ColumnLabelProvider sortColumnLabelProvider = (ColumnLabelProvider)this.tableViewer.getLabelProvider(sortColumnIndex);
		final String container1ColumnValue = sortColumnLabelProvider.getText(container1);
		final String container2ColumnValue = sortColumnLabelProvider.getText(container2);
		if(this.sortDirection == SWT.UP) {
			return container1ColumnValue.compareToIgnoreCase(container2ColumnValue);
		} else {
			return -container1ColumnValue.compareToIgnoreCase(container2ColumnValue);
		}
	}
}
