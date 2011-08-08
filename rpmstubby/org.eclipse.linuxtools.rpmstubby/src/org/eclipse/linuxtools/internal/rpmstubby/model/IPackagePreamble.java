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

public interface IPackagePreamble {
	
	public String getRelease();

	public void setRelease(String release);

	public String getLicense();

	public void setLicense(String license);

	public String getURL();

	public void setURL(String url);

	public List<String> getSources();

	public void setSources(List<String> sources);

	public List<String> getPaches();

	public void setPaches(List<String> paches);

	public String getBuildroot();

	public void setBuildroot(String buildroot);
	
	public List<PackageItem> getBuildRequires();

	public void setBuildRequires(List<PackageItem> buildRequires);
	
}
