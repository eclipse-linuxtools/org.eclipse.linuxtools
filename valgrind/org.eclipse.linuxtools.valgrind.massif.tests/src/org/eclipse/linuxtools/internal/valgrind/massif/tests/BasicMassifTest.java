/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;

public class BasicMassifTest extends AbstractMassifTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}

	public void testNumSnapshots() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testNumSnapshots"); //$NON-NLS-1$

		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		MassifSnapshot[] snapshots = view.getSnapshots();
		assertEquals(14, snapshots.length);
		checkSnapshots(snapshots , 40, 16);
	}
}

