/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core;

import org.eclipse.debug.core.ILaunch;

public interface IValgrindMessage {

    IValgrindMessage getParent();

    IValgrindMessage[] getChildren();

    String getText();

    ILaunch getLaunch();

    void addChild(IValgrindMessage child);

}