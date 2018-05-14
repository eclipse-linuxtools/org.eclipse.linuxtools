/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class SpecialColumnLabelProvider extends ColumnLabelProvider {

	public String getCompareText(Object element) {
		return getText(element);
	}

}
