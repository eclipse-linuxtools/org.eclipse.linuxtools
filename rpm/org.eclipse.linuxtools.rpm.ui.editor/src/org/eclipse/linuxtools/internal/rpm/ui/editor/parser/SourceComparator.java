/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.parser;

import java.io.Serializable;
import java.util.Comparator;

public class SourceComparator implements Comparator<SpecfileSource>, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(SpecfileSource source0, SpecfileSource source1) {
		if (source0 == null) {
			return -1;
		}
		if (source1 == null) {
			return 1;
		}
		if (source0.getNumber() < source1.getNumber()) {
			return -1;
		} else if (source0.getNumber() == source1.getNumber()) {
			return 0;
		} else {
			return 1;
		}
	}
}
