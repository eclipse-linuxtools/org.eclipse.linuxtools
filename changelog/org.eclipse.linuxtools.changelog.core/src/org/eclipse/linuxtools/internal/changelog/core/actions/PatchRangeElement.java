/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.changelog.core.actions;

/**
 * 
 * @author klee
 *
 */
public class PatchRangeElement {
	
	public int ffromLine;
	public int ftoLine;
	public boolean flocalChange;
	
	public PatchRangeElement(int from, int to, boolean localChange) {
		ffromLine =from;
		ftoLine= to;
		flocalChange = localChange;
	}

	public boolean isLocalChange() {
		return flocalChange;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PatchRangeElement) {
			PatchRangeElement b = (PatchRangeElement)o;
			return b.ffromLine == ffromLine && b.ftoLine == ftoLine && b.flocalChange == flocalChange;
		}
		else
			return this == o;
	}
	
	@Override
	public int hashCode() {
		return ffromLine + ftoLine + (flocalChange ? 47 : 83);
	}
}