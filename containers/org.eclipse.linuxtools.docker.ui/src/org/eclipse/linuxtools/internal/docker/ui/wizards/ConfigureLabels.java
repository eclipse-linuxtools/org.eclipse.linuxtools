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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;

/**
 * 
 */
public class ConfigureLabels extends Wizard {

	private final ConfigureLabelsPage configureLabelsPage;
	private ConfigureLabelsModel model;
	private Map<String, String> labelMap = new LinkedHashMap<>();

	/**
	 * Constructor when an {@link IDockerConnection} has been selected to run an
	 * {@link IDockerImage}.
	 * 
	 * @param connection
	 *            the {@link IDockerConnection} pointing to a specific Docker
	 *            daemon/host.
	 * @throws DockerException
	 */
	public ConfigureLabels() {
		super();
		setWindowTitle(WizardMessages.getString("ConfigureLabels.title")); //$NON-NLS-1$
		this.configureLabelsPage = new ConfigureLabelsPage();
		this.model = configureLabelsPage.getModel();
	}

	@Override
	public void addPages() {
		addPage(configureLabelsPage);
	}

	@Override
	public boolean canFinish() {
		return this.configureLabelsPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		labelMap = calculateConfigureLabels();
		return true;
	}

	public Map<String, String> getConfigureLabels() {
		return labelMap;
	}

	private Map<String, String> calculateConfigureLabels() {
		Map<String, String> labelMap = new LinkedHashMap<>();
		List<LabelVariableModel> vars = model.getLabelVariables();
		for (int i = 0; i < vars.size(); i++) {
			String key = vars.get(i).getName();
			String value = vars.get(i).getValue();
			labelMap.put(key, value);
		}
		return labelMap;
	}

}
