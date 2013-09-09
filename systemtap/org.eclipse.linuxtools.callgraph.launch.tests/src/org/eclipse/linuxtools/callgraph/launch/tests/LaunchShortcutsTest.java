/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.callgraph.launch.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.internal.callgraph.launch.LaunchStapGraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LaunchShortcutsTest extends AbstractStapTest {

	/**
	 * Checks that the scripts are correct/exist and that the expected command
	 * is sent.
	 */

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
	}

	@Override
	@After
	public void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}

	@Test
	public void testLaunchCallGraph() throws CModelException {
		SystemTapUIErrorMessages.setActive(false);

		LaunchStapGraph launch = new LaunchStapGraph();
		launch.setTestMode(true);

		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		launch.launch(bin, "profile");
		String script = launch.getScript();

		assert(script
				.contains("probe process(@1).function(\"calledOnce\").call{	callFunction(probefunc())	}	probe process(@1).function(\"calledOnce\").return{		returnFunction(probefunc())	}"));
		assert(script
				.contains("probe process(@1).function(\"calledTwice\").call{	callFunction(probefunc())	}	probe process(@1).function(\"calledTwice\").return{		returnFunction(probefunc())	}"));
		assert(script
				.contains("probe process(@1).function(\"main\").call{	callFunction(probefunc())	}	probe process(@1).function(\"main\").return{		returnFunction(probefunc())	}"));

	}

}
