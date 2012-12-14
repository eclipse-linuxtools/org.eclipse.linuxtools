/*******************************************************************************
 * (C) Copyright 2012 IBM Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza (IBM) - Initial implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.internal.profiling.launch.provider.remote;


public class TimingPropertyTab extends AbstractProviderPropertyTab {

	public TimingPropertyTab() {
		super();
	}

	@Override
	protected String getType() {
		return "timing"; //$NON-NLS-1$
	}

	@Override
	protected String getPrefPageId() {
		return "org.eclipse.linuxtools.profiling.provider.TimingPreferencePage"; //$NON-NLS-1$
	}

}
