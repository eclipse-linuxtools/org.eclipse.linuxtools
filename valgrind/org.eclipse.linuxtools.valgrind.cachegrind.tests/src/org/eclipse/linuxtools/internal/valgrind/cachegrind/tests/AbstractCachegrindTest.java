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
package org.eclipse.linuxtools.internal.valgrind.cachegrind.tests;

import org.eclipse.linuxtools.internal.valgrind.cachegrind.CachegrindPlugin;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFunction;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.internal.valgrind.tests.AbstractValgrindTest;

public abstract class AbstractCachegrindTest extends AbstractValgrindTest {

    @Override
    protected String getToolID() {
        return CachegrindPlugin.TOOL_ID;
    }

    protected CachegrindFile getFileByName(CachegrindOutput output, String name) {
        CachegrindFile file = null;
        for (CachegrindFile f : output.getFiles()) {
            if (f.getName().equals(name)) {
                file = f;
            }
        }
        return file;
    }

    protected CachegrindFunction getFunctionByName(CachegrindFile file, String name) {
        CachegrindFunction function = null;
        for (CachegrindFunction f : file.getFunctions()) {
            if (f.getName().equals(name)) {
                function = f;
            }
        }
        return function;
    }

}
