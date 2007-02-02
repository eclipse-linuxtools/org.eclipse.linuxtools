/*******************************************************************************
 * Copyright (c) 2006 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpmstubby.popup.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.rpmstubby.StubbyPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.update.core.model.FeatureModel;
import org.eclipse.update.core.model.FeatureModelFactory;
import org.eclipse.update.core.model.ImportModel;
import org.eclipse.update.core.model.PluginEntryModel;
import org.eclipse.update.core.model.URLEntryModel;
import org.xml.sax.SAXException;

public class StubifyAction implements IObjectActionDelegate {

	ISelection selection;
	
	/**
	 * Constructor for StubifyAction.
	 */
	public StubifyAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		IStructuredSelection structuredSelection = null;
		ISelectionProvider provider= targetPart.getSite().getSelectionProvider();
		if (provider != null) {
			ISelection selection= provider.getSelection();
			if (selection instanceof IStructuredSelection)
				structuredSelection = (IStructuredSelection)selection;
		}
		structuredSelection = StructuredSelection.EMPTY;
		this.selection = (ISelection) structuredSelection.getFirstElement();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
		List /* of FeatureModel */ features = new ArrayList();
		
		/* SRPM Stuff */
		String name = "";
		String version = "";
		String release = "";
		String summary = "";
		String license = "";
		String url = "";

		List /* of ImportModel */ buildRequirements = new ArrayList();
		
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		for (Iterator selectionIter = structuredSelection.iterator(); selectionIter.hasNext();) {
			IProject project = null;
			IFile featureFile = null;
			
			Object selected = (Object) selectionIter.next();
			if (selected instanceof IProject) {
				project = (IProject) selected;
				featureFile = project.getFile(new Path("/feature.xml"));
			} else if (selected instanceof IFile) {
				featureFile = (IFile) selected;
				project = featureFile.getProject();
			} else {
				// FIXME:  error
			}
			
			// Process the feature
			FeatureModelFactory featureModelFactory = new FeatureModelFactory();
			try {
				FeatureModel featureModel = featureModelFactory
				.parseFeature(featureFile.getContents());
				features.add(featureModel);
			} catch (CoreException e) {
				// Hmm.  What could cause this?  Malformed feature.xml?
			} catch (SAXException e) {
				// Malformed feature.xml?
			}
		}
		
		for (Iterator iter = features.iterator(); iter.hasNext();) {
			FeatureModel featureModel = (FeatureModel) iter.next();
			URLEntryModel descriptionModel = featureModel.getDescriptionModel();

			if (descriptionModel != null)
				url = "URL:  " + descriptionModel.getURLString();
			else
				url = "URL:  FIXME";
			
			URLEntryModel licenseModel = featureModel.getLicenseModel();
			if (licenseModel != null) {
				String urlString = licenseModel.getURLString();
				if (urlString.contains("epl")) {
					String newLicense = "EPL";
					if (!license.equals(newLicense)) {
						license += " " + newLicense;
					}
				}
			}
			
			System.out.println("Version of " + featureModel.getFeatureIdentifier() + ":  " + featureModel.getFeatureVersion());
		}
		
		for (Iterator iter = features.iterator(); iter.hasNext();) {
			FeatureModel featureModel = (FeatureModel) iter.next();
			
			System.out.println(fixme("This should be a shorter name than the feature id"));
			System.out.println("%package  " + featureModel.getFeatureIdentifier());
			System.out.println("Summary:  " + featureModel.getLabel());
			System.out.println(fixme("What should the group for this sub-package be?"));
			// FIXME:  add a default?
			System.out.println("Group:");
			
			URLEntryModel descriptionModel = featureModel.getDescriptionModel();
			
			
			if (descriptionModel != null) {
				System.out.println("%description");
				System.out.println(descriptionModel.getAnnotation());
			} else {
				System.out.println("%description");
				System.out.println("FIXME");
			}
			
			System.out.println();
				
			// Plugins that are part of this feature
			PluginEntryModel[] includedPlugins = featureModel.getPluginEntryModels();
			for (int i = 0; i < includedPlugins.length; i++) {
				System.out.print("Provides:  " + includedPlugins[i].getPluginIdentifier());
				System.out.println(" = " + includedPlugins[i].getPluginVersion());
			}
			
			ImportModel[] requirements = getRequirements(featureModel);
			for (int i = 0; i < requirements.length; i++) {
				buildRequirements.add(requirements[i]);
			}
			System.out.println(printRequirements(getRequirements(featureModel), "Requires:  "));
		}
		
		System.out.println(fixme("What should the SRPM name be?"));
		System.out.println("Name:  ");
		System.out.println(fixme("What should the SRPM summary be?"));
		System.out.println("Summary:  ");
		System.out.println(fixme("What should the SRPM version be?"));
		System.out.println("Version:  ");
		System.out.println(fixme("What should the SRPM release be?"));
		System.out.println("Release:  ");
		// FIXME:  we can probably get this from below
		System.out.println(fixme("What should the SRPM license be?"));
		System.out.println("License:  ");
		// FIXME:  source(s) and/or generating script?
		// FIXME:  build script?
		System.out.println("BuildRoot:  %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)");
		System.out.println();
		// FIXME:  do we need an SRPM description?
		
		System.out.println(printRequirements((ImportModel[]) buildRequirements.toArray(), "BuildRequires:  "));
		Shell shell = StubbyPlugin.getActiveWorkbenchShell();
		MessageDialog.openInformation(
			shell,
			"Plug-in RPM Stubber",
			"Stub out an RPM specfile was executed.");
	}

	private String fixme(String info) {
		return "# FIXME:  " + info;
	}
	
	private ImportModel[] getRequirements(FeatureModel featureModel) {
		if (featureModel != null)
			return featureModel.getImportModels();
		return null;
	}

	private String printRequirements(ImportModel[] requiredPlugins, String prefix) {
		String toReturn = "";
		for (int i = 0; i < requiredPlugins.length; i++) {
			toReturn += prefix + requiredPlugins[i].getIdentifier();
			String requiredVersion = requiredPlugins[i].getVersion();
			if (!requiredVersion.equals("0.0.0")) {
				if (requiredPlugins[i].getMatchingRuleName().equals("greaterOrEqual"))
					toReturn += " >= ";
				else if (requiredPlugins[i].getMatchingRuleName().equals("greater"))
					toReturn += " > ";
				else if (requiredPlugins[i].getMatchingRuleName().equals("lessOrEqual"))
					toReturn += " <= ";
				else if (requiredPlugins[i].getMatchingRuleName().equals("less"))
					toReturn += " < ";
				else
					toReturn += " = ";
				toReturn += requiredPlugins[i].getVersion() + "\n";
			} else
				toReturn += "\n";
		}
		return toReturn;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
