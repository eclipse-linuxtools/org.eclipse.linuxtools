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
package org.eclipse.linuxtools.rpmstubby;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.rpmstubby.model.MainPackage;
import org.eclipse.linuxtools.rpmstubby.model.SubPackage;

/**
 * Utility class used for writing the generated specfile to a file.
 *
 */
public class SpecfileWriter {
	
	IPreferenceStore store;
	
	/**
	 * Creates the writer.
	 */
	public SpecfileWriter() {
		store = StubbyPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Parse the feature.xml and write the generated specfile.
	 * @param featureFile The feature.xml file.
	 */
	public void write(IFile featureFile) {
		// Populate Main package model
		StubbyPackageModel stubbyPackageModel = new StubbyPackageModel(
				featureFile);
		MainPackage mainPackage = new MainPackage();
		stubbyPackageModel.populatePackageData(mainPackage);
		stubbyPackageModel.populatePackagePreambleData(mainPackage);
		stubbyPackageModel.populateDocFiles(mainPackage);
		List<IFile> includedFeatureFiles = stubbyPackageModel.getIncudedFeatures();
		// Populate Sub package model
		List<SubPackage> subPackages = new ArrayList<SubPackage>();
		for (IFile includedFeatureFile: includedFeatureFiles) {
			StubbyPackageModel strubbySubPackageModel = new StubbyPackageModel(
					includedFeatureFile);
			SubPackage subPackage = new SubPackage();
			strubbySubPackageModel.populatePackageData(subPackage);
			subPackages.add(subPackage);
		}
		if (!stubbyPackageModel.isAllIncludedFeatureFound()) {
			String message = "";
			if (stubbyPackageModel.getMissingFeaturesAsString().indexOf(",") >= 0) {
				message = "Stub out an RPM specfile for '"
					+ mainPackage.getName()
					+ "' fails because '"
					+ stubbyPackageModel.getMissingFeaturesAsString()
					+ "' features were not found.\n\n"
					+ "Please, add these features somewhere in your workspace.\n";
			} else {
				message = "Stub out an RPM specfile for '"
					+ mainPackage.getName()
					+ "' fails because '"
					+ stubbyPackageModel.getMissingFeaturesAsString()
					+ "' feature was not found.\n\n"
					+ "Please, add these feature somewhere in your workspace.\n";
			}
				MessageDialog.openError(StubbyPlugin.getActiveWorkbenchShell(),
						null, message);
		} else {
			// Write generated files to the main feature project
			StubbyGenerator generator = new StubbyGenerator(mainPackage,
					subPackages);
			String packageName = "eclipse-"
					+ generator.getPackageName(mainPackage.getName());
			try {
				generator.writeContent(featureFile.getProject().getName(), packageName.toLowerCase() + ".spec", generator.generateSpecfile());
			} catch (CoreException e) {
				StubbyLog.logError(e);
			}
		}

	}

}
