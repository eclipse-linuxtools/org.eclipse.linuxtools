/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.core;

/**
 * TCP Connection settings
 */
public class TCPConnectionSettings extends BaseConnectionSettings {

	/**
	 * the host to connect to (a URI representation, including 'tcp' scheme and
	 * port number).
	 */
	private final String host;

	/**
	 * absolute path to folder containing the certificates (ca.pem, key.pem and
	 * cert.pem).
	 */
	private final String pathToCertificates;

	/**
	 * Constructor
	 * 
	 * @param host
	 *            host to connect to
	 * @param pathToCertificates
	 *            absolute path to folder containing the certificates
	 */
	public TCPConnectionSettings(final String host,
			final String pathToCertificates) {
		super();
		this.host = new HostBuilder(host).enableTLS(pathToCertificates);
		this.pathToCertificates = pathToCertificates;
	}

	@Override
	public BindingType getType() {
		return BindingType.TCP_CONNECTION;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	public boolean hasHost() {
		return this.host != null && !this.host.isEmpty();
	}

	/**
	 * @return the tlsVerify
	 */
	public boolean isTlsVerify() {
		return this.pathToCertificates != null;
	}

	/**
	 * @return the pathToCertificates
	 */
	public String getPathToCertificates() {
		return pathToCertificates;
	}

	/**
	 * A utility class to build the actual {@code host} field from the given
	 * input by setting the correct {@code http} or {@code https} scheme.
	 */
	private static class HostBuilder {

		private static String HTTP_SCHEME = "http://"; //$NON-NLS-1$

		private static String TCP_SCHEME = "tcp://"; //$NON-NLS-1$

		private static String HTTPS_SCHEME = "https://"; //$NON-NLS-1$

		private final String host;

		public HostBuilder(final String host) {
			if (host == null || host.isEmpty()) {
				this.host = "";
			} else if (!host.matches("\\w+://.*")) { //$NON-NLS-1$
				this.host = HTTP_SCHEME + host;
			} else {
				this.host = host.replace(TCP_SCHEME, HTTP_SCHEME);
			}
		}

		public String enableTLS(final String pathToCertificates) {
			if (pathToCertificates == null || pathToCertificates.isEmpty()) {
				return this.host;
			}
			// enforce 'https'
			return this.host.replace(HTTP_SCHEME, HTTPS_SCHEME);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((pathToCertificates == null) ? 0
				: pathToCertificates.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TCPConnectionSettings other = (TCPConnectionSettings) obj;
		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}
		if (pathToCertificates == null) {
			if (other.pathToCertificates != null) {
				return false;
			}
		} else if (!pathToCertificates.equals(other.pathToCertificates)) {
			return false;
		}
		return true;
	}


}
