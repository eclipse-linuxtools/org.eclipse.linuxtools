/*******************************************************************************
 * Copyright (c) 2006, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.changelog.core.actions;

public class PatchRangeElement {

    public int fromLine;
    public int toLine;
    private boolean localChange;

    public PatchRangeElement(int from, int to, boolean localChange) {
        this.fromLine =from;
        this.toLine= to;
        this.localChange = localChange;
    }

    public boolean isLocalChange() {
        return localChange;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PatchRangeElement) {
            PatchRangeElement b = (PatchRangeElement)o;
            return b.fromLine == fromLine && b.toLine == toLine && b.localChange == localChange;
        }
        else
            return this == o;
    }

    @Override
    public int hashCode() {
        return fromLine + toLine + (localChange ? 47 : 83);
    }
}