/*
 * (c) 2007 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.core.tests;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.rpm.core.RPMProjectNature;

public class RPMProjectNatureTest extends TestCase {

	IWorkspace workspace;
    IWorkspaceRoot root;
    NullProgressMonitor monitor;
    String pluginRoot;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IWorkspaceDescription desc;
        workspace = ResourcesPlugin.getWorkspace();
        root = workspace.getRoot();
        monitor = new NullProgressMonitor();
        if(workspace == null) {
            fail("Workspace was not setup");
        }
        if(root == null) {
            fail("Workspace root was not setup");
        }
        desc = workspace.getDescription();
        desc.setAutoBuilding(false);
        workspace.setDescription(desc);
    }
	
	public void testAddRPMProjectNature() throws Exception {
		IProject testProject = root.getProject("testProject");
		testProject.create(monitor);
		testProject.open(monitor);
		if(testProject == null) {
            fail("Unable to create test project");
        }
		RPMProjectNature.addRPMNature(testProject, monitor);
		assertTrue(testProject.hasNature(RPMProjectNature.RPM_NATURE_ID));
		testProject.delete(true, false, monitor);
	}
	
}
