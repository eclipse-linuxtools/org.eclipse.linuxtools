/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.launch.tests.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpUnitMask;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpUnitMask.MaskInfo;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.AbstractEventConfigTab;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileCounter;
import org.eclipse.swt.widgets.Button;

// mock event configuration tab
public class OprofileTestingEventConfigTab extends AbstractEventConfigTab {
	private IProject project;

	@Override
	protected boolean getOprofileTimerMode() {
		return false;
	}

	@Override
	protected int getNumberOfOprofileCounters() {
		return 1;
	}

	@Override
	protected boolean checkEventSetupValidity(int counter, String name,
			int maskValue) {
		return true;
	}

	@Override
	protected IProject getOprofileProject() {
		return project;
	}

	@Override
	public void setOprofileProject(IProject proj) {
		project = proj;
	}

	@Override
	protected void updateOprofileInfo() {
	}

	public Button getDefaultCheck() {
		return defaultEventCheck;
	}

	@Override
	protected OprofileCounter[] getOprofileCounters(ILaunchConfiguration config) {
		// setup and return mock counters
		OprofileCounter[] ctrs = new OprofileCounter[] { getOprofileCounter(1) };
		if (config != null) {
			ctrs[0].loadConfiguration(config);
		}
		return ctrs;
	}

	@Override
	public OprofileCounter getOprofileCounter(int i) {
		// mock mask info
		MaskInfo maskInfo = new MaskInfo();
		maskInfo.description = "mock mask info"; //$NON-NLS-1$
		maskInfo.value = 0;

		MaskInfo[] maskInfoDescriptions = { maskInfo };

		// mock mask
		OpUnitMask mask = new OpUnitMask();
		mask.setDefault(0);
		mask.setMaskDescriptions(maskInfoDescriptions);
		mask.setType(0);
		mask.setMaskFromIndex(0);
		mask.setMaskValue(0);

		// mock events
		OpEvent event = new OpEvent();
		event.setMinCount(1);
		event.setText("mock-event"); //$NON-NLS-1$
		event.setTextDescription("Mock Event"); //$NON-NLS-1$
		event.setUnitMask(mask);

		OpEvent[] events = { event };

		// mock counter
		OprofileCounter ctr = new OprofileCounter(i, events);
		ctr.setCount(1);
		ctr.setEvent(event);

		return ctr;
	}

}
