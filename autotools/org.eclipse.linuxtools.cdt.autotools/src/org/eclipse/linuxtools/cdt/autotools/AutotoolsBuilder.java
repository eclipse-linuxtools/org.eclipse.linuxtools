/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools;

import java.util.Map;

import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.macros.IFileContextBuildMacroValues;
import org.eclipse.cdt.managedbuilder.macros.IReservedMacroNameSupplier;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.linuxtools.cdt.autotools.ui.properties.AutotoolsPropertyConstants;



// Proxy class for IBuilder to allow overriding of getBuildLocation().

public class AutotoolsBuilder implements IBuilder {

	private IBuilder builder;
	private String buildPath;
	private IProject project;
	
	public AutotoolsBuilder(IBuilder builder, IProject project) {
		this.builder = builder;
		this.project = project;
	}
	
	protected IProject getProject() {
		return project;
	}
	
	public boolean canKeepEnvironmentVariablesInBuildfile() {
		// TODO Auto-generated method stub
		return builder.canKeepEnvironmentVariablesInBuildfile();
	}

	public String getArguments() {
		// TODO Auto-generated method stub
		return builder.getArguments();
	}

	public CBuildData getBuildData() {
		// TODO Auto-generated method stub
		return builder.getBuildData();
	}

	public IManagedBuilderMakefileGenerator getBuildFileGenerator() {
		// TODO Auto-generated method stub
		return builder.getBuildFileGenerator();
	}

	public IConfigurationElement getBuildFileGeneratorElement() {
		// TODO Auto-generated method stub
		return builder.getBuildFileGeneratorElement();
	}

	public String getBuildPath() {
		// TODO Auto-generated method stub
		return buildPath;
	}

	public String getBuilderVariablePattern() {
		// TODO Auto-generated method stub
		return builder.getBuilderVariablePattern();
	}

	public String getCommand() {
		// TODO Auto-generated method stub
		return builder.getCommand();
	}

	public String getConvertToId() {
		// TODO Auto-generated method stub
		return builder.getConvertToId();
	}

	public String getErrorParserIds() {
		// TODO Auto-generated method stub
		return builder.getErrorParserIds();
	}

	public String[] getErrorParserList() {
		// TODO Auto-generated method stub
		return builder.getErrorParserList();
	}

	public IFileContextBuildMacroValues getFileContextBuildMacroValues() {
		// TODO Auto-generated method stub
		return builder.getFileContextBuildMacroValues();
	}

	public IToolChain getParent() {
		// TODO Auto-generated method stub
		return builder.getParent();
	}

	public IReservedMacroNameSupplier getReservedMacroNameSupplier() {
		// TODO Auto-generated method stub
		return builder.getReservedMacroNameSupplier();
	}

	public String[] getReservedMacroNames() {
		// TODO Auto-generated method stub
		return builder.getReservedMacroNames();
	}

	public IBuilder getSuperClass() {
		// TODO Auto-generated method stub
		return builder.getSuperClass();
	}

	public String getUniqueRealName() {
		// TODO Auto-generated method stub
		return builder.getUniqueRealName();
	}

	public String getUnusedChildren() {
		// TODO Auto-generated method stub
		return builder.getUnusedChildren();
	}

	public String getVersionsSupported() {
		// TODO Auto-generated method stub
		return builder.getVersionsSupported();
	}

	public boolean isAbstract() {
		// TODO Auto-generated method stub
		return builder.isAbstract();
	}

	public boolean isCustomBuilder() {
		// TODO Auto-generated method stub
		return builder.isCustomBuilder();
	}

	public boolean isDirty() {
		// TODO Auto-generated method stub
		return builder.isDirty();
	}

	public boolean isExtensionElement() {
		// TODO Auto-generated method stub
		return builder.isExtensionElement();
	}

	public boolean isInternalBuilder() {
		// TODO Auto-generated method stub
		return builder.isInternalBuilder();
	}

	public boolean isSystemObject() {
		// TODO Auto-generated method stub
		return builder.isSystemObject();
	}

	public boolean isVariableCaseSensitive() {
		// TODO Auto-generated method stub
		return builder.isVariableCaseSensitive();
	}

	public boolean keepEnvironmentVariablesInBuildfile() {
		// TODO Auto-generated method stub
		return builder.keepEnvironmentVariablesInBuildfile();
	}

	public boolean matches(IBuilder builder) {
		// TODO Auto-generated method stub
		return builder.matches(builder);
	}

	public void setArguments(String makeArgs) {
		// TODO Auto-generated method stub
		builder.setArguments(makeArgs);
	}

	public void setBuildFileGeneratorElement(IConfigurationElement element) {
		// TODO Auto-generated method stub
		builder.setBuildFileGeneratorElement(element);
	}

	public void setBuildPath(String path) {
		// TODO Auto-generated method stub
		this.buildPath = path;
	}

	public void setCommand(String command) {
		// TODO Auto-generated method stub
		builder.setCommand(command);
	}

	public void setConvertToId(String convertToId) {
		// TODO Auto-generated method stub
		builder.setConvertToId(convertToId);
	}

	public void setDirty(boolean isDirty) {
		// TODO Auto-generated method stub
		builder.setDirty(isDirty);
	}

	public void setErrorParserIds(String ids) {
		// TODO Auto-generated method stub
		builder.setErrorParserIds(ids);
	}

	public void setIsAbstract(boolean b) {
		// TODO Auto-generated method stub
		builder.setIsAbstract(b);
	}

	public void setKeepEnvironmentVariablesInBuildfile(boolean keep) {
		// TODO Auto-generated method stub
		builder.setKeepEnvironmentVariablesInBuildfile(keep);
	}

	public void setVersionsSupported(String versionsSupported) {
		// TODO Auto-generated method stub
		builder.setVersionsSupported(versionsSupported);
	}	

	public boolean supportsCustomizedBuild() {
		// TODO Auto-generated method stub
		return builder.supportsCustomizedBuild();
	}

	public String getBaseId() {
		// TODO Auto-generated method stub
		return builder.getBaseId();
	}

	public String getId() {
		// TODO Auto-generated method stub
		return builder.getId();
	}

	public String getManagedBuildRevision() {
		// TODO Auto-generated method stub
		return builder.getManagedBuildRevision();
	}

	public String getName() {
		// TODO Auto-generated method stub
		return builder.getName();
	}

	public PluginVersionIdentifier getVersion() {
		// TODO Auto-generated method stub
		return builder.getVersion();
	}

	public void setVersion(PluginVersionIdentifier version) {
		// TODO Auto-generated method stub
		builder.setVersion(version);
	}

	public String getAutoBuildTarget() {
		// TODO Auto-generated method stub
		return builder.getAutoBuildTarget();
	}

	public String getCleanBuildTarget() {
		String target = null;
		try {
			target = getProject().getPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET);
		} catch (CoreException ce) {
			// do nothing
		}
		if (target == null)
			target = AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT;
		return target;
	}

	public String getFullBuildTarget() {
		// TODO Auto-generated method stub
		return builder.getFullBuildTarget();
	}

	public String getIncrementalBuildTarget() {
		// TODO Auto-generated method stub
		return builder.getIncrementalBuildTarget();
	}

	public boolean isAutoBuildEnable() {
		// TODO Auto-generated method stub
		return builder.isAutoBuildEnable();
	}

	public boolean isCleanBuildEnabled() {
		// TODO Auto-generated method stub
		return builder.isCleanBuildEnabled();
	}

	public boolean isFullBuildEnabled() {
		// TODO Auto-generated method stub
		return builder.isFullBuildEnabled();
	}

	public boolean isIncrementalBuildEnabled() {
		// TODO Auto-generated method stub
		return builder.isIncrementalBuildEnabled();
	}

	public void setAutoBuildEnable(boolean enabled) throws CoreException {
		// TODO Auto-generated method stub
		builder.setAutoBuildEnable(enabled);
	}

	public void setAutoBuildTarget(String target) throws CoreException {
		// TODO Auto-generated method stub
		builder.setAutoBuildTarget(target);
	}

	public void setCleanBuildEnable(boolean enabled) throws CoreException {
		// TODO Auto-generated method stub
		builder.setCleanBuildEnable(enabled);
	}

	public void setCleanBuildTarget(String target) throws CoreException {
		// TODO Auto-generated method stub
		builder.setCleanBuildTarget(target);
	}

	public void setFullBuildEnable(boolean enabled) throws CoreException {
		// TODO Auto-generated method stub
		builder.setFullBuildEnable(enabled);
	}

	public void setFullBuildTarget(String target) throws CoreException {
		// TODO Auto-generated method stub
		builder.setFullBuildTarget(target);
	}

	public void setIncrementalBuildEnable(boolean enabled) throws CoreException {
		// TODO Auto-generated method stub
		builder.setIncrementalBuildEnable(enabled);
	}

	public void setIncrementalBuildTarget(String target) throws CoreException {
		// TODO Auto-generated method stub
		builder.setIncrementalBuildTarget(target);
	}

	public boolean appendEnvironment() {
		// TODO Auto-generated method stub
		return builder.appendEnvironment();
	}

	public String getBuildArguments() {
		// TODO Auto-generated method stub
		return builder.getBuildArguments();
	}

	public String getBuildAttribute(String name, String defaultValue) {
		// TODO Auto-generated method stub
		return builder.getBuildAttribute(name, defaultValue);
	}

	public IPath getBuildCommand() {
		// TODO Auto-generated method stub
		return builder.getBuildCommand();
	}

	public IPath getBuildLocation() {
		// TODO Auto-generated method stub
		return new Path(getBuildPath());
	}

	public Map getEnvironment() {
		// TODO Auto-generated method stub
		return builder.getEnvironment();
	}

	public String[] getErrorParsers() {
		// TODO Auto-generated method stub
		return builder.getErrorParsers();
	}

	public Map getExpandedEnvironment() throws CoreException {
		// TODO Auto-generated method stub
		return builder.getExpandedEnvironment();
	}

	public int getParallelizationNum() {
		// TODO Auto-generated method stub
		return builder.getParallelizationNum();
	}

	public boolean isDefaultBuildCmd() {
		// TODO Auto-generated method stub
		return builder.isDefaultBuildCmd();
	}

	public boolean isManagedBuildOn() {
		// TODO Auto-generated method stub
		return builder.isManagedBuildOn();
	}

	public boolean isParallelBuildOn() {
		// TODO Auto-generated method stub
		return builder.isParallelBuildOn();
	}

	public boolean isStopOnError() {
		// TODO Auto-generated method stub
		return builder.isStopOnError();
	}

	public void setAppendEnvironment(boolean append) throws CoreException {
		// TODO Auto-generated method stub
		builder.setAppendEnvironment(append);
	}

	public void setBuildArguments(String args) throws CoreException {
		// TODO Auto-generated method stub
		builder.setBuildArguments(args);
	}

	public void setBuildAttribute(String name, String value)
			throws CoreException {
		// TODO Auto-generated method stub
		 builder.setBuildAttribute(name, value);
	}

	public void setBuildCommand(IPath command) throws CoreException {
		// TODO Auto-generated method stub
		builder.setBuildCommand(command);
	}

	public void setBuildLocation(IPath location) throws CoreException {
		// TODO Auto-generated method stub
		builder.setBuildLocation(location);
	}	

	public void setEnvironment(Map env) throws CoreException {
		// TODO Auto-generated method stub
		builder.setEnvironment(env);
	}

	public void setErrorParsers(String[] parsers) throws CoreException {
		// TODO Auto-generated method stub
		builder.setErrorParsers(parsers);
	}

	public void setManagedBuildOn(boolean on) throws CoreException {
		// TODO Auto-generated method stub
		builder.setManagedBuildOn(on);
	}

	public void setParallelBuildOn(boolean on) throws CoreException {
		// TODO Auto-generated method stub
		builder.setParallelBuildOn(on);
	}

	public void setParallelizationNum(int num) throws CoreException {
		// TODO Auto-generated method stub
		builder.setParallelizationNum(num);
	}

	public void setStopOnError(boolean on) throws CoreException {
		// TODO Auto-generated method stub
		builder.setStopOnError(on);
	}	

	public void setUseDefaultBuildCmd(boolean on) throws CoreException {
		// TODO Auto-generated method stub
		builder.setUseDefaultBuildCmd(on);
	}

	public boolean supportsBuild(boolean managed) {
		// TODO Auto-generated method stub
		return builder.supportsBuild(managed);
	}

	public boolean supportsParallelBuild() {
		// TODO Auto-generated method stub
		return builder.supportsParallelBuild();
	}

	public boolean supportsStopOnError(boolean on) {
		// TODO Auto-generated method stub
		return builder.supportsStopOnError(on);
	}
}
