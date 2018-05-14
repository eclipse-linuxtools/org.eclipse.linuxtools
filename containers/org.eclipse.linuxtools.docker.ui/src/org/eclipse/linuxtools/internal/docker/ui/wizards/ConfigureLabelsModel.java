/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.Map;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;

public class ConfigureLabelsModel extends BaseDatabindingModel {

	public static final String LABEL_VARIABLES = "labelVariables"; //$NON-NLS-1$

	private WritableList<LabelVariableModel> labelVariables = new WritableList<>();

	public WritableList<LabelVariableModel> getLabelVariables() {
		return labelVariables;
	}

	public void setLabelVariables(final Map<String, String> labelVariables) {
		this.labelVariables.clear();
		if (labelVariables != null) {
			for (Map.Entry<String, String> entry : labelVariables.entrySet()) {
				this.labelVariables.add(new LabelVariableModel(entry.getKey(),
						entry.getValue()));
			}
		}
	}

	public void setLabelVariables(
			final WritableList<LabelVariableModel> labelVariables) {
		firePropertyChange(LABEL_VARIABLES, this.labelVariables,
				this.labelVariables = labelVariables);
	}

	public void addLabelVariable(final LabelVariableModel variable) {
		this.labelVariables.add(variable);
	}

	public void removeLabelVariables() {
		this.labelVariables.clear();
	}

	public void removeLabelVariable(final LabelVariableModel variable) {
		this.labelVariables.remove(variable);
	}

}
