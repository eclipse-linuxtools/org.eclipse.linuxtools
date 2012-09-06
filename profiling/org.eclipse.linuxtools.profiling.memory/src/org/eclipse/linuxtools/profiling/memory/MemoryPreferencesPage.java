/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.profiling.memory;

import org.eclipse.linuxtools.internal.profiling.provider.AbstractProviderPreferencesPage;

public class MemoryPreferencesPage extends AbstractProviderPreferencesPage {

	@Override
	public String getProfilingType() {
		return MemoryProfileConstants.PROFILING_TYPE;
	}
}