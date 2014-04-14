/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Johnston <jjohnstn@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core;

import org.eclipse.jface.text.IDocument;

public interface IEditorChangeLogContrib2 {

	/**
	 * Perform documentation setup.  Use this to specify partitioning.
	 *
	 * @param document to set up.
	 */
	void setup(IDocument document);
}
