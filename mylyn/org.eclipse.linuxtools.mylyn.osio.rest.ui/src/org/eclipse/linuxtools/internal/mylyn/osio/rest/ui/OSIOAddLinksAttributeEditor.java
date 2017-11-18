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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestClient;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemResponse;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class OSIOAddLinksAttributeEditor extends AbstractAttributeEditor {

	private Combo typeCombo;
	
	private Combo workitemCombo;
	
	private final AbstractRepositoryConnector connector;
	
	private final TaskRepository repository;
	
	private Map<String, String> linkTypes = new HashMap<>();
	
	private Map<String, WorkItemResponse> workitems = new HashMap<>();
	
	private TaskAttribute attrAddLink;

	protected boolean suppressRefresh;

	public OSIOAddLinksAttributeEditor(TaskDataModel manager, AbstractRepositoryConnector connector, TaskAttribute taskAttribute) {
		super(manager, taskAttribute);
		this.connector = connector;
		this.repository = manager.getTaskRepository();
		setLayoutHint(new LayoutHint(RowSpan.MULTIPLE, ColumnSpan.MULTIPLE));
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, true));
		
		attrAddLink = getModel().getTaskData()
				.getRoot()
				.getMappedAttribute(OSIORestTaskSchema.getDefault().ADD_LINK.getKey());

		
		typeCombo = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		toolkit.adapt(typeCombo, true, true);
		typeCombo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		typeCombo.setFont(JFaceResources.getDefaultFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(typeCombo);
		typeCombo.setToolTipText(getDescription());
		typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index= typeCombo.getSelectionIndex();
				String linkType = typeCombo.getItem(index);
				TaskAttribute attr = getTaskAttribute();
				attr.setValue(linkType + " " + workitemCombo.getText()); //$NON-NLS-1$
				TaskAttributeMetaData metadata = attr.getMetaData();
				String linkid = linkTypes.containsKey(linkType) ? linkTypes.get(linkType) : ""; //$NON-NLS-1$
				metadata.putValue("linkid", linkid);
				if (index % 2 == 1) {
					metadata.putValue("direction", "forward"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					metadata.putValue("direction", "reverse"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				getModel().attributeChanged(attrAddLink);
			}
		});
		
		workitemCombo = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		toolkit.adapt(workitemCombo, true, true);
		workitemCombo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		workitemCombo.setFont(JFaceResources.getDefaultFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(workitemCombo);
		workitemCombo.setToolTipText(getDescription());
		workitemCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index= workitemCombo.getSelectionIndex();
				String workitem = workitemCombo.getItem(index);
				TaskAttribute attr = getTaskAttribute();
				attr.setValue(typeCombo.getText() + " " + workitem); //$NON-NLS-1$
				TaskAttributeMetaData metadata = attr.getMetaData();
				metadata.putValue("targetWidName", workitem);
				metadata.putValue("targetWid", workitems.get(workitem).getId()); //$NON-NLS-1$
				getModel().attributeChanged(attrAddLink);
			}
		});
		

		populateFromAttribute();

		setControl(comp);
	}

	private void populateFromAttribute() {
		OSIORestClient client;
		String spaceId = getTaskAttribute().getOption("space"); //$NON-NLS-1$
		try {
			client = ((OSIORestConnector)connector).getClient(repository);
			linkTypes = client.getSpaceLinkTypes(spaceId, repository);
			workitems = client.getSpaceWorkItems(spaceId);
		} catch (CoreException e) {
			// do nothing
		}
		typeCombo.setItems(linkTypes.keySet().toArray(new String[0]));
		typeCombo.add("", 0);
		String link = getTaskAttribute().getValue();
		int index = 0;
		if (link != null && !link.isEmpty()) {
			int i = 1;
			for (String linkType : linkTypes.keySet()) {
				if (link.startsWith(linkType)) {
					index = i;
				}
				++i;
			}
		}
		if (index >= 0) {
			typeCombo.select(index);
		}
		workitemCombo.setItems(workitems.keySet().toArray(new String[0]));
		index = -1;
		if (link != null && !link.isEmpty()) {
			int i = 0;
			for (String workitem : workitems.keySet()) {
				if (link.endsWith(workitem)) {
					index = i;
				}
				++i;
			}
		}
		if (index >= 0) {
			workitemCombo.select(index);
		}
	}
	
	@Override
	public void refresh() {
		if (typeCombo != null && !typeCombo.isDisposed()) {
			typeCombo.removeAll();
			populateFromAttribute();
		}
	}

	@Override
	public boolean shouldAutoRefresh() {
		return !suppressRefresh;
	}

}
