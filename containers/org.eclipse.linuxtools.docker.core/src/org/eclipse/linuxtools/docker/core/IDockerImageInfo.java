/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

import java.util.Date;

public interface IDockerImageInfo {

	public String id();

	public String parent();

	public String comment();

	public Date created();

	public String container();

	public IDockerContainerConfig containerConfig();

	public String dockerVersion();

	public String author();

	public IDockerContainerConfig config();

	public String architecture();

	public String os();

	public Long size();

}
