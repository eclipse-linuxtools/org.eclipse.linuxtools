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
package org.eclipse.linuxtools.internal.rpmstubby.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean representation of the main package of the RPM.
 *
 */
public class MainPackage extends SubPackage implements IPackagePreamble {

	private String release;
	private String license;
	private String URL;
	private List<String> sources;
	private List<String> paches;
	private String buildroot;
	private List<PackageItem> buildRequires;
	private List<String> docFiles = new ArrayList<String>();
	private String docFilesRoot;


	/**
	 * Returns the doc files for this package.
	 * @return The doc files.
	 */
	public List<String> getDocFiles() {
		return docFiles;
	}

	/**
	 * Sets the doc files for this package.
	 * @param docFiles The doc files.
	 */
	public void setDocFiles(List<String> docFiles) {
		this.docFiles = docFiles;
	}

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

	public List<String> getSources() {
		return sources;
	}

	public void setSources(List<String> sources) {
		this.sources = sources;
	}

	public List<String> getPaches() {
		return paches;
	}

	public void setPaches(List<String> paches) {
		this.paches = paches;
	}

	public String getBuildroot() {
		return buildroot;
	}

	public void setBuildroot(String buildroot) {
		this.buildroot = buildroot;
	}

	public List<PackageItem> getBuildRequires() {
		return buildRequires;
	}

	public void setBuildRequires(List<PackageItem> buildRequires) {
		this.buildRequires = buildRequires;
	}


	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(super.toString());
		buffer.append("\nRelease: ").append(release);
		buffer.append("\nLicense; ").append(license);
		buffer.append("\nURL: ").append(URL);
		if (sources == null) {
			buffer.append("\nSources: ").append("null");
		} else {
			buffer.append("\nSources: ").append(
					sources.toString());
		}
		if (paches == null) {
			buffer.append("\nPaches: ").append("null");
		} else {
			buffer.append("\nPaches: ")
					.append(paches.toString());
		}
		if (buildRequires == null) {
			buffer.append("\nBuildRequires: ").append("null");
		} else {
			buffer.append("\nBuildRequires: ").append(
					buildRequires.toString());
		}
		buffer.append("\nBuildRoot: ").append(buildroot);
		return buffer.toString();
	}

	/**
	 * @param docFilesRoot The docFilesRoot to set
	 */
	public void setDocFilesRoot(String docFilesRoot) {
		this.docFilesRoot = docFilesRoot;
	}

	/**
	 * @return The docFilesRoot
	 */
	public String getDocFilesRoot() {
		return docFilesRoot;
	}

}
