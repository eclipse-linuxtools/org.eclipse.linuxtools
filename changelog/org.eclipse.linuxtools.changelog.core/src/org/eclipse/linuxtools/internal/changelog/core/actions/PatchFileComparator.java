/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Johnston <jjohnstn@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.actions;

import java.util.Comparator;

public class PatchFileComparator implements Comparator<PatchFile> {

    @Override
    public int compare(PatchFile p1, PatchFile p2) {
        return p1.getResource().getLocation().toOSString().compareToIgnoreCase(p2.getResource().getLocation().toOSString());
    }

}
