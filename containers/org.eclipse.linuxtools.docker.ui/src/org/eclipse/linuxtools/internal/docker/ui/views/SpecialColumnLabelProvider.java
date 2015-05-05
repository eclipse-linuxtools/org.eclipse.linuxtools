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
package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class SpecialColumnLabelProvider extends ColumnLabelProvider {

	public String getCompareText(Object element) {
		return getText(element);
	}

}
