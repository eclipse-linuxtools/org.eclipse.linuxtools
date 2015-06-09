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
package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public class DockerImagesComparator extends ViewerComparator {

	private final TableViewer tableViewer;
	private int sortColumnIndex;
	private int sortDirection;

	public DockerImagesComparator(final TableViewer tableViewer) {
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
		if (!(e1 instanceof IDockerImage) || !(e2 instanceof IDockerImage)) {
			return 0;
		}
		final int tmp = compareByColumn((IDockerImage) e1, (IDockerImage) e2);
		return tmp * sortDirection;
	}

	private int compareByColumn(final IDockerImage image1,
			final IDockerImage image2) {
		final ColumnLabelProvider sortColumnLabelProvider = (ColumnLabelProvider)this.tableViewer.getLabelProvider(sortColumnIndex);
		final String image1ColumnValue;
		final String image2ColumnValue;
		// Special columns will provide a special value to use in comparing
		if (sortColumnLabelProvider instanceof SpecialColumnLabelProvider) {
			image1ColumnValue = ((SpecialColumnLabelProvider) sortColumnLabelProvider)
					.getCompareText(image1);
			image2ColumnValue = ((SpecialColumnLabelProvider) sortColumnLabelProvider)
					.getCompareText(image2);
		} else {
			image1ColumnValue = sortColumnLabelProvider.getText(image1);
			image2ColumnValue = sortColumnLabelProvider.getText(image2);
		}
		if(this.sortDirection == SWT.UP) {
			return image1ColumnValue.compareToIgnoreCase(image2ColumnValue);
		} else {
			return image1ColumnValue.compareToIgnoreCase(image2ColumnValue)
					* -1;
		}
	}
}
