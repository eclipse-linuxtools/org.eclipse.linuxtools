/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.rpmstubby.model.MainPackage;
import org.eclipse.linuxtools.internal.rpmstubby.model.PackageItem;
import org.eclipse.linuxtools.internal.rpmstubby.model.SubPackage;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Generates the RPM specfile and the fetch script based on the feature and user
 * preferences.
 * 
 */
public class StubbyGenerator {

	private MainPackage mainPackage;
	private List<SubPackage> subPackages;

	/**
	 * Creates the specfile and fetch script generator for the given packages.
	 * 
	 * @param mainPackage
	 *            The main feature.
	 * @param subPackages
	 *            The included features or plugins.
	 */
	public StubbyGenerator(MainPackage mainPackage, List<SubPackage> subPackages) {
		this.mainPackage = mainPackage;
		this.subPackages = subPackages;
	}

	/**
	 * Generates a RPM specfile based on the parsed data from the feature.xml.
	 * 
	 * @return The generated specfile.
	 */
	public String generateSpecfile() {
		StringBuilder buffer = new StringBuilder();
		String simplePackageName = getPackageName(mainPackage.getName());
		String packageName = "eclipse-" + simplePackageName;
		buffer.append("%global eclipse_base   %{_libdir}/eclipse\n");
		buffer.append("%global install_loc    %{_datadir}/eclipse/dropins/"
				+ simplePackageName.toLowerCase() + "\n\n");
		buffer.append("Name:           " + packageName.toLowerCase() + "\n");
		buffer.append("Version:        "
				+ mainPackage.getVersion().replaceAll("\\.qualifier", "")
				+ "\n");
		buffer.append("Release:        1%{?dist}" + "\n");
		buffer.append("Summary:        " + mainPackage.getSummary() + "\n\n");
		buffer.append("Group:          Development/Tools\n");
		buffer.append("License:        " + mainPackage.getLicense() + "\n");
		buffer.append("URL:            " + mainPackage.getURL() + "\n");
		buffer.append("Source0:        #FIXME\n");
		buffer.append("BuildArch: noarch\n\n");
		buffer.append("BuildRequires: eclipse-pde >= 1:3.4.0\n");
		buffer.append("Requires: eclipse-platform >= 3.4.0\n");
		buffer.append("\n%description\n" + mainPackage.getDescription() + "\n");
		for (SubPackage subPackage : subPackages) {
			String subPackageName = getPackageName(subPackage.getName());
			buffer.append("\n%package  " + subPackageName + "\n");
			buffer.append("Summary:  " + subPackage.getSummary() + "\n");
			buffer.append("Requires: %{name} = %{version}-%{release}\n");
			buffer.append("Group: Development/Tools\n\n");
			buffer.append("%description " + subPackageName + "\n");
			buffer.append(subPackage.getDescription() + "\n");
		}
		generatePrepSection(buffer);

		generateBuildSection(buffer);
		buffer.append("%install\n");
		buffer.append("install -d -m 755 %{buildroot}%{install_loc}\n\n");
		buffer.append("%{__unzip} -q -d %{buildroot}%{install_loc} \\\n");
		buffer.append("     build/rpmBuild/" + mainPackage.getName()
				+ ".zip \n\n");
		generateFilesSections(buffer);
		buffer.append("%changelog\n\n");
		buffer.append("#FIXME\n");
		return buffer.toString();
	}

	private void generateFilesSections(StringBuilder buffer) {
		buffer.append("%files\n");
		buffer.append("%{install_loc}\n");
		for (String fileName : mainPackage.getDocFiles()) {
			buffer.append("%doc ").append(mainPackage.getDocFilesRoot())
					.append("/").append(fileName).append("\n");
		}
		buffer.append("\n");
		for (SubPackage subPackage : subPackages) {
			buffer.append("%files " + getPackageName(subPackage.getName())
					+ "\n");
			buffer.append("%dir %{eclipse_base}/features/"
					+ subPackage.getName() + "_*/\n");
			buffer.append("%doc %{eclipse_base}/features/"
					+ subPackage.getName() + "_*/*.html\n");
			buffer.append("%{eclipse_base}/features/" + subPackage.getName()
					+ "_*/feature.*\n");
			buffer.append(getPackageFiles(subPackage.getProvides()) + "\n");
		}
	}

	private void generatePrepSection(StringBuilder buffer) {
		buffer.append("\n%prep\n");
		buffer.append("#FIXME Replace FIXME with the root directory name in Source0\n");
		buffer.append("%setup -q -n FIXME\n\n");
	}

	private void generateBuildSection(StringBuilder buffer) {
		buffer.append("%build\n");
		buffer.append("%{eclipse_base}/buildscripts/pdebuild -f ").append(
				mainPackage.getName());
		buffer.append("\n\n");
	}

	/**
	 * Returns the last meaningful part of the feature id before the feature
	 * substring.
	 * 
	 * @param packageName
	 *            The feature id from which to extract the name.
	 * @return The part of the feature id to be used for package name.
	 */
	public String getPackageName(String packageName) {
		String[] packageItems = packageName.split("\\.");
		String name = packageItems[packageItems.length - 1];
		if (name.equalsIgnoreCase("feature")) {
			name = packageItems[packageItems.length - 2];
		}
		return name;
	}

	/**
	 * Writes the given contents to a file with the given fileName in the
	 * specified project.
	 * 
	 * @param projectName
	 *            The name of the project to put the file into.
	 * @param fileName
	 *            The name of the file.
	 * @param contents
	 *            The contents of the file.
	 * @throws CoreException
	 *             Thrown when the project doesn't exist.
	 */
	public void writeContent(String projectName, String fileName,
			String contents) throws CoreException {
		InputStream contentInputStream = new ByteArrayInputStream(
				contents.getBytes());
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(projectName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Project \"" + projectName
					+ "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = contentInputStream;
			if (file.exists()) {
				file.setContents(stream, true, true, null);
			} else {
				file.create(stream, true, null);
			}
			stream.close();
		} catch (IOException e) {
			StubbyLog.logError(e);
		}
		StubbyPlugin.getActiveWorkbenchShell().getDisplay()
				.asyncExec(new Runnable() {
					public void run() {
						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						try {
							IDE.openEditor(page, file, true);
						} catch (PartInitException e) {
							StubbyLog.logError(e);
						}
					}
				});
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, StubbyPlugin.PLUGIN_ID,
				IStatus.OK, message, null);
		throw new CoreException(status);
	}

	private String getPackageFiles(List<PackageItem> packageItems) {
		StringBuilder toRet = new StringBuilder();
		for (PackageItem packageItem : packageItems) {
			toRet.append("%{eclipse_base}/plugins/")
					.append(packageItem.getName()).append("_*.jar\n");
		}
		return toRet.toString();
	}
}
