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
package org.eclipse.linuxtools.valgrind.ui;

import org.eclipse.ui.IViewPart;

/**
 * Provides an interface for including controls in the Valgrind view.
 */
public interface IValgrindToolView extends IViewPart {
	
	/**
	 * Refreshes the controls within this view
	 */
	public void refreshView();

}
