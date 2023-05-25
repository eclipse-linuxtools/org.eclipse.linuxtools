/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public class DockerContainersComparator extends ViewerComparator {

	private final TableViewer tableViewer;
	private int sortColumnIndex;
	private int sortDirection;

	public DockerContainersComparator(final TableViewer tableViewer) {
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
		if (!(e1 instanceof IDockerContainer d1)
				|| !(e2 instanceof IDockerContainer d2)) {
			return 0;
		}
		final int tmp = compareByColumn(d1, d2);
		return tmp * sortDirection;
	}

	private int compareByColumn(final IDockerContainer container1,
			final IDockerContainer container2) {
		final ColumnLabelProvider sortColumnLabelProvider = (ColumnLabelProvider)this.tableViewer.getLabelProvider(sortColumnIndex);
		final String container1ColumnValue = sortColumnLabelProvider.getText(container1);
		final String container2ColumnValue = sortColumnLabelProvider.getText(container2);
		if(this.sortDirection == SWT.UP) {
			return container1ColumnValue.compareToIgnoreCase(container2ColumnValue);
		} else {
			return container1ColumnValue.compareToIgnoreCase(container2ColumnValue) * -1;
		}
	}
}
