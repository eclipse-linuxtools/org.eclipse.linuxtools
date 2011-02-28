package org.eclipse.linuxtools.cdt.autotools.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IProject;

public class AutotoolsProjectNatureTest extends TestCase {
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        if (!ProjectTools.setup())
        	fail("could not perform basic project workspace setup");
     }
	
	public void testAutotoolsProjectNature() throws Exception {
		IProject testProject = ProjectTools.createProject("testProject");
		if(testProject == null) {
            fail("Unable to create test project");
        }
		assertTrue(testProject.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
		testProject.delete(true, false, ProjectTools.getMonitor());
	}
}
