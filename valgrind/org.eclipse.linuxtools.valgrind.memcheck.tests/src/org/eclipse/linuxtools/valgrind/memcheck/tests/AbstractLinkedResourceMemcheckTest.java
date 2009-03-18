package org.eclipse.linuxtools.valgrind.memcheck.tests;

import java.net.URL;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public abstract class AbstractLinkedResourceMemcheckTest extends AbstractMemcheckTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Turn off auto-building
		IWorkspaceDescription wsd = ResourcesPlugin.getWorkspace().getDescription();
		if (wsd.isAutoBuilding()) {
			wsd.setAutoBuilding(false);
			ResourcesPlugin.getWorkspace().setDescription(wsd);
		}
		
		proj = createProject(getBundle(), "linkedTest"); //$NON-NLS-1$
		
		// delete source folder and replace it with a link to its bundle location
		final Exception[] ex = new Exception[1];
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {					
					URL location = FileLocator.find(getBundle(), new Path("resources/linkedTest/src"), null); //$NON-NLS-1$
					IFolder srcFolder = proj.getProject().getFolder("src"); //$NON-NLS-1$
					srcFolder.delete(true, null);
					srcFolder.createLink(FileLocator.toFileURL(location).toURI(), IResource.REPLACE, null);
				} catch (Exception e) {
					ex[0] = e;
				}
			}

		}, null);

		if (ex[0] != null) {
			throw ex[0];
		}

		assertEquals(0, proj.getBinaryContainer().getBinaries().length);
		
		proj.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		proj.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(proj);
	}

}
