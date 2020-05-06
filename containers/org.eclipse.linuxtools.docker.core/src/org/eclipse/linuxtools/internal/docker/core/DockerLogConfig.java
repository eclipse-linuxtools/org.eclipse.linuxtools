/*******************************************************************************
 * Copyright (c) 2019, 2020 Red Hat.
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
package org.eclipse.linuxtools.internal.docker.core;

import java.util.Map;

import org.eclipse.linuxtools.docker.core.IDockerLogConfig;

import org.mandas.docker.client.messages.LogConfig;

public class DockerLogConfig implements IDockerLogConfig {

	private final String logType;
	private final Map<String, String> logOptions;

	public DockerLogConfig(final LogConfig config) {
		this.logType = config.logType();
		this.logOptions = config.logOptions();
	}

	private DockerLogConfig(final Builder builder) {
		this.logType = builder.logType;
		this.logOptions = builder.logOptions;
	}

	@Override
	public String logType() {
		return logType;
	}

	@Override
	public Map<String, String> logOptions() {
		return logOptions;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String logType;
		private Map<String, String> logOptions;

		public Builder logType(String logType) {
			this.logType = logType;
			return this;
		}

		public Builder logOptions(Map<String, String> logOptions) {
			this.logOptions = logOptions;
			return this;
		}

		public IDockerLogConfig build() {
			return new DockerLogConfig(this);
		}

	}
}
