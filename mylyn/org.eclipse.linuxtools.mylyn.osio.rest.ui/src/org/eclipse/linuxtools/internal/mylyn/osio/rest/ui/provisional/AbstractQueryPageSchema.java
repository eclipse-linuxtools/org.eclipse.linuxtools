/*******************************************************************************
 * Copyright (c) 2015 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskData;

@SuppressWarnings("nls")
public class AbstractQueryPageSchema {

	public static class Field {

		private EnumSet<Flag> flags;

		private final String key;

		private final String label;

		private final String type;

		private final String indexKey;

		private final String dependsOn;

		private final Integer layoutPriority;

		protected Field(String key, String label, String type) {
			this(key, label, type, null, (Flag[]) null);
		}

		protected Field(String key, String label, String type, Flag... flags) {
			this(key, label, type, null, flags);
		}

		/**
		 * @param key
		 *            the task attribute key, which may be a common task attribute key defined in defined in
		 *            {@link TaskAttribute}
		 * @param label
		 *            the user-visible label that is used by the user to identify this field
		 * @param type
		 *            the type of the field, should be one of the constants defined in TaskAttribute (
		 *            <code>TaskAttribute.TYPE_*</code>)
		 * @param indexKey
		 *            the index key, or null if this should not be indexed
		 * @param flags
		 *            the flags, or null
		 */
		public Field(String key, String label, String type, String indexKey, Flag... flags) {
			this(key, label, type, indexKey, null, null, flags);
		}

		/**
		 * @param key
		 *            the task attribute key, which may be a common task attribute key defined in defined in
		 *            {@link TaskAttribute}
		 * @param label
		 *            the user-visible label that is used by the user to identify this field
		 * @param type
		 *            the type of the field, should be one of the constants defined in TaskAttribute (
		 *            <code>TaskAttribute.TYPE_*</code>)
		 * @param indexKey
		 *            the index key, or null if this should not be indexed
		 * @param layoutPriority
		 *            the layout priority, or null if this should not be used
		 * @param flags
		 *            the flags, or null
		 */
		public Field(String key, String label, String type, String indexKey, Integer layoutPriority, String dependsOn,
				Flag... flags) {
			Assert.isNotNull(key);
			Assert.isNotNull(label);
			Assert.isNotNull(type);
			this.key = key;
			this.label = label;
			this.type = type;
			this.indexKey = indexKey;
			this.layoutPriority = layoutPriority;
			this.dependsOn = dependsOn;
			if (flags == null || flags.length == 0) {
				this.flags = EnumSet.noneOf(Flag.class);
			} else {
				this.flags = EnumSet.copyOf(Arrays.asList(flags));
			}
		}

		public TaskAttribute createAttribute(TaskAttribute parent) {
			TaskAttribute attribute = parent.createMappedAttribute(getKey());
			// meta data
			TaskAttributeMetaData metaData = attribute.getMetaData();
			metaData.setLabel(getLabel());
			metaData.setType(getType());
			metaData.setReadOnly(isReadOnly());
			metaData.setKind(getKind());
			metaData.setRequired(isRequired());
			if (getLayoutPriority() != null) {
				metaData.putValue("LayoutPriority", getLayoutPriority().toString());
			}
			if (getDependsOn() != null) {
				metaData.setDependsOn(getDependsOn());
			}
			// options
			Map<String, String> options = getDefaultOptions();
			if (options != null) {
				for (Entry<String, String> option : options.entrySet()) {
					attribute.putOption(option.getKey(), option.getValue());
				}
			}
			return attribute;
		}

		public Map<String, String> getDefaultOptions() {
			return Collections.emptyMap();
		}

		public String getKey() {
			return key;
		}

		/**
		 * the key to use when indexing this field
		 *
		 * @return the index key, or null if this should not be indexed
		 */
		public String getIndexKey() {
			return indexKey;
		}

		public String getKind() {
			if (flags.contains(Flag.ATTRIBUTE)) {
				return TaskAttribute.KIND_DEFAULT;
			} else if (flags.contains(Flag.PEOPLE)) {
				return TaskAttribute.KIND_PEOPLE;
			} else if (flags.contains(Flag.OPERATION)) {
				return TaskAttribute.KIND_OPERATION;
			} else if (flags.contains(Flag.DESCRIPTION)) {
				return TaskAttribute.KIND_DESCRIPTION;
			}
			return null;
		}

		public String getLabel() {
			return label;
		}

		public String getType() {
			return type;
		}

		public boolean isReadOnly() {
			return flags.contains(Flag.READ_ONLY);
		}

		@Override
		public String toString() {
			return getLabel();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((indexKey == null) ? 0 : indexKey.hashCode());
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Field other = (Field) obj;
			if (indexKey == null) {
				if (other.indexKey != null) {
					return false;
				}
			} else if (!indexKey.equals(other.indexKey)) {
				return false;
			}
			if (key == null) {
				if (other.key != null) {
					return false;
				}
			} else if (!key.equals(other.key)) {
				return false;
			}
			return true;
		}

		public boolean isRequired() {
			return flags.contains(Flag.REQUIRED);
		}

		Integer getLayoutPriority() {
			return layoutPriority;
		}

		public boolean isQueryRequired() {
			return flags.contains(Flag.QUERY_REQUIRED);
		}

		public String getDependsOn() {
			return dependsOn;
		}

	}

	public enum Flag {
		ATTRIBUTE, OPERATION, PEOPLE, READ_ONLY,
		/**
		 * A flag used to indicate that the field is related to a description.
		 *
		 * @see TaskAttribute#KIND_DESCRIPTION
		 */
		DESCRIPTION,
		/**
		 * A flag used to indicate that the field is required.
		 *
		 * @see TaskAttribute#META_REQUIRED
		 */
		REQUIRED,

		/**
		 * A flag used to indicate that the field is required in the Query Page.
		 *
		 * @see TaskAttribute#META_QUERY_REQUIRED
		 */
		QUERY_REQUIRED

	};

	protected class FieldFactory {

		private EnumSet<Flag> flags;

		private String key;

		private String label;

		private String type;

		private Integer layoutPriority;

		private String dependsOn;

		public FieldFactory(Field source) {
			this.flags = EnumSet.copyOf(source.flags);
			this.key = source.key;
			this.label = source.label;
			this.type = source.type;
			this.dependsOn = source.dependsOn;
		}

		public FieldFactory(org.eclipse.mylyn.tasks.core.data.AbstractTaskSchema.Field source) {
			this.flags = EnumSet.noneOf(Flag.class);
			this.key = source.getKey();
			this.label = source.getLabel();
			this.type = source.getType();
			this.dependsOn = source.getDependsOn();
		}

		public FieldFactory addFlags(Flag... flags) {
			this.flags.addAll(Arrays.asList(flags));
			return this;
		}

		public Field create() {
			return createField(key, label, type, null, layoutPriority, dependsOn,
					(!flags.isEmpty()) ? flags.toArray(new Flag[0]) : null);
		}

		public FieldFactory flags(Flag... flags) {
			this.flags = EnumSet.copyOf(Arrays.asList(flags));
			return this;
		}

		public FieldFactory key(String key) {
			this.key = key;
			return this;
		}

		public FieldFactory label(String label) {
			this.label = label;
			return this;
		}

		public FieldFactory removeFlags(Flag... flags) {
			this.flags.removeAll(Arrays.asList(flags));
			return this;
		}

		public FieldFactory type(String type) {
			this.type = type;
			return this;
		}

		public FieldFactory layoutPriority(Integer layoutPriority) {
			this.layoutPriority = layoutPriority;
			return this;
		}

		public FieldFactory dependsOn(String dependsOn) {
			this.dependsOn = dependsOn;
			return this;
		}

	}

	private final Map<String, Field> fieldByKey = new LinkedHashMap<String, Field>();

	/**
	 * Returns the specified field for the given key.
	 */
	public Field getFieldByKey(String taskKey) {
		return fieldByKey.get(taskKey);
	}

	/**
	 * Creates no-value attributes with default options for the supplied task for each schema field.
	 */
	public void initialize(TaskData taskData) {
		for (Field field : fieldByKey.values()) {
			field.createAttribute(taskData.getRoot());
		}
	}

	/**
	 * Provides an iterator for all fields within the schema. Subsequent modifications to the returned collection are
	 * not reflected to schema.
	 *
	 * @return all fields within the schema
	 */
	public Collection<Field> getFields() {
		return new ArrayList<Field>(fieldByKey.values());
	}

	protected Field createField(String key, String label, String type) {
		return createField(key, label, type, null, (Flag[]) null);
	}

	protected Field createField(String key, String label, String type, Flag... flags) {
		return createField(key, label, type, null, flags);
	}

	/**
	 * @see Field#Field(String, String, String, String, Flag...)
	 */
	protected Field createField(String key, String label, String type, String indexKey, Flag... flags) {
		return createField(key, label, type, indexKey, null, flags);
	}

	/**
	 * @see Field#Field(String, String, String, String, Integer, Flag...)
	 */
	protected Field createField(String key, String label, String type, String indexKey, Integer layoutPriority,
			Flag... flags) {
		Field field = new Field(key, label, type, indexKey, layoutPriority, null, flags);
		fieldByKey.put(key, field);
		return field;
	}

	/**
	 * @see Field#Field(String, String, String, String, Integer, Flag...)
	 */
	protected Field createField(String key, String label, String type, String indexKey, Integer layoutPriority,
			String dependsOn, Flag... flags) {
		Field field = new Field(key, label, type, indexKey, layoutPriority, dependsOn, flags);
		fieldByKey.put(key, field);
		return field;
	}

	protected FieldFactory inheritFrom(Field source) {
		return new FieldFactory(source);
	}

	protected FieldFactory copyFrom(org.eclipse.mylyn.tasks.core.data.AbstractTaskSchema.Field source) {
		return new FieldFactory(source);
	}

}
