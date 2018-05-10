/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.linuxtools.systemtap.structures.TreeDefinitionNode;

public class DefinitionMenuTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        return receiver instanceof TreeDefinitionNode
                ? ((TreeDefinitionNode) receiver).getDefinition() != null
                : false;
    }

}
