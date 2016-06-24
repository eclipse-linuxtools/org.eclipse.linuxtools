/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;

/**
 * Model to describe a Label Variable in the 'Image Run...' wizard and
 * associated dialog.
 * 
 * @author jjohnstn
 *
 */
public class LabelVariableModel extends BaseDatabindingModel {

	public static final String NAME = "name"; //$NON-NLS-1$

	public static final String VALUE = "value"; //$NON-NLS-1$

	private String name;

	private String value;

	public LabelVariableModel() {

	}

	public LabelVariableModel(final String variableName,
			final String variableValue) {
		this.name = variableName;
		this.value = variableValue != null ? variableValue : "";
	}

	public LabelVariableModel(final LabelVariableModel variable) {
		this.name = variable.getName();
		this.value = variable.getValue();
	}

	public static LabelVariableModel createLabelVariableModel(
			String key, String value) {
		return new LabelVariableModel(key, value);
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		firePropertyChange(NAME, this.name, this.name = name);
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		firePropertyChange(VALUE, this.value, this.value = value);
	}

	@Override
	public String toString() {
		return name + "=" + value; //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LabelVariableModel other = (LabelVariableModel) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}