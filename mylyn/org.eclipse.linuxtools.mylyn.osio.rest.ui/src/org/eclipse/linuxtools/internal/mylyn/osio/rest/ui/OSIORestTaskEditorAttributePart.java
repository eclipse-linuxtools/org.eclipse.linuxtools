/*******************************************************************************
 * Copyright (c) 2016, 2017 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Red Hat Inc - modified to use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttributePart;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class OSIORestTaskEditorAttributePart extends TaskEditorAttributePart {

	private static final int LABEL_WIDTH = 220;

	private static final int COLUMN_WIDTH = 280;

	private static final int COLUMN_GAP = 40;

	private static final int MULTI_COLUMN_WIDTH = COLUMN_WIDTH + 5 + COLUMN_GAP + LABEL_WIDTH + 5 + COLUMN_WIDTH;

	private static final int MULTI_ROW_HEIGHT = 55;

	private boolean hasIncoming;
	
	private List<AbstractAttributeEditor> attributeEditors;
	
	private Composite attributesComposite;

	@Override
	public void createControl(Composite parent, final FormToolkit toolkit) {
		initialize();
		super.createControl(parent, toolkit);
	}

	public boolean hasIncoming() {
		return hasIncoming;
	}

	private void initialize() {
		attributeEditors = new ArrayList<AbstractAttributeEditor>();
		hasIncoming = false;

		Collection<TaskAttribute> attributes = getAttributes();
		for (TaskAttribute attribute : attributes) {
			AbstractAttributeEditor attributeEditor = createAttributeEditor(attribute);
			if (attributeEditor != null) {
				attributeEditors.add(attributeEditor);
				if (getModel().hasIncomingChanges(attribute)) {
					hasIncoming = true;
				}
			}
		}

		Comparator<AbstractAttributeEditor> attributeSorter = createAttributeEditorSorter();
		if (attributeSorter != null) {
			Collections.sort(attributeEditors, attributeSorter);
		}
	}

	@Override
	protected Control createContent(FormToolkit toolkit, Composite parent) {
		attributesComposite = toolkit.createComposite(parent);
		attributesComposite.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Control focus = event.display.getFocusControl();
				if (focus instanceof Text && ((Text) focus).getEditable() == false) {
					getManagedForm().getForm().setFocus();
				}
			}
		});

		GridLayout attributesLayout = EditorUtil.createSectionClientLayout();
		attributesLayout.numColumns = 4;
		attributesLayout.horizontalSpacing = 9;
		attributesLayout.verticalSpacing = 6;
		attributesComposite.setLayout(attributesLayout);

		GridData attributesData = new GridData(GridData.FILL_BOTH);
		attributesData.horizontalSpan = 1;
		attributesData.grabExcessHorizontalSpace = true;
		attributesData.grabExcessVerticalSpace = false;
		attributesComposite.setLayoutData(attributesData);

		createAttributeControls(attributesComposite, toolkit, attributesLayout.numColumns);
		toolkit.paintBordersFor(attributesComposite);

		return attributesComposite;
	}

	private void createAttributeControls(Composite attributesComposite, FormToolkit toolkit, int columnCount) {
		int currentColumn = 1;
		int currentPriority = 0;
		for (AbstractAttributeEditor attributeEditor : attributeEditors) {
			int priority = (attributeEditor.getLayoutHint() != null)
					? attributeEditor.getLayoutHint().getPriority()
					: LayoutHint.DEFAULT_PRIORITY;
			if (priority != currentPriority) {
				currentPriority = priority;
				if (currentColumn > 1) {
					while (currentColumn <= columnCount) {
						getManagedForm().getToolkit().createLabel(attributesComposite, ""); //$NON-NLS-1$
						currentColumn++;
					}
					currentColumn = 1;
				}
			}

			if (attributeEditor.hasLabel()) {
				attributeEditor.createLabelControl(attributesComposite, toolkit);
				Label label = attributeEditor.getLabelControl();
				String text = label.getText();
				String shortenText = TaskDiffUtil.shortenText(label, text, LABEL_WIDTH);
				label.setText(shortenText);
				if (!text.equals(shortenText)) {
					label.setToolTipText(text);
				}
				GridData gd = GridDataFactory.fillDefaults()
						.align(SWT.RIGHT, SWT.CENTER)
						.hint(LABEL_WIDTH, SWT.DEFAULT)
						.create();
				if (currentColumn > 1) {
					gd.horizontalIndent = COLUMN_GAP;
					gd.widthHint = LABEL_WIDTH + COLUMN_GAP;
				}
				label.setLayoutData(gd);
				currentColumn++;
			}

			attributeEditor.createControl(attributesComposite, toolkit);
			LayoutHint layoutHint = attributeEditor.getLayoutHint();
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			RowSpan rowSpan = (layoutHint != null && layoutHint.rowSpan != null) ? layoutHint.rowSpan : RowSpan.SINGLE;
			ColumnSpan columnSpan = (layoutHint != null && layoutHint.columnSpan != null)
					? layoutHint.columnSpan
					: ColumnSpan.SINGLE;
			gd.horizontalIndent = 1;// prevent clipping of decorators on Windows
			if (rowSpan == RowSpan.SINGLE && columnSpan == ColumnSpan.SINGLE) {
				gd.widthHint = COLUMN_WIDTH;
				gd.horizontalSpan = 1;
			} else {
				if (rowSpan == RowSpan.MULTIPLE) {
					gd.heightHint = MULTI_ROW_HEIGHT;
				}
				if (columnSpan == ColumnSpan.SINGLE) {
					gd.widthHint = COLUMN_WIDTH;
					gd.horizontalSpan = 1;
				} else {
					gd.widthHint = MULTI_COLUMN_WIDTH;
					gd.horizontalSpan = columnCount - currentColumn + 1;
				}
			}
			attributeEditor.getControl().setLayoutData(gd);

			getTaskEditorPage().getAttributeEditorToolkit().adapt(attributeEditor);

			currentColumn += gd.horizontalSpan;
			currentColumn %= columnCount;
		}
	}

	/**
	 * Integrator requested the ability to control whether the attributes section is expanded on creation.
	 */
	@Override
	protected boolean shouldExpandOnCreate() {
		return getTaskData().isNew() || hasIncoming;
	}

}
