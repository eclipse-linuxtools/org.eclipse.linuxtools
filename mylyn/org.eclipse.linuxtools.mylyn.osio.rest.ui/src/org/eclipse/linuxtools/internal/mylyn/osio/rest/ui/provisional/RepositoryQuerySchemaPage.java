/*******************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional.AbstractQueryPageSchema.Field;
import org.eclipse.mylyn.commons.workbench.forms.SectionComposite;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

@SuppressWarnings("nls")
public class RepositoryQuerySchemaPage extends AbstractRepositoryQueryPage2 {

	private static final int LABEL_WIDTH = 110;

	private static final int COLUMN_WIDTH = 140;

	private static final int COLUMN_GAP = 20;

	private static final int MULTI_COLUMN_WIDTH = COLUMN_WIDTH + 5 + COLUMN_GAP + LABEL_WIDTH + 5 + COLUMN_WIDTH;

	private static final int MULTI_ROW_HEIGHT = 55;

	protected final AbstractQueryPageSchema schema;

	private final TaskData data;

	private final Pattern URL_PATTERN;

	private QueryPageSearch search;

	private final QueryPageDetails pageDetails;

	private AttributeEditorFactory factory;

	private SectionComposite scrolledComposite;

	private TaskData targetTaskData;

	protected final Map<String, AbstractAttributeEditor> editorMap = new HashMap<String, AbstractAttributeEditor>();

	public RepositoryQuerySchemaPage(String pageName, TaskRepository repository, IRepositoryQuery query,
			AbstractQueryPageSchema schema, TaskData data, QueryPageDetails pageDetails) {
		super(pageName, repository, query);
		this.schema = schema;
		this.data = data;
		this.pageDetails = pageDetails;
		setTitle(pageDetails.getPageTitle());
		setDescription(pageDetails.getPageDescription());
		URL_PATTERN = Pattern.compile(pageDetails.getUrlPattern());
		if (query != null) {
			search = new QueryPageSearch(query.getUrl());
		} else {
			search = new QueryPageSearch();
		}
	}

	@Override
	protected void createPageContent(@NonNull SectionComposite parent) {
		this.scrolledComposite = parent;

		Composite scrolledBodyComposite = scrolledComposite.getContent();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 10;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		scrolledBodyComposite.setLayout(layout);

		Composite attributesComposite = new Composite(scrolledBodyComposite, SWT.NONE);
		GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL)
				.grab(true, true)
				.span(2, 1)
				.applyTo(attributesComposite);
		layout = new GridLayout(6, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		attributesComposite.setLayout(layout);
		GridData g = new GridData(GridData.FILL, GridData.FILL, true, true);
		g.widthHint = 400;
		attributesComposite.setLayoutData(g);
		attributesComposite.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		TaskRepository repository = getTaskRepository();
		ITask nTask = new TaskTask(repository.getConnectorKind(), repository.getRepositoryUrl(), data.getTaskId());
		ITaskDataWorkingCopy workingCopy = TasksUi.getTaskDataManager().createWorkingCopy(nTask, data);

		final TaskDataModel model = new TaskDataModel(repository, nTask, workingCopy);
		factory = new AttributeEditorFactory(model, repository);
		model.addModelListener(new TaskDataModelListener() {

			@Override
			public void attributeChanged(TaskDataModelEvent event) {
				getContainer().updateButtons();
			}
		});
		targetTaskData = workingCopy.getLocalData();
		final TaskAttribute target = targetTaskData.getRoot();
		createFieldControls(attributesComposite, toolkit, layout.numColumns, target);
		Point p = scrolledBodyComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		scrolledComposite.setMinSize(p);

	}

	private void createFieldControls(Composite attributesComposite, FormToolkit toolkit, int columnCount,
			TaskAttribute target) {
		int currentColumn = 1;
		int currentPriority = 0;
		int currentLayoutPriority = 0;
		for (Field field : schema.getFields()) {
			TaskAttribute dataAttribute = target.getAttribute(field.getKey());
			AbstractAttributeEditor attributeEditor = factory.createEditor(field.getType(), dataAttribute);
			editorMap.put(dataAttribute.getId(), attributeEditor);

			String layoutPriorityString = dataAttribute.getMetaData().getValue("LayoutPriority");
			int layoutPriority = layoutPriorityString == null ? -1 : Integer.parseInt(layoutPriorityString);
			int priority = (attributeEditor.getLayoutHint() != null)
					? attributeEditor.getLayoutHint().getPriority()
					: LayoutHint.DEFAULT_PRIORITY;
			// TODO: copied from AbstractTaskEditorAttributeSection.createAttributeControls (only layoutPriority is new)
			if (priority != currentPriority || currentLayoutPriority != layoutPriority) {
				currentPriority = priority;
				currentLayoutPriority = layoutPriority;
				if (currentColumn > 1) {
					while (currentColumn <= columnCount) {
						Label l = toolkit.createLabel(attributesComposite, ""); //$NON-NLS-1$

						GridData gd = GridDataFactory.fillDefaults()
								.align(SWT.LEFT, SWT.CENTER)
								.hint(0, SWT.DEFAULT)
								.create();
						l.setLayoutData(gd);

						currentColumn++;
					}
					currentColumn = 1;
				}
			}

			if (attributeEditor.hasLabel()) {
				attributeEditor.createLabelControl(attributesComposite, toolkit);
				Label label = attributeEditor.getLabelControl();
				label.setBackground(attributesComposite.getBackground());
				label.setForeground(attributesComposite.getForeground());
				String text = label.getText();
				String shortenText = TaskDiffUtil.shortenText(label, text, LABEL_WIDTH);
				label.setText(shortenText);
				if (!text.equals(shortenText)) {
					label.setToolTipText(text);
				}
				GridData gd = GridDataFactory.fillDefaults()
						.align(SWT.RIGHT, SWT.CENTER)
						.grab(true, true)
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
			attributeEditor.getControl().setBackground(
					attributesComposite.getParent().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			attributeEditor.getControl().setForeground(attributesComposite.getForeground());
			LayoutHint layoutHint = attributeEditor.getLayoutHint();
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
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

			currentColumn += gd.horizontalSpan;
			currentColumn %= columnCount;
		}
	}

	@Override
	public boolean isPageComplete() {
		setMessage(pageDetails.getPageDescription());
		boolean result = super.isPageComplete();
		if (!result) {
			return result;
		}
		setErrorMessage(null);
		setMessage("");
		boolean oneFieldHasValue = false;
		for (Field field : schema.getFields()) {
			oneFieldHasValue |= (targetTaskData.getRoot().getAttribute(field.getKey()).hasValue()
					&& !targetTaskData.getRoot().getAttribute(field.getKey()).getValue().equals(""));
			if (field.isQueryRequired()) {
				String text = targetTaskData.getRoot().getAttribute(field.getKey()).getValue();
				if (text == null || text.length() == 0) {
					setMessage("Enter a value for " + field.getLabel());
					return false;
				}
			}
			if (field.getType().equals("url")) {
				String text = targetTaskData.getRoot().getAttribute(field.getKey()).getValue();
				if (text != null && text.length() > 0) {
					Matcher m = URL_PATTERN.matcher(text);
					if (m.find()) {
						setErrorMessage(null);
						return true;
					} else {
						setErrorMessage("Please specify a valid URL in " + field.getLabel());
						return false;
					}
				}
			}
		}
		if (!oneFieldHasValue) {
			setErrorMessage("Please fill at least on field!");
		}
		return true;
	}

	protected String getQueryUrl(String repsitoryUrl) {
		StringBuilder sb = new StringBuilder();
		sb.append(repsitoryUrl);
		sb.append("/");
		sb.append(pageDetails.getQueryUrlPart());
		sb.append(search.toQuery());
		return sb.toString();

	}

	@Override
	public void applyTo(IRepositoryQuery query) {
		query.setSummary(this.getQueryTitle());
		query.setUrl(getQueryUrl(getTaskRepository().getRepositoryUrl()));
		if (pageDetails.getQueryAttributeName() != null) {
			query.setAttribute(pageDetails.getQueryAttributeName(), Boolean.TRUE.toString());
		}
	}

	//FIXME: REST überarbeiten
	@Override
	protected void doRefreshControls() {
		// ignore

	}

	//FIXME: REST überarbeiten
	@Override
	protected boolean hasRepositoryConfiguration() {
		// ignore
		return true;
	}

	//FIXME: REST überarbeiten
	@Override
	protected boolean restoreState(@NonNull IRepositoryQuery query) {
		// ignore
		return false;
	}

	protected TaskData getTargetTaskData() {
		return targetTaskData;
	}

}
