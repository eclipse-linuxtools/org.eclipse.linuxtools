/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import org.eclipse.core.resources.IContainer;

public interface IProjectConfiguration {

	public abstract IContainer getBuildFolder();

	public abstract IContainer getRpmsFolder();

	public abstract IContainer getSourcesFolder();

	public abstract IContainer getSpecsFolder();

	public abstract IContainer getSrpmsFolder();

}