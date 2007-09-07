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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpmstubby.model.IPackage;
import org.eclipse.linuxtools.rpmstubby.model.IPackagePreamble;
import org.eclipse.linuxtools.rpmstubby.model.PackageItem;
import org.eclipse.update.core.IIncludedFeatureReference;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.core.model.FeatureModel;
import org.eclipse.update.core.model.FeatureModelFactory;
import org.eclipse.update.core.model.ImportModel;
import org.eclipse.update.core.model.PluginEntryModel;
import org.eclipse.update.core.model.URLEntryModel;
import org.xml.sax.SAXException;

public class StubbyPackageModel {
	
	private static final String valueNoFoundMessage = "FIXME";
	private String featurePropertiesFile;
	private FeatureModel featureModel;
	private List includedFeatureFiles = new ArrayList();
	private String[] includedFeatureIdentifiers;
	private List includedFeatureIdentifiersAdded;
	
	public StubbyPackageModel(IFile featureFile) {
		this.featurePropertiesFile = featureFile.getLocation().removeLastSegments(1).toOSString() + "/feature.properties";
		FeatureModelFactory featureModelFactory = new FeatureModelFactory();
		try {
			this.featureModel = featureModelFactory
			.parseFeature(featureFile.getContents());
		} catch (CoreException e) {
			// Can be throw if the file does not exist. 
			e.printStackTrace();
		} catch (SAXException e) {
			// Probably malformed feature.xml?
			e.printStackTrace();
		}
	}
	
	public void populatePackageData(IPackage packageModel) {
		packageModel.setName(getFeatureName());
		packageModel.setVersion(getVersion());
		packageModel.setSummary(getSummary());
		packageModel.setDescription(getDescription());
		packageModel.setProvides(getProvides());
		packageModel.setRequires(getRequires());
	}
	
	public void populatePackagePreambleData(IPackagePreamble packagePreambleModel) {
		packagePreambleModel.setURL(getURL());
		packagePreambleModel.setLicense(getLicense());
	}
	
	public List getIncudedFeatures() {
		FeatureModelFactory featureModelFactory = new FeatureModelFactory();
		IIncludedFeatureReference[] includedFeatureReferences = featureModel.getFeatureIncluded();
		includedFeatureIdentifiers = new String[includedFeatureReferences.length];
		includedFeatureIdentifiersAdded = new ArrayList();
		try {
			for (int i = 0; i < includedFeatureReferences.length; i++) {
				VersionedIdentifier versionedIdentifier = includedFeatureReferences[i].getVersionedIdentifier();
				includedFeatureIdentifiers[i] = versionedIdentifier.getIdentifier();
			}
	        IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
	        IProject[] projects = workspace.getProjects();
	        for (int i = 0; i < projects.length; i++) {
				FeatureVisitor featureVisitor = new FeatureVisitor();
				projects[i].accept(featureVisitor);
		        for (Iterator iterator = featureVisitor.getFeatures().iterator(); iterator.hasNext();) {
		        	IFile featureFile = (IFile) iterator.next();
		        	FeatureModel includedFeatureModel = featureModelFactory.parseFeature(featureFile.getContents());
		        	if (checkFeatureIncluded(includedFeatureModel.getFeatureIdentifier())) {
		        		// Each feature that include other feature is considered as a
		        		// top-level RPM package
		        		if (includedFeatureModel.getFeatureIncluded().length > 0) {
		        			SpecfileWriter specfileWriter = new SpecfileWriter();
		        			specfileWriter.write(featureFile);
		        		} else 
		        			includedFeatureIdentifiersAdded.add(includedFeatureModel.getFeatureIdentifier());
		        			includedFeatureFiles.add(featureFile);
		        	}
		        }
	        }
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} 
		return includedFeatureFiles;
	}
	
	public boolean isAllIncludedFeatureFound() {
		if (includedFeatureFiles.size() == includedFeatureIdentifiers.length)
			return true;
		else
			return false;
	}
	
	public String getMissingFeaturesAsString() {
		String toRet = "";
		for (int i = 0; i < includedFeatureIdentifiers.length; i++) {
			if (!includedFeatureIdentifiersAdded.contains(includedFeatureIdentifiers[i]));
			toRet += includedFeatureIdentifiers[i] + ", ";
		}
		return toRet.substring(0, toRet.length() - 2 );
	}
	
	private boolean checkFeatureIncluded(String featureIdetifier) {
		for (int i = 0; i < includedFeatureIdentifiers.length; i++) {
			// Check if the feature found by the visitor is a included feature 
			if (includedFeatureIdentifiers[i].equals(featureIdetifier)) {
				// Check if the given feature is not already added the the included feature list.
				for(Iterator iterator = includedFeatureIdentifiersAdded.iterator(); iterator.hasNext();) {
					if (iterator.next().equals(featureIdetifier)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;		
	}
	
	private String getFeatureName() {
		return featureModel.getFeatureIdentifier();
	}
	
	private String getVersion() {
		return featureModel.getFeatureVersion();
	}
	
	private String getSummary() {
		return resolveFeatureProperties(featureModel.getLabel());
	}
	
	private String getLicense() {
		String license = valueNoFoundMessage;
		URLEntryModel licenseModel = featureModel.getLicenseModel();
		if (licenseModel != null) {
			String urlString = resolveFeatureProperties(licenseModel.getURLString());
			if (urlString.contains("epl")) {
				String newLicense = "EPL";
				if (!license.equals(newLicense)) {
					license = newLicense;
				}
			}
		}
		return license;
	}
	
	private String getURL() {
		String url = valueNoFoundMessage;
		URLEntryModel descriptionModel = featureModel.getDescriptionModel();
		if (descriptionModel != null && descriptionModel.getURLString() != null)
			url = descriptionModel.getURLString();
		return url;
	}
	
	private String getDescription() {
		URLEntryModel descriptionModel = featureModel.getDescriptionModel();
		if (descriptionModel.getAnnotation() != null) 
			return resolveFeatureProperties(descriptionModel.getAnnotation());
		else
			return valueNoFoundMessage;
	}

	private PackageItem[] getProvides() {
		// Plugins that are part of this feature
		PluginEntryModel[] includedPlugins = featureModel.getPluginEntryModels();
		PackageItem[] provides = new PackageItem[includedPlugins.length];
		String pluginVersion;
		for (int i = 0; i < includedPlugins.length; i++) {
			PackageItem provide = new PackageItem();
			provide.setName(includedPlugins[i].getPluginIdentifier());
			provide.setOperator("=");
			pluginVersion = includedPlugins[i].getPluginVersion();
			// If the bundle version retrieve in the feature is 0.0.0, we try to get it using OSGIUtils.
			if (pluginVersion.equals("0.0.0")) {
				pluginVersion = getBundleValue(provide.getName(), "Bundle-Version");
				if (pluginVersion == null)
					pluginVersion = "0.0.0";
			}
			provide.setVersion(pluginVersion);
			provides[i] = provide;
		}
		return provides;
	}
	
	private PackageItem[] getRequires() {
		ImportModel[] importModels = featureModel.getImportModels();
		PackageItem[] requires = new PackageItem[importModels.length];
		for (int i = 0; i < importModels.length; i++) {
			PackageItem require = new PackageItem();
			require.setName(importModels[i].getIdentifier());
			String pluginVersion = importModels[i].getVersion();
			String operator = "";
			// If the bundle version retrieve in the feature is 0.0.0, we try to get it using OSGIUtils.			
			if (pluginVersion.equals("0.0.0")) {
				pluginVersion = getBundleValue(require.getName(), "Bundle-Version");
				if (pluginVersion == null)
					pluginVersion = "0.0.0";
			}
			if (!pluginVersion.equals("0.0.0")) {
				require.setVersion(pluginVersion);
				if (importModels[i].getMatchingRuleName().equals("greaterOrEqual"))
					operator = ">=";
				else if (importModels[i].getMatchingRuleName().equals("greater"))
					operator = ">";
				else if (importModels[i].getMatchingRuleName().equals("lessOrEqual"))
					operator = "<=";
				else if (importModels[i].getMatchingRuleName().equals("less"))
					operator = "<";
				else
					operator = "=";
			} else {
				require.setVersion("");
			}
			require.setOperator(operator);
			requires[i] = require;
		}
		return requires;
	}
	
	/**
	 * Get value for a given key from the feature.properties file, if the key
	 * don't start with '%' we just return the given key.
	 * 
	 * @param key
	 *            to find in feature.properties
	 * @return the value
	 */
	private String resolveFeatureProperties(String key) {
		Properties properties = new Properties();
		if (key.startsWith("%")) {
			try {
				properties.load(new FileInputStream(featurePropertiesFile));
			} catch (FileNotFoundException e) {
				// Do nothing if the feature.properties is not found
			} catch (IOException e) {
				e.printStackTrace();
			}        	
        	return properties.getProperty(key.replaceAll("%", ""));
        } else {
        	return key;
        }
	}
	
	/**
	 * Get bundle value for a given bundleID, bundleKey
	 * 
	 * @param bundleID
	 *            the bundle
	 * @param bundleKey
	 *            the bundle key
	 * @return the value if the bundle key or null if the bundle key is not
	 *         found.
	 */
	private String getBundleValue(String bundleID, String bundleKey) {
		try {
			return (String) OSGIUtils.getDefault().getBundle(bundleID)
					.getHeaders().get(bundleKey);
		} catch (NullPointerException exception) {
			return null;
		}
	}

}
