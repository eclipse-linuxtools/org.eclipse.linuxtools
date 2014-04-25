/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif;

import java.util.HashMap;
import java.util.Map;

public class MassifOutput {
    protected Map<Integer, MassifSnapshot[]> pidMap;

    public MassifOutput() {
        pidMap = new HashMap<>();
    }

    public void putSnapshots(Integer pid, MassifSnapshot[] snapshots) {
        pidMap.put(pid, snapshots);
    }

    public MassifSnapshot[] getSnapshots(Integer pid) {
        return pidMap.get(pid);
    }

    public Integer[] getPids() {
        return pidMap.keySet().toArray(new Integer[pidMap.size()]);
    }
}
