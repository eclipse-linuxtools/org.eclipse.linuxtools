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

import java.util.List;

public class SubPackage implements IPackage {

	private String name;
	private String version;
	private String group;
	private String summary;
	private String description;
	private List<PackageItem> requires;
	private List<PackageItem> provides;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<PackageItem> getRequires() {
		return requires;
	}

	public void setRequires(List<PackageItem> requires) {
		this.requires = requires;
	}

	public List<PackageItem> getProvides() {
		return provides;
	}

	public void setProvides(List<PackageItem> provides) {
		this.provides = provides;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("%package ").append(name);
		buffer.append("\nVersion: ").append(version);
		buffer.append("\nSummary: ").append(summary);
		buffer.append("\nGroup: ").append(group);
		if (provides == null) {
			buffer.append("\nProvides: ").append("null");
		} else {
			buffer.append("\nProvides: ").append(
					provides.toString());
		}
		if (requires == null) {
			buffer.append("\nRequires: ").append("null");
		} else {
			buffer.append("\nRequires: ").append(
					requires.toString());
		}
		buffer.append("\n\n%description ").append(name + "\n");
		buffer.append(description);
		return buffer.toString();
	}

}
