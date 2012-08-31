/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core;

import org.eclipse.core.resources.IProject;

/**
 * Interface for oprofile core to utilize opcontrol program. Platform plugins should define/register
 * an OpcontrolProvider for the core to use.
 * @since 1.1
 */
public interface IOpcontrolProvider2 extends IOpcontrolProvider {
	//TODO: Merge this interface with IOpcontrolProvider for 2.0 version
	public boolean hasPermissions(IProject project) throws OpcontrolException;
}
