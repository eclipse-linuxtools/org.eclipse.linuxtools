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
package org.eclipse.linuxtools.docker.core;

import java.util.List;

/**
 * Interface for Docker Connection Settings finder utilities. This bundle
 * provides a default implementation, which can be replaced if needed (hint: for
 * testing purposes)
 */
public interface IDockerConnectionSettingsFinder {

	public List<IDockerConnectionSettings> findConnectionSettings();
}
