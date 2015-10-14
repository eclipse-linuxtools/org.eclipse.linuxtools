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
package org.eclipse.linuxtools.vagrant.core;

import java.io.File;

public interface IVagrantVM {

	public String id();

	public String name();

	public String provider();

	public String state();

	public String state_desc();

	public File directory();

	public String ip();

	public String user();

	public String identityFile();
}
