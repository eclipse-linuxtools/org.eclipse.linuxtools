/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.views;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.swt.graphics.Image;

public class ImageViewLabelAndContentProvider extends BaseLabelProvider
		implements IStructuredContentProvider, ITableLabelProvider {

	private IDockerConnection connection;
	private List<IDockerImage> images;

	public enum Column {
		ID(0, 20), TAGS(1, 20), PARENT(2, 20), CREATED(3, 20), SIZE(4, 10), VIRTSIZE(
				5, 10);

		private int column;
		private int weight;
		private static final Map<Integer, Column> lookup = new HashMap<>();

		static {
			for (Column c : EnumSet.allOf(Column.class))
				lookup.put(c.getColumnNumber(), c);
		}

		private Column(int column, int weight) {
			this.column = column;
			this.weight = weight;
		}

		public int getColumnNumber() {
			return column;
		}

		public int getWeight() {
			return weight;
		}

		public static Column getColumn(int number) {
			return lookup.get(number);
		}

		public static int getSize() {
			return lookup.size();
		}

	}

	public void setTableViewer(TableViewer viewer) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return images.toArray(new IDockerImage[0]);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput != null) {
			if (newInput instanceof IDockerImage[]) {
				images = Arrays.asList((IDockerImage[])newInput);
			} else {
				connection = (IDockerConnection) newInput;
				images = connection.getImages();
			}
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public Object getColumnCompareObject(Object element, int columnIndex) {
		Column c = Column.getColumn(columnIndex);
		IDockerImage i = (IDockerImage) element;
		switch (c) {
		case CREATED:
			// We want latest created by default so we want lowest time value to
			// be higher by default.
			Long l = Long.valueOf(Long.MAX_VALUE) - Long.valueOf(i.created());
			return l;
		case SIZE:
			// For size, it is numeric, so just use the original numerical
			// ordering.
			return i.size();
		case VIRTSIZE:
			// For virtual size, it is numeric, so just use the original
			// numerical ordering.
			return i.virtualSize();
		case ID:
			return i.id();
		case PARENT:
			return i.parentId();
		case TAGS:
			// For tags, there are multiple values, just use the first for
			// ordering.
			return i.repoTags().get(0);
		}
		return "";
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		Column c = Column.getColumn(columnIndex);
		IDockerImage i = (IDockerImage) element;
		switch (c) {
		case CREATED:
			return i.createdDate();
		case ID:
			return i.id();
		case PARENT:
			return i.parentId();
		case SIZE:
			return displaySize(i.size());
		case VIRTSIZE:
			return displaySize(i.virtualSize());
		case TAGS:
			// For tags, there are multiple values, just use the first for
			// ordering.
			final StringBuilder tags = new StringBuilder();
			for(Iterator<String> iterator = i.tags().iterator(); iterator.hasNext();) {
				tags.append(iterator.next());
				if(iterator.hasNext()) {
					tags.append(", ");
				}
			}
			return tags.toString();
		}
		return "";
	}

	private String displaySize(Long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1000));
		return new DecimalFormat("#,##0.#").format(size
				/ Math.pow(1000, digitGroups))
				+ " " + units[digitGroups];
	}

}
