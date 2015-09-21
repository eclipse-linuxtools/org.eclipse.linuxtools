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

	/** flag to indicate if TLS is used. */
	private final boolean tlsVerify;

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
	 * @param tlsVerify
	 *            flag to indicate if TLS is used
	 * @param pathToCertificates
	 *            absolute path to folder containing the certificates
	 */
	public TCPConnectionSettings(final String host, final boolean tlsVerify,
			final String pathToCertificates) {
		super();
		this.host = host;
		this.tlsVerify = tlsVerify;
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

	/**
	 * @return the tlsVerify
	 */
	public boolean isTlsVerify() {
		return tlsVerify;
	}

	/**
	 * @return the pathToCertificates
	 */
	public String getPathToCertificates() {
		return pathToCertificates;
	}

}
