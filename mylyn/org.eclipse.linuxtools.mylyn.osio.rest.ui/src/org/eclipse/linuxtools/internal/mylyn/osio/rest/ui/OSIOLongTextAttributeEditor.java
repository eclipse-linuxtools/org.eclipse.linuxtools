/*******************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Steffen Pingel
 */
public class OSIOLongTextAttributeEditor extends AbstractAttributeEditor {

	private Text text;

	private boolean suppressRefresh;

	public OSIOLongTextAttributeEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
		super(manager, taskAttribute);
		setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.MULTIPLE));
	}

	protected Text getText() {
		return text;
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		createControl(parent, toolkit, SWT.NONE);
	}

	protected void createControl(Composite parent, FormToolkit toolkit, int style) {
		if (isReadOnly()) {
			text = new Text(parent, SWT.FLAT | SWT.READ_ONLY);
			text.setFont(JFaceResources.getDefaultFont());
			text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
			text.setToolTipText(getDescription());
			text.setText(getValue());
		} else {
			text = toolkit.createText(parent, getValue(), SWT.FLAT | style);
			text.setFont(JFaceResources.getDefaultFont());
			text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
			text.setToolTipText(getDescription());
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					try {
						suppressRefresh = true;
						setValue(text.getText());
					} finally {
						suppressRefresh = false;
					}
					CommonFormUtil.ensureVisible(text);
				}
			});
		}
		toolkit.adapt(text, false, false);
		setControl(text);
	}

	public String getValue() {
		return getAttributeMapper().getValue(getTaskAttribute());
	}

	public void setValue(String text) {
		getAttributeMapper().setValue(getTaskAttribute(), text);
		attributeChanged();
	}

	@Override
	public void refresh() {
		if (text != null && !text.isDisposed()) {
			text.setText(getValue());
		}
	}

	@Override
	public boolean shouldAutoRefresh() {
		return !suppressRefresh;
	}

}
