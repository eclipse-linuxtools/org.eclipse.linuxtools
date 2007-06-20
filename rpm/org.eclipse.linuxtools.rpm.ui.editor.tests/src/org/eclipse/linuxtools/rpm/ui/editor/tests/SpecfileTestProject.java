package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class SpecfileTestProject {
	private IProject project;
	
	public SpecfileTestProject() throws CoreException {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		project = workspaceRoot.getProject("TestSpecfileProject");
		project.create(null);
		project.open(null);
	}
	
	public void dispose() throws CoreException {
		project.delete(true, true, null);
		String[] cmd = { "rm", "-f", "/tmp/pkglist" };
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public IFile createFile(String filename) throws CoreException {
		IFile testSpecfile = project.getFile("testspecfile.spec");
		testSpecfile.create(null, false, null);
		return testSpecfile;
	}
	
	protected IMarker[] getFailureMarkers() throws CoreException {
		return project.findMarkers("org.eclipse.linuxtools.rpm.ui.editor.specfileerror", false, IResource.DEPTH_INFINITE);
	}
}
