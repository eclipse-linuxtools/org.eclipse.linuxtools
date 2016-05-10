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
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IDockerAuthConfig;

public class DockerAuthConfig implements IDockerAuthConfig {

	private char[] username;
	private char[] password;
	private char[] email;
	private char[] serverAddress;

	public DockerAuthConfig(final Builder builder) {
		this.username = builder.username();
		this.password = builder.password();
		this.email = builder.email();
		this.serverAddress = builder.serverAddress();
	}

	@Override
	public char[] username() {
		return username;
	}

	@Override
	public char[] password() {
		return password;
	}

	@Override
	public char[] email() {
		return email;
	}

	@Override
	public char[] serverAddress() {
		return serverAddress;
	}

	public static class Builder {

		private char[] username;
		private char[] password;
		private char[] email;
		private char[] serverAddress;
		
		public Builder username(final char[] username) {
			this.username = username;
			return this;
		}

		public char[] username() {
			return username;
		}

		public Builder password(final char[] password) {
			this.password = password;
			return this;
		}

		public char[] password() {
			return password;
		}

		public Builder email(final char[] email) {
			this.email = email;
			return this;
		}

		public char[] email() {
			return email;
		}

		public Builder serverAddress(final char[] serverAddress) {
			this.serverAddress = serverAddress;
			return this;
		}

		public char[] serverAddress() {
			return serverAddress;
		}

		public DockerAuthConfig build() {
			return new DockerAuthConfig(this);
		}
	}

}
