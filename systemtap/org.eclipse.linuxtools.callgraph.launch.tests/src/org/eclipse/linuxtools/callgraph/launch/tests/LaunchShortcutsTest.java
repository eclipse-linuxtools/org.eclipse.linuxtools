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

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.internal.callgraph.launch.LaunchStapGraph;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;




public class LaunchShortcutsTest extends AbstractStapTest{

	/**
	 * Checks that the scripts are correct/exist and that the expected 
	 * command is sent.
	 */
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}
	
	
	public void testLaunchCallGraph() {
		try {
			SystemTapUIErrorMessages.setActive(false);
			
			LaunchStapGraph launch = new LaunchStapGraph();
			launch.setTestMode(true);
			
			IBinary bin = proj.getBinaryContainer().getBinaries()[0];
			launch.launch(bin, "profile");
			String script = launch.getScript();
			System.out.println(script);
			
			assert(script.contains("probe process(@1).function(\"calledOnce\").call{	callFunction(probefunc())	}	probe process(@1).function(\"calledOnce\").return{		returnFunction(probefunc())	}"));
			assert(script.contains("probe process(@1).function(\"calledTwice\").call{	callFunction(probefunc())	}	probe process(@1).function(\"calledTwice\").return{		returnFunction(probefunc())	}"));
			assert(script.contains("probe process(@1).function(\"main\").call{	callFunction(probefunc())	}	probe process(@1).function(\"main\").return{		returnFunction(probefunc())	}"));

			
			
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@Override
	protected Bundle getBundle() {
		return FrameworkUtil.getBundle(this.getClass());
	}

}
