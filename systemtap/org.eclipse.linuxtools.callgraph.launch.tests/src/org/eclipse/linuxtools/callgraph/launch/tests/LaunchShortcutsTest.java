/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
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

    @Before
    public void prep() throws Exception {
        proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
    }

    @After
    public void clean() throws Exception {
        deleteProject(proj);
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
                .contains("probe process(@1).function(\"calledOnce\").call{    callFunction(probefunc())    }    probe process(@1).function(\"calledOnce\").return{        returnFunction(probefunc())    }"));
        assert(script
                .contains("probe process(@1).function(\"calledTwice\").call{    callFunction(probefunc())    }    probe process(@1).function(\"calledTwice\").return{        returnFunction(probefunc())    }"));
        assert(script
                .contains("probe process(@1).function(\"main\").call{    callFunction(probefunc())    }    probe process(@1).function(\"main\").return{        returnFunction(probefunc())    }"));

    }

}
