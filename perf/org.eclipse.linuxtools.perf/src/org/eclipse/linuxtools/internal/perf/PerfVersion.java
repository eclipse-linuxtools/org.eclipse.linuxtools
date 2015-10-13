/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov <akurtako@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

public final class PerfVersion {

	private static final String DELIMITER = "."; //$NON-NLS-1$
	private int major;
	private int minor;
	private int micro;
	private String qualifier;

	public PerfVersion(String version) {
		String escapedVersion = version.replace('-', '.');
		String[] splitVersion = escapedVersion.split("\\.", 4); //$NON-NLS-1$
		if (splitVersion[0] != null) {
			major = Integer.valueOf(splitVersion[0]);
		}
		if (splitVersion[1] != null) {
			minor = Integer.valueOf(splitVersion[1]);
		}
		if (splitVersion[2] != null) {
			micro = Integer.valueOf(splitVersion[2]);
		}
		if (splitVersion.length > 3 && splitVersion[3] != null) {
			qualifier = splitVersion[3];
		}
	}

	public PerfVersion(int major, int minor, int micro) {
		this.major = major;
		this.minor = minor;
		this.micro = micro;
	}

	public boolean isNewer(PerfVersion other) {
		if (major != other.major) {
			return major > other.major;
		}

		if (minor != other.minor) {
			return minor > other.minor;
		}

		return micro > other.micro;
	}

	@Override
	public String toString() {
		return major + DELIMITER + minor + DELIMITER + micro + DELIMITER + qualifier;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getMicro() {
		return micro;
	}

	public String getQualifier() {
		if (qualifier == null) {
			return ""; //$NON-NLS-1$
		}
		return qualifier;
	}

}
