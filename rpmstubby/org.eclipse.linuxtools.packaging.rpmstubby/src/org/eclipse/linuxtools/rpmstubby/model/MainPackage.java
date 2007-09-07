/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpmstubby.model;

import java.util.Arrays;

public class MainPackage extends SubPackage implements IPackagePreamble {

	private String release;
	private String license;
	private String URL;
	private String[] sources;
	private String[] paches;
	private String buildroot;
	private PackageItem[] buildRequires;


	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String url) {
		URL = url;
	}

	public String[] getSources() {
		return sources;
	}

	public void setSources(String[] sources) {
		this.sources = sources;
	}

	public String[] getPaches() {
		return paches;
	}

	public void setPaches(String[] paches) {
		this.paches = paches;
	}

	public String getBuildroot() {
		return buildroot;
	}

	public void setBuildroot(String buildroot) {
		this.buildroot = buildroot;
	}

	public PackageItem[] getBuildRequires() {
		return buildRequires;
	}

	public void setBuildRequires(PackageItem[] buildRequires) {
		this.buildRequires = buildRequires;
	}


	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString());
		buffer.append("\nRelease: ").append(release);
		buffer.append("\nLicense; ").append(license);
		buffer.append("\nURL: ").append(URL);
		if (sources == null) {
			buffer.append("\nSources: ").append("null");
		} else {
			buffer.append("\nSources: ").append(
					Arrays.asList(sources).toString());
		}
		if (paches == null) {
			buffer.append("\nPaches: ").append("null");
		} else {
			buffer.append("\nPaches: ")
					.append(Arrays.asList(paches).toString());
		}
		if (buildRequires == null) {
			buffer.append("\nBuildRequires: ").append("null");
		} else {
			buffer.append("\nBuildRequires: ").append(
					Arrays.asList(buildRequires).toString());
		}
		buffer.append("\nBuildRoot: ").append(buildroot);
		return buffer.toString();
	}

}
