/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.history;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.XMLMemento;

public interface IValgrindPersistable {
	
	/**
	 * This method allows extensions to save the state of any tool-specific
	 * data. The launch's state can then be restored by the restoreState()
	 * method. 
	 * @param memento - an XML document containing saved state information
	 * @throws CoreException
	 */
	public void saveState(XMLMemento memento) throws CoreException;
	
	/**
	 * This method is to be called to reparse the output of recent launches where the
	 * output still remains in the local filesystem. This method should perform the
	 * same output parsing as if a Valgrind instance just ran and display the appropriate
	 * UI.
	 * @param memento - an XML document containing saved state information
	 * @throws CoreException - if parsing the files fails
	 */
	public void restoreState(XMLMemento memento) throws CoreException;
}
