/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import org.eclipse.mylyn.tasks.core.data.AbstractTaskSchema;
import org.eclipse.mylyn.tasks.core.data.DefaultTaskSchema;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.google.common.collect.ImmutableMap;

public class OSIORestTaskSchema extends AbstractTaskSchema {

	private static final OSIORestTaskSchema instance = new OSIORestTaskSchema();
	
	public static final String PREFIX_LINK = "osiorest.link-"; //$NON-NLS-1$
	
	public static OSIORestTaskSchema getDefault() {
		return instance;
	}
	
	private static ImmutableMap<String, String> field2AttributeFieldMapper = new ImmutableMap.Builder<String, String>()
			.put("system.number", getDefault().NUMBER.getKey()) //$NON-NLS-1$
			.put("system.description", getDefault().DESCRIPTION.getKey()) //$NON-NLS-1$
			.put("system.state", getDefault().STATUS.getKey()) //$NON-NLS-1$
			.put("system.title", getDefault().SUMMARY.getKey()) //$NON-NLS-1$
			.put("version", getDefault().VERSION.getKey()) //$NON-NLS-1$
			.put("system.updated_at", getDefault().DATE_MODIFICATION.getKey()) //$NON-NLS-1$
			.put("system.created_at", getDefault().DATE_CREATION.getKey()) //$NON-NLS-1$
			.put("baseType", getDefault().WORKITEM_TYPE.getKey()) //$NON-NLS-1$
			.put("area", getDefault().AREA.getKey()) //$NON-NLS-1$
			.put("iteration", getDefault().ITERATION.getKey()) //$NON-NLS-1$
			.put("space", getDefault().SPACE.getKey()) //$NON-NLS-1$
			.build();

	private static ImmutableMap<String, String> attribute2FieldMapper = new ImmutableMap.Builder<String, String>()
			.put(getDefault().NUMBER.getKey(), "system.number") //$NON-NLS-1$
			.put(getDefault().DESCRIPTION.getKey(), "system.description") //$NON-NLS-1$
			.put(getDefault().STATUS.getKey(), "system.state") //$NON-NLS-1$
			.put(getDefault().SUMMARY.getKey(), "system.title") //$NON-NLS-1$
			.put(getDefault().VERSION.getKey(), "version") //$NON-NLS-1$
			.put(getDefault().DATE_MODIFICATION.getKey(), "system.updated_at") //$NON-NLS-1$
			.put(getDefault().DATE_CREATION.getKey(), "system.created_at") //$NON-NLS-1$
			.put(getDefault().WORKITEM_TYPE.getKey(), "baseType") //$NON-NLS-1$
			.put(getDefault().AREA.getKey(), "area") //$NON-NLS-1$
			.put(getDefault().ITERATION.getKey(), "iteration") //$NON-NLS-1$
			.put(getDefault().SPACE.getKey(), "space") //$NON-NLS-1$
			.build();

	public static String getAttributeNameFromFieldName(String fieldName) {
		String result = field2AttributeFieldMapper.get(fieldName);
		if (result == null) {
			result = fieldName;
		}
		return result;
	}

	public static String getFieldNameFromAttributeName(String attributeName) {
		String result = attribute2FieldMapper.get(attributeName);
		if (result == null) {
			result = attributeName;
		}
		return result;
	}



	private final DefaultTaskSchema parent = DefaultTaskSchema.getInstance();

	public final Field ID = createField("id", //$NON-NLS-1$ 
			OSIORestMessages.getString("SchemaID.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SHORT_TEXT, Flag.READ_ONLY);
	
	public final Field UUID = createField("uuid", //$NON-NLS-1$ 
			OSIORestMessages.getString("SchemaUUID.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SHORT_TEXT, Flag.READ_ONLY);
	
	public final Field NUMBER = createField("system.number", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaNumber.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_INTEGER, Flag.READ_ONLY);
	
	public final Field SPACE = createField("space", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaSpace.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SHORT_TEXT, Flag.READ_ONLY);
	
	public final Field DESCRIPTION = inheritFrom(parent.DESCRIPTION).create();
	
	public final Field SUMMARY = inheritFrom(parent.SUMMARY).addFlags(Flag.READ_ONLY).create();
	
	public final Field DATE_CREATION = inheritFrom(parent.DATE_CREATION).addFlags(Flag.READ_ONLY).create();

	public final Field DATE_MODIFICATION = inheritFrom(parent.DATE_MODIFICATION).addFlags(Flag.READ_ONLY).create();

	public final Field WORKITEM_TYPE = createField("baseType", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaWorkitemType.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SINGLE_SELECT, null, SPACE.getKey(), Flag.REQUIRED, Flag.READ_ONLY);
	
	public final Field CREATOR = createField("creator", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaCreator.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_PERSON, Flag.READ_ONLY);
	
	public final Field CREATOR_ID = createField("creatorID", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaCreatorID.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SHORT_TEXT, Flag.READ_ONLY);
	
	public final Field ASSIGNEES = createField("assignees", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaAssignees.label"), //$NON-NLS-1$
			IOSIORestConstants.EDITOR_TYPE_ASSIGNEES, Flag.PEOPLE);
	
	public final Field ADD_ASSIGNEE = createField("addAssignee", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaAddAssignee.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_MULTI_LABEL, Flag.PEOPLE);
	
	public final Field REMOVE_ASSIGNEE = createField("removeAssignee", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaRemoveAssignee.label"), //$NON-NLS-1$
			IOSIORestConstants.EDITOR_TYPE_ASSIGNEES);

	public final Field LABELS = createField("labels", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaLabels.label"), //$NON-NLS-1$
			IOSIORestConstants.EDITOR_TYPE_LABELS, Flag.ATTRIBUTE);
	
	
	public final Field REMOVE_LABEL = createField("removeLabel", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaRemoveLabel.label"), //$NON-NLS-1$
			IOSIORestConstants.EDITOR_TYPE_LABELS);
	
	public final Field LABELS_LINK = createField("labelsLink", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaLabelsLink.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_URL);

	public final Field ASSIGNEE_IDS = createField("assigneeIDs", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaAssigneeIDs.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SHORT_TEXT, Flag.READ_ONLY);
	
	public final Field STATUS = createField("system.state", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaStatus.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SINGLE_SELECT, Flag.REQUIRED);
	
	public final Field VERSION = createField("version", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaVersion.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_INTEGER, Flag.ATTRIBUTE, Flag.READ_ONLY);
	
	public final Field AREA = createField("area", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaArea.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SINGLE_SELECT, null, SPACE.getKey(), Flag.ATTRIBUTE);
	
	public final Field ITERATION = createField("iteration", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaIteration.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SINGLE_SELECT, null, SPACE.getKey(), Flag.ATTRIBUTE);
	
	public final Field LINKS = createField("links", //$NON-NLS-1$)
			OSIORestMessages.getString("SchemaLinks.label"),
			TaskAttribute.TYPE_SHORT_TEXT, Flag.ATTRIBUTE);
	
	public final Field ADD_LABEL = createField("addLabel", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaLabelAdd.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_MULTI_LABEL, Flag.ATTRIBUTE);
	
	public final Field COMMENT_COUNT = createField("commentCount", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaCommentCount.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_INTEGER, Flag.READ_ONLY);

	public final Field TASK_URL = inheritFrom(parent.TASK_URL).addFlags(Flag.ATTRIBUTE).create();
	
	public final Field NEW_COMMENT = inheritFrom(parent.NEW_COMMENT).create();

}
