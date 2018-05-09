/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Alena Laskavaia - Bug 482947 - Valgrind Message API's: get rid of launch dependency
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindStackFrame;


/**
 * Represents a valgrind message
 */
public interface IValgrindMessage {
	/**
	 * If message if part of hierarchy returns the parent message
	 * @return parent message, can be null
	 */
	IValgrindMessage getParent();

	/**
	 * If message if part of hierarchy returns children messages
	 * @return non null array of children messages
	 */
	IValgrindMessage[] getChildren();

	/**
	 * Get message test
	 * @return message test
	 */
	String getText();
	/**
	 * Returns launch object associated with this message, it may be null if trace was imported
	 * Use {@link ValgrindStackFrame#getSourceLocator()} to resolve locations instead
	 * @return launch object
	 */
	ILaunch getLaunch();

	/**
	 * Add a child message to a message
	 * @param child - a child message, cannot be null
	 */
	void addChild(IValgrindMessage child);
}