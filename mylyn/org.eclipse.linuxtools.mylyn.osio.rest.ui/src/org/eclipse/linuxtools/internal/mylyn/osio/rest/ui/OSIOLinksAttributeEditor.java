/*******************************************************************************
 * Copyright (c) 2015, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class OSIOLinksAttributeEditor extends AbstractAttributeEditor {

	private List list;
	
	private TaskAttribute attrRemoveLinks;

	protected boolean suppressRefresh;

	public OSIOLinksAttributeEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
		super(manager, taskAttribute);
		setLayoutHint(new LayoutHint(RowSpan.MULTIPLE, ColumnSpan.MULTIPLE));
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		list = new List(parent, SWT.FLAT | SWT.MULTI | SWT.V_SCROLL);
		toolkit.adapt(list, true, true);
		list.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		list.setFont(JFaceResources.getDefaultFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(list);
		list.setToolTipText(getDescription());

		populateFromAttribute();

		attrRemoveLinks = getModel().getTaskData()
				.getRoot()
				.getMappedAttribute(OSIORestTaskSchema.getDefault().REMOVE_LINKS.getKey());

		copyLinkMetaData();
		
		selectValuesToRemove();

		list.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					suppressRefresh = true;
					for (String cc : list.getItems()) {
						int index = list.indexOf(cc);
						if (list.isSelected(index)) {
							java.util.List<String> remove = attrRemoveLinks.getValues();
							if (!remove.contains(cc)) {
								attrRemoveLinks.addValue(cc);
							}
						} else {
							attrRemoveLinks.removeValue(cc);
						}
					}
					getModel().attributeChanged(attrRemoveLinks);
				} finally {
					suppressRefresh = false;
				}
			}
		});

		list.showSelection();

		setControl(list);
	}

	private void populateFromAttribute() {
		TaskAttribute attrLinks = getTaskAttribute();
		if (attrLinks != null) {
			for (String value : attrLinks.getValues()) {
				list.add(value);
			}
		}
	}
	
	// Copy link meta data from Links attribute to RemoveLinks attribute
	// (a map of link strings to their link uuids which is needed to delete them)
	private void copyLinkMetaData() {
		TaskAttribute attrLinks = getTaskAttribute();
		if (attrLinks != null) {
			TaskAttributeMetaData metadata = attrLinks.getMetaData();
			TaskAttributeMetaData removeMetaData = attrRemoveLinks.getMetaData();
			if (metadata != null) {
				for (String link : attrLinks.getValues()) {
					String metaValue = metadata.getValue(link);
					removeMetaData.putValue(link, metaValue);
				}
			}
		}
	}

	private void selectValuesToRemove() {
		for (String item : attrRemoveLinks.getValues()) {
			int i = list.indexOf(item);
			if (i != -1) {
				list.select(i);
			}
		}
	}

	@Override
	public void refresh() {
		if (list != null && !list.isDisposed()) {
			list.removeAll();
			populateFromAttribute();
			selectValuesToRemove();
		}
	}

	@Override
	public boolean shouldAutoRefresh() {
		return !suppressRefresh;
	}

}
