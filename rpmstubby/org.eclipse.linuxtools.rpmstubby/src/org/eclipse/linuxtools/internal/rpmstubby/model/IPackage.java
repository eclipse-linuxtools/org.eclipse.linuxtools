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

public interface IPackage {

	public String getName();

	public void setName(String name);

	public String getGroup();

	public void setGroup(String group);

	public String getSummary();

	public void setSummary(String summary);

	public String getDescription();

	public void setDescription(String description);

	public List<PackageItem> getRequires();

	public void setRequires(List<PackageItem> requires);

	public List<PackageItem> getProvides();

	public void setProvides(List<PackageItem> provides);
	
	public String getVersion();

	public void setVersion(String version);

}
