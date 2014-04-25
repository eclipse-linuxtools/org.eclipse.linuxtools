/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.helgrind.tests;

import org.eclipse.linuxtools.internal.valgrind.helgrind.HelgrindPlugin;
import org.eclipse.linuxtools.internal.valgrind.tests.AbstractValgrindTest;

public abstract class AbstractHelgrindTest extends AbstractValgrindTest {

    @Override
    protected String getToolID() {
        return HelgrindPlugin.TOOL_ID;
    }
}
