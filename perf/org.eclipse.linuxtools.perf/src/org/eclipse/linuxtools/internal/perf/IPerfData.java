/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

/**
 * Interface for generic perf data.
 */
public interface IPerfData {

	/**
	 * Get string representation of the data.
	 * @return String perf data
	 */
	public String getPerfData();

	/**
	 * Get title for this data.
	 * @return title for perf data
	 */
	public String getTitle();
}
