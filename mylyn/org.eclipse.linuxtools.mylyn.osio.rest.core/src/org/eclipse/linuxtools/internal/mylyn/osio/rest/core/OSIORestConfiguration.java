/*******************************************************************************
 * Copyright (c) 2013, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.IdNamed;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.User;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeAttributes;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeData;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeField;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeFieldType;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;

public class OSIORestConfiguration implements Serializable {

	private static final OSIORestTaskSchema SCHEMA = OSIORestTaskSchema.getDefault();

	private static final long serialVersionUID = 4173223872076958202L;

	private final String repositoryId;

	private final String userName;
	
	private Map<String, Space> spaces;
	
	private Map<String, Space> externalSpaces = new TreeMap<>();
	
	private Map<String, User> users;
	
	private final static String NEW = "new"; //$NON-NLS-1$
	private final static String OPEN = "open"; //$NON-NLS-1$
	private final static String IN_PROGRESS = "in progress"; //$NON-NLS-1$
	private final static String RESOLVED = "resolved"; //$NON-NLS-1$
	private final static String CLOSED = "closed"; //$NON-NLS-1$
	
	private static final List<String> statusValues = Arrays.asList(new String[] {
			NEW, OPEN, IN_PROGRESS, RESOLVED, CLOSED});
	
	private static final List<String> newStatusTransitions = Arrays.asList(new String[] {
			OPEN, IN_PROGRESS, RESOLVED, CLOSED
	});
	private static final List<String> openStatusTransitions = Arrays.asList(new String[] {
			IN_PROGRESS, RESOLVED, CLOSED
	});
	private static final List<String> inProgressStatusTransitions = Arrays.asList(new String[] {
			OPEN, RESOLVED, CLOSED
	});
	private static final List<String> resolvedStatusTransitions = Arrays.asList(new String[] {
			OPEN, IN_PROGRESS, CLOSED
	});
	private static final List<String> closedStatusTransitions = Arrays.asList(new String[] {
			OPEN, IN_PROGRESS, RESOLVED
	});
	
	private Map<String, List<String>> statusTransitions = new HashMap<>();

	public OSIORestConfiguration(String repositoryId, String userName) {
		this.repositoryId = repositoryId;
		this.userName = userName;
		statusTransitions.put("", statusValues); //$NON-NLS-1$
		statusTransitions.put(NEW, newStatusTransitions);
		statusTransitions.put(OPEN, openStatusTransitions);
		statusTransitions.put(IN_PROGRESS, inProgressStatusTransitions);
		statusTransitions.put(RESOLVED, resolvedStatusTransitions);
		statusTransitions.put(CLOSED, closedStatusTransitions);
	}

	public String getRepositoryId() {
		return repositoryId;
	}


	public String getUserName() {
		return userName;
	}
	
	public void setSpaces(Map<String, Space> spaces) {
		Function<Space, String> getName = new Function<Space, String>() {
			public String apply(Space item) {
				return item.getName();
			}
		};
		Function<String, String> comparatorFunction = Functions.compose(getName, Functions.forMap(spaces));
		Ordering<String> comparator = Ordering.natural().onResultOf(comparatorFunction);
		this.spaces = ImmutableSortedMap.copyOf(spaces, comparator);
	}
	
	public Map<String, Space> getExternalSpaces() {
		return externalSpaces;
	}
	
	public Map<String, Space> getSpaces() {
		return spaces;
	}

	public Space getSpaceById(String spaceId) {
		for (Space space : getSpaces().values()) {
			if (space.getId().equals(spaceId)) {
				return space;
			}
		}
		for (Space space : getExternalSpaces().values()) {
			if (space.getId().equals(spaceId)) {
				return space;
			}
		}
		return null;
	}

	public Space getSpaceWithName(String name) {
		return getSpaces().get(name);
	}
	
	public void updateInitialTaskData(TaskData data) throws CoreException {
		setSpaceOptions(data, getSpaces());
		updateSpaceOptions(data);
		for (String key : data.getRoot().getAttributes().keySet()) {
			if (key.equals(OSIORestTaskSchema.getDefault().NEW_COMMENT.getKey())
					|| key.equals(TaskAttribute.OPERATION)
					|| key.equals(OSIORestTaskSchema.getDefault().DATE_MODIFICATION.getKey())) {
				continue;
			}
			TaskAttribute attribute = data.getRoot().getAttribute(key);
			if (key.equals(OSIORestTaskSchema.getDefault().STATUS.getKey())) {
				if (attribute.getOptions().isEmpty()) {
					for (String status : statusValues) {
						attribute.putOption(status,  status);
					}
				}
			}
			if (key.equals(OSIORestTaskSchema.getDefault().ASSIGNEES.getKey())) {
				if (attribute.getOptions().isEmpty()) {
					attribute.putOption(userName, userName);
				}
				continue;
			}
			if (!key.equals(SCHEMA.SPACE.getKey())) {
				String configName = mapTaskAttributeKey2ConfigurationFields(key);
				if (configName.equals("baseType")) {
					if (attribute.getOptions().size() == 1 && attribute.getValue().isEmpty()) {
						attribute.setValue((String) attribute.getOptions().values().toArray()[0]);
					}
				}
			}
		}
	}

	private String mapTaskAttributeKey2ConfigurationFields(String taskAttributeKey) {
		String resultString;
		if (taskAttributeKey.equals("task.common.summary")) {
			resultString = "system.title";
		} else if (taskAttributeKey.equals(TaskAttribute.STATUS)) {
			resultString = "system.state"; //$NON-NLS-1$
		} else if (taskAttributeKey.equals(TaskAttribute.USER_ASSIGNED)) {
			resultString = "assignees"; //$NON-NLS-1$
		} else if (taskAttributeKey.equals(TaskAttribute.DESCRIPTION)) {
			resultString = "system.description"; //$NON-NLS-1$
		} else if (taskAttributeKey.equals("comment")) { //$NON-NLS-1$
			resultString = "longdesc"; //$NON-NLS-1$
		} else {
			resultString = taskAttributeKey;
		}
		return resultString;
	}

	private void setAttributeOptionsForSpace(TaskAttribute taskAttribute, Space actualSpace) {
		taskAttribute.clearOptions();
		if (taskAttribute.getId().equals(SCHEMA.WORKITEM_TYPE.getKey())) {
			internalSetAttributeOptions(taskAttribute, actualSpace.getWorkItemTypes());
		} else if (taskAttribute.getId().equals(SCHEMA.AREA.getKey())) {
			internalSetAttributeOptions(taskAttribute, actualSpace.getAreas());
		} else if (taskAttribute.getId().equals(SCHEMA.ITERATION.getKey())) {
			internalSetAttributeOptions(taskAttribute, actualSpace.getIterations());
		} else if (taskAttribute.getId().equals(SCHEMA.ADD_ASSIGNEE.getKey())) {
			internalSetAttributeOptions(taskAttribute, actualSpace.getUsers());
		} else if (taskAttribute.getId().equals(SCHEMA.ADD_LABEL.getKey())) {
			internalSetAttributeOptions(taskAttribute, actualSpace.getLabels());
		} else if (taskAttribute.getId().equals(SCHEMA.STATUS.getKey())) {
			Map<String, String> stateMap = new LinkedHashMap<>();
			Map<String, WorkItemTypeData> workItemTypes = actualSpace.getWorkItemTypes();
			for (Entry<String, WorkItemTypeData> entry : workItemTypes.entrySet()) {
				WorkItemTypeData data = entry.getValue();
				WorkItemTypeAttributes attributes = data.getWorkItemTypeAttributes();
				Map<String, WorkItemTypeField> fields = attributes.getFields();
				if (fields != null) {
					WorkItemTypeField state = fields.get("system.state"); //$NON-NLS-1
					if (state != null) {
						WorkItemTypeFieldType stateType = state.getType();
						for (String value : stateType.getValues()) {
							stateMap.put(value,  value);
						}
					}
				}
			}
			internalSetAttributeOptions(taskAttribute, stateMap);
		}
	}

	private void internalSetAttributeOptions(TaskAttribute taskAttribute, @SuppressWarnings("rawtypes") Map spaceMap) {
		boolean found = false;
		String actualValue = taskAttribute.getValue();
		for (Object entry : spaceMap.keySet()) {
			String option = (String)entry;
			String name = option;
			Object optionValue = spaceMap.get(entry);
			if (optionValue instanceof IdNamed) {
				name = ((IdNamed)optionValue).getName();
			}
			taskAttribute.putOption(option, name);
			if (!found) {
				found = actualValue.equals(option);
			}

		}
		if (!found) {
			taskAttribute.setValue(""); //$NON-NLS-1$
		}

	}

	public boolean setSpaceOptions(@NonNull TaskData taskData, @NonNull Map<String, Space> spaces) {
		TaskAttribute attributeSpace = taskData.getRoot().getMappedAttribute(SCHEMA.SPACE.getKey());
		if (attributeSpace != null) {
			SortedSet<String> spaceSet = new TreeSet<String>();
			for (String key : spaces.keySet()) {
				spaceSet.add(key);
			}
			attributeSpace.clearOptions();
			for (String SpaceName : spaceSet) {
				attributeSpace.putOption(SpaceName, SpaceName);
			}
			return true;
		}
		return false;
	}

	public boolean updateSpaceOptions(@NonNull TaskData taskData) {
		if (taskData == null) {
			return false;
		}
		TaskAttribute attributeSpaceId = taskData.getRoot().getMappedAttribute(SCHEMA.SPACE_ID.getKey());
		TaskAttribute attributeSpace = taskData.getRoot().getMappedAttribute(SCHEMA.SPACE.getKey());
		if (attributeSpaceId != null && !attributeSpaceId.getValue().isEmpty()) {
			Space actualSpace = getSpaceById(attributeSpaceId.getValue());
			if (actualSpace == null) {
				return false;
			}
			TaskAttribute attributeWorkItemType = taskData.getRoot().getMappedAttribute(SCHEMA.WORKITEM_TYPE.getKey());
			if (attributeWorkItemType != null) {
				setAttributeOptionsForSpace(attributeWorkItemType, actualSpace);
			}
			TaskAttribute attributeArea = taskData.getRoot().getMappedAttribute(SCHEMA.AREA.getKey());
			if (attributeArea != null) {
				setAttributeOptionsForSpace(attributeArea, actualSpace);
			}
			TaskAttribute attributeIteration = taskData.getRoot().getMappedAttribute(SCHEMA.ITERATION.getKey());
			if (attributeIteration != null) {
				setAttributeOptionsForSpace(attributeIteration, actualSpace);
			}
			TaskAttribute attributeAddAssignee = taskData.getRoot().getMappedAttribute(SCHEMA.ADD_ASSIGNEE.getKey());
			if (attributeAddAssignee != null) {
				setAttributeOptionsForSpace(attributeAddAssignee, actualSpace);
			}
			TaskAttribute attributeAddLabel = taskData.getRoot().getMappedAttribute(SCHEMA.ADD_LABEL.getKey());
			if (attributeAddLabel != null) {
				setAttributeOptionsForSpace(attributeAddLabel, actualSpace);
			}
			TaskAttribute attributeState = taskData.getRoot().getMappedAttribute(SCHEMA.STATUS.getKey());
			if (attributeState != null) {
				setAttributeOptionsForSpace(attributeState, actualSpace);
			}	
		} else {
			SortedSet<String> workItemTypes = new TreeSet<>();
			SortedSet<String> areas = new TreeSet<>();
			SortedSet<String> iterations = new TreeSet<>();
			SortedSet<String> users = new TreeSet<>();
			Set<String> states = new LinkedHashSet<>();
			for (Space space : getSpaces().values()) {
				if (attributeSpace != null) {
					attributeSpace.putOption(space.getName(), space.getName());
				}
				if (space.getWorkItemTypes() != null) {
					// assume first workItemType is representative of all with regards to states
					if (!space.getWorkItemTypes().isEmpty()) {
						WorkItemTypeData data = space.getWorkItemTypes().values().iterator().next();
						WorkItemTypeAttributes attributes = data.getWorkItemTypeAttributes();
						Map<String, WorkItemTypeField> fields = attributes.getFields();
						WorkItemTypeField state = fields.get("system.state"); //$NON-NLS-1$
						WorkItemTypeFieldType stateType = state.getType();
						String[] values = stateType.getValues();
						if (values != null) {
							for (String value : values) {
								states.add(value);
							}
						}
					}
					for (Entry<String, WorkItemTypeData> entry : space.getWorkItemTypes().entrySet()) {
						workItemTypes.add(entry.getKey());
						WorkItemTypeData data = entry.getValue();
					}
				}
				if (space.getAreas() != null) {
					for (String entry : space.getAreas().keySet()) {
						areas.add(entry);
					}
				}
				if (space.getIterations() != null) {
					for (String entry : space.getIterations().keySet()) {
						iterations.add(entry);
					}
				}
				if (space.getUsers() != null) {
					for (String entry : space.getUsers().keySet()) {
						users.add(entry);
					}
				}
			}
			TaskAttribute attributeWorkItemType = taskData.getRoot().getMappedAttribute(SCHEMA.WORKITEM_TYPE.getKey());
			if (attributeWorkItemType != null) {
				setAllAttributeOptions(attributeWorkItemType, workItemTypes);
			}
			TaskAttribute attributeState = taskData.getRoot().getMappedAttribute(SCHEMA.STATUS.getKey());
			if (attributeState != null) {
				setAllAttributeOptions(attributeState, states);
			}
			TaskAttribute attributeAssignees = taskData.getRoot().getMappedAttribute(SCHEMA.ASSIGNEES.getKey());
			if (attributeAssignees != null && attributeAssignees.getOptions().size() == 0) {
				attributeAssignees.putOption(userName,  userName);
			}

		}
		return true;
	}

	private void setAllAttributeOptions(TaskAttribute updateAttribute, Set<String> values) {
		for (String value : values) {
			updateAttribute.putOption(value, value);
		}
	}

	public void addValidOperations(TaskData workItem) {
		TaskAttribute attributeStatus = workItem.getRoot().getMappedAttribute(TaskAttribute.STATUS);
		String attributeStatusValue = attributeStatus.getValue();
		TaskAttribute operationAttribute = workItem.getRoot().getAttribute(TaskAttribute.OPERATION);
		if (operationAttribute == null) {
			operationAttribute = workItem.getRoot().createAttribute(TaskAttribute.OPERATION);
		}
		TaskAttribute attribute = workItem.getRoot()
				.createAttribute(TaskAttribute.PREFIX_OPERATION + attributeStatusValue);
		TaskOperation.applyTo(attribute, attributeStatusValue, 
				OSIORestMessages.getFormattedString("KeepStateOperation", attributeStatusValue)); //$NON-NLS-1$
		// set as default
		TaskOperation.applyTo(operationAttribute, attributeStatusValue, 
				OSIORestMessages.getFormattedString("KeepStateOperation", attributeStatusValue)); //$NON-NLS-1$
		for (String statusValue : statusValues) {
			if (attributeStatusValue == null ||
					(attributeStatusValue != null && attributeStatusValue.equals(statusValue))) {
				if (attributeStatusValue == null) {
					attributeStatusValue = ""; //$NON-NLS-1$
				}
				for (String transition : statusTransitions.get(statusValue)) {
					attribute = workItem.getRoot()
							.createAttribute(TaskAttribute.PREFIX_OPERATION + transition);
					TaskOperation.applyTo(attribute, transition, 
							OSIORestMessages.getFormattedString("StateChangeOperation", transition)); //$NON-NLS-1$
				}
			}
		}
	}

}