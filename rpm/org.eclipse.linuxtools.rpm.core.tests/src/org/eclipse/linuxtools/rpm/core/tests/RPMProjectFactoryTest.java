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
import org.eclipse.linuxtools.rpm.core.IRPMProject;
import org.eclipse.linuxtools.rpm.core.RPMProjectFactory;

public class RPMProjectFactoryTest extends TestCase {
	
	IWorkspace workspace;
    IWorkspaceRoot root;
    NullProgressMonitor monitor;
    String pluginRoot;
    final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
    
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
	
	public void testGetNewProject() throws Exception {
		// Create a project for the test
        IProject testProject = root.getProject("testHelloWorld");
        testProject.create(monitor);
        testProject.open(monitor);
		
		IRPMProject rpmProject = RPMProjectFactory.getRPMProject(testProject);
		assertNotNull(rpmProject);
		testProject.delete(true, false, monitor);
	}
}
