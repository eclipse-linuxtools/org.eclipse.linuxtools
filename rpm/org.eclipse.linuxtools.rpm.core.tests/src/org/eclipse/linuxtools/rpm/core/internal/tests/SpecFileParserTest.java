/*
 * (c) 2007 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core.internal.tests;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.rpm.core.internal.SpecFileParser;
import org.eclipse.linuxtools.rpm.core.tests.RPMCoreTestsPlugin;

public class SpecFileParserTest extends TestCase {
    
    IWorkspace workspace;
    IWorkspaceRoot root;
    NullProgressMonitor monitor;
    String pluginRoot;
    final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
    
    public static TestSuite suite() {
        return new TestSuite(SpecFileParserTest.class);
    }
    
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

    public void testParseHelloWorld() throws Exception {
        // Create a project for the test
        IProject testProject = root.getProject("testHelloWorldSpec");
        testProject.create(monitor);
        testProject.open(monitor);
        if(testProject == null) {
            fail("Unable to create test project");
        }
        
        // Find the spec
        URL url = RPMCoreTestsPlugin.getDefault().find(new Path("resources" + file_sep + "specs" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
                "helloworld.spec"));
        if (url == null) {
            fail("Unable to find resource" + file_sep + "srpms" + file_sep +
                "helloworld.spec");
        }
        File foo = new File(Platform.asLocalURL(url).getPath());
        
        // Copy the spec into workspace
        IFile specFile = testProject.getFile("helloworld.spec");
        specFile.create(new FileInputStream(foo), false, null);
        assertTrue(specFile.exists());
        
        // Try parsing it
        SpecFileParser parser = new SpecFileParser(specFile);
        parser.parse();
        
        // Make sure we parsed the spec correctly
        String name = parser.getName();
        assertTrue(name.equals("helloworld"));
        String version = parser.getVersion();
        assertTrue(version.equals("2"));
        String release = parser.getRelease();
        assertTrue(release.equals("2"));
        assertTrue(parser.getConfigureArgs() == null);
        
        // Clean up
        testProject.delete(true, false, monitor);
    }

}
