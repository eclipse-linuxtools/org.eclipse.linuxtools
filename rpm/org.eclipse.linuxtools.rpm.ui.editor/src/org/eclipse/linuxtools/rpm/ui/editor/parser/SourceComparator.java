/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.parser;

import java.util.Comparator;

public class SourceComparator implements Comparator {
	public int compare(Object arg0, Object arg1) {
		SpecfileSource source0 = (SpecfileSource) arg0;
		SpecfileSource source1 = (SpecfileSource) arg1;
		if (source0 == null)
			return -1;
		if (source1 == null)
			return 1;
		if (source0.getNumber() < source1.getNumber())
			return -1;
		else if (source0.getNumber() == source1.getNumber())
			return 0;
		else
			return 1;
	}
}
