/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Alena Laskavaia - Bug 482947 - Valgrind Message API's: get rid of launch dependency
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindStackFrame;

public interface IValgrindMessage {
	IValgrindMessage getParent();

	IValgrindMessage[] getChildren();

	String getText();
	/**
	 * Returns launch object associated with this message, it may be null if trace was imported
	 * Use {@link ValgrindStackFrame#getSourceLocator()} to resolve locations instead
	 * @return launch object
	 */
	ILaunch getLaunch();

	void addChild(IValgrindMessage child);
}