/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
