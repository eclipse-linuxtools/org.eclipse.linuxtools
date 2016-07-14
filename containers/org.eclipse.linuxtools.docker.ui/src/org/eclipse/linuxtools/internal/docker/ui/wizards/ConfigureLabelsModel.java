/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
