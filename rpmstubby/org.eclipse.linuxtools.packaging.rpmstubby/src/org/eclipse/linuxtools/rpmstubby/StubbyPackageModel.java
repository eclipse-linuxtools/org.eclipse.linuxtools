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
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
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
	private List<IFile> includedFeatureFiles = new ArrayList<IFile>();
	private List<String> includedFeatureIdentifiers;
	private List<String> includedFeatureIdentifiersAdded;

	public StubbyPackageModel(IFile featureFile) {
		this.featurePropertiesFile = featureFile.getLocation()
				.removeLastSegments(1).toOSString()
				+ "/feature.properties";
		FeatureModelFactory featureModelFactory = new FeatureModelFactory();
		try {
			this.featureModel = featureModelFactory.parseFeature(featureFile
					.getContents());
		} catch (CoreException e) {
			// Can be throw if the file does not exist.
			StubbyLog.logError(e);
		} catch (SAXException e) {
			// Probably malformed feature.xml?
			StubbyLog.logError(e);
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

	public void populatePackagePreambleData(
			IPackagePreamble packagePreambleModel) {
		packagePreambleModel.setURL(getURL());
		packagePreambleModel.setLicense(getLicense());
	}

	public List<IFile> getIncudedFeatures() {
		FeatureModelFactory featureModelFactory = new FeatureModelFactory();
		IIncludedFeatureReference[] includedFeatureReferences = featureModel
				.getFeatureIncluded();
		includedFeatureIdentifiers = new ArrayList<String>();
		includedFeatureIdentifiersAdded = new ArrayList<String>();
		try {
			for (IIncludedFeatureReference includedFeatureReference: includedFeatureReferences) {
				VersionedIdentifier versionedIdentifier = includedFeatureReference.getVersionedIdentifier();
				includedFeatureIdentifiers.add(versionedIdentifier
						.getIdentifier());
			}
			IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = workspace.getProjects();
			for (IProject project: projects) {
				FeatureVisitor featureVisitor = new FeatureVisitor();
				project.accept(featureVisitor);
				for (IFile featureFile : featureVisitor.getFeatures()) {
					FeatureModel includedFeatureModel = featureModelFactory
							.parseFeature(featureFile.getContents());
					if (isFeatureIncluded(includedFeatureModel
							.getFeatureIdentifier())) {
						// Each feature that include other features is
						// considered as a
						// top-level RPM package.
						if (includedFeatureModel.getFeatureIncluded().length > 0) {
							SpecfileWriter specfileWriter = new SpecfileWriter();
							specfileWriter.write(featureFile);
						} else
							includedFeatureIdentifiersAdded
									.add(includedFeatureModel
											.getFeatureIdentifier());
						includedFeatureFiles.add(featureFile);
					}
				}
			}
		} catch (CoreException e) {
			StubbyLog.logError(e);
		} catch (SAXException e) {
			StubbyLog.logError(e);
		}
		return includedFeatureFiles;
	}

	public boolean isAllIncludedFeatureFound() {
		if (includedFeatureFiles.size() == includedFeatureIdentifiers.size())
			return true;
		else
			return false;
	}

	public String getMissingFeaturesAsString() {
		String toRet = "";
		for (String includedFeatureIdentifier: includedFeatureIdentifiers) {
			if (!includedFeatureIdentifiersAdded
					.contains(includedFeatureIdentifier))
				;
			toRet += includedFeatureIdentifier + ", ";
		}
		return toRet.substring(0, toRet.length() - 2);
	}

	private boolean isFeatureIncluded(String featureIdetifier) {
		for (String includedFeatureIdentifier: includedFeatureIdentifiers) {
			// Check if the feature found by the visitor is a included feature
			if (includedFeatureIdentifier.equals(featureIdetifier)) {
				// Check if the given feature is not already added in the
				// included feature list.
				for (String includedFeatureID : includedFeatureIdentifiersAdded) {
					if (includedFeatureID.equals(featureIdetifier)) {
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
			String urlString = resolveFeatureProperties(licenseModel
					.getURLString());
			String urlAnotation = resolveFeatureProperties(licenseModel
					.getAnnotation());
			if ((urlString != null && urlAnotation != null)) {
					if ((urlString.indexOf("epl") > -1 || urlAnotation
							.indexOf("epl") > -1)) {
						license = "EPL";
					} else if ((urlString.indexOf("cpl") > -1 || urlAnotation
							.indexOf("cpl") > -1)) {
						license = "CPL";
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
		if (descriptionModel.getAnnotation() != null) {
			// Each description line contain maximum 80 characters.
			String[] descriptionToken = resolveFeatureProperties(
					descriptionModel.getAnnotation()).split(" ");
			String description = descriptionToken[0] + " ";
			// We add +2 because an array start at index 0 and one space is
			// removed for each token.
			int lineLenght = descriptionToken[0].length() + 2;
			int i;
			for (i = 2; i < descriptionToken.length; i++) {
				lineLenght += descriptionToken[i].length() + 2;
				if (lineLenght > 80) {
					description += descriptionToken[i - 1] + "\n";
					lineLenght = 0;
				} else {
					description += descriptionToken[i - 1] + " ";
				}
			}
			description += descriptionToken[i - 1];
			return description;
		} else
			return valueNoFoundMessage;
	}

	private List<PackageItem> getProvides() {
		// Plugins that are part of this feature
		PluginEntryModel[] includedPlugins = featureModel
				.getPluginEntryModels();
		List<PackageItem> providesList = new ArrayList<PackageItem>();
		String pluginVersion;
		for (PluginEntryModel includedPlugin : includedPlugins) {
				PackageItem provide = new PackageItem();
				provide.setName(includedPlugin.getPluginIdentifier());
				provide.setOperator("=");
				pluginVersion = includedPlugin.getPluginVersion();
				// If the bundle version retrieve in the feature is 0.0.0, we
				// try to get it using OSGIUtils.
				if (pluginVersion.equals("0.0.0")) {
					pluginVersion = getBundleValue(provide.getName(),
							"Bundle-Version");
					if (pluginVersion == null)
						pluginVersion = "0.0.0";
				}
				provide.setVersion(pluginVersion);
				providesList.add(provide);
		}
		return providesList;
	}

	private List<PackageItem> getRequires() {
		ImportModel[] importModels = featureModel.getImportModels();
		List<PackageItem> requiresList = new ArrayList<PackageItem>();
		for (ImportModel importModel: importModels) {
			PackageItem require = new PackageItem();
			require.setName(importModel.getIdentifier());
			String pluginVersion = importModel.getVersion();
			String operator = "";
			// If the bundle version retrieve in the feature is 0.0.0, we try to
			// get it using OSGIUtils.
			if (pluginVersion.equals("0.0.0")) {
				pluginVersion = getBundleValue(require.getName(),
						"Bundle-Version");
				if (pluginVersion == null)
					pluginVersion = "0.0.0";
			}
			if (!pluginVersion.equals("0.0.0")) {
				require.setVersion(pluginVersion);
				if (importModel.getMatchingRuleName().equals(
						"greaterOrEqual"))
					operator = ">=";
				else if (importModel.getMatchingRuleName()
						.equals("greater"))
					operator = ">";
				else if (importModel.getMatchingRuleName().equals(
						"lessOrEqual"))
					operator = "<=";
				else if (importModel.getMatchingRuleName().equals("less"))
					operator = "<";
				else
					operator = "=";
			} else {
				require.setVersion("");
			}
			require.setOperator(operator);
			requiresList.add(require);
		}
		return requiresList;
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
				StubbyLog.logError(e);
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
			return (String) Platform.getBundle(bundleID).getHeaders().get(
					bundleKey);
		} catch (NullPointerException exception) {
			return null;
		}
	}

}
