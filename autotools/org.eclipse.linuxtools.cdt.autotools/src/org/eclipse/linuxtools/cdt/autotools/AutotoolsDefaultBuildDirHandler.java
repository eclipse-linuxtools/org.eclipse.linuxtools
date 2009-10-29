/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedOptionValueHandler;

public class AutotoolsDefaultBuildDirHandler extends ManagedOptionValueHandler 
	implements IOptionApplicability {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler#handleValue(IConfiguration,IToolChain,IOption,String,int)
	 */
	
	public final static String DEFAULT_BUILD_DIR = "build"; //$NON-NLS-1$
	public final static String CONFIGURE_TOOL_ID = "org.eclipse.linuxtools.cdt.autotools.gnu.toolchain.tool.configure"; //$NON-NLS-1$
	public final static String BUILD_DIR_OPTION_ID = "org.eclipse.linuxtools.cdt.autotools.option.configure.builddir"; //$NON-NLS-1$
	public final static String BUILD_DIR_APPLY = "BuildDir.apply"; //$NON-NLS-1$
	public final static String BUILD_DIR_DEFAULT_QUESTION = "BuildDir.default"; //$NON-NLS-1$
	public final static String BUILD_DIR_YES = "BuildDir.yes"; //$NON-NLS-1$
	public final static String BUILD_DIR_NO = "BuildDir.no"; //$NON-NLS-1$

	//FIXME: Use holder to set option value, not the "option" parameter
	public boolean handleValue(IBuildObject buildObject, 
                   IHoldsOptions holder, 
                   IOption option,
                   String extraArgument, int event)
	{
		// Get the current value of the build dir option.
		String value = (String)option.getValue();
		String valueBase = value;

		if (buildObject instanceof IConfiguration &&
				(event == IManagedOptionValueHandler.EVENT_OPEN)) {
//						|| event == IManagedOptionValueHandler.EVENT_APPLY)) {
			SortedSet<Integer> nums = new TreeSet<Integer>();
			IConfiguration configuration = (IConfiguration)buildObject;
			IConfiguration[] cfgs = configuration.getManagedProject().getConfigurations();
			int index = 1;
			boolean valueFound = false;
			for (int i = 0; i < cfgs.length; ++i) {
				IConfiguration config = cfgs[i];
				if (config == null || config.getName().equals(configuration.getName())) {
					continue;
				}
				ITool tool = config.getToolFromOutputExtension("status");  //$NON-NLS-1$
				if (tool == null) // can happen if user has purposely mixed autotools and non-autotools cfgs
					continue;
				
				// We now want to get the builddir option for the tool.  If we use
				// getOptionById(), we must know the full id which in our case has a generated
				// numeric extension at the end.  Otherwise, the base builddir option id
				// will get us the default option, not the one for the configuration we
				// are currently looking at.  We use getOptionBySuperClassId() instead
				// which will find us options that are based off the original builddir
				// option which include those with generated extensions at the end.
				IOption buildDirOption = tool.getOptionBySuperClassId(BUILD_DIR_OPTION_ID);
				String buildDir = (String)buildDirOption.getValue();
				if (buildDir.equals(value)) {
					valueFound = true;
				}
				// For "buildXX" values, store the XX values in a list of used extensions.
				if (buildDir.startsWith(DEFAULT_BUILD_DIR)) {
					String numstr = buildDir.substring(DEFAULT_BUILD_DIR.length());
					try {
						Integer k = Integer.valueOf(numstr);
						// Assume the value to start with is the last value in the list
						// plus 1.
						index = k.intValue() + 1;
						nums.add(k);
					} catch (NumberFormatException e) {
						// ignore
					}
				}
			}
			
			// If there is no name collision for the configurations, then we simply return.
			if (!valueFound)
				return true;
			
//			// If the user has applied a change and it matches an existing build directory,
//			// then warn and ask if the user wants the value defaulted to a safe value.
//			if (event == EVENT_APPLY) {
//				String title = AutotoolsPlugin.getResourceString(BUILD_DIR_APPLY);
//				String question = AutotoolsPlugin.getResourceString(BUILD_DIR_DEFAULT_QUESTION);
//				String[] buttonLabels = new String[2];
//				buttonLabels[0] = AutotoolsPlugin.getResourceString(BUILD_DIR_YES);
//				buttonLabels[1] = AutotoolsPlugin.getResourceString(BUILD_DIR_NO);
//				MessageDialog d = new MessageDialog(AutotoolsPlugin.getActiveWorkbenchShell(),
//						title, null, question, MessageDialog.QUESTION, buttonLabels, 0);
//				int result = d.open();
//				if (result == 1)
//					return true;
//			}
			
			// For defaulted buildXX values, we support defaulting a unique XX value.
			if (value.startsWith(DEFAULT_BUILD_DIR)) {
				valueBase = DEFAULT_BUILD_DIR;
				// Try and establish a unique "buildXX" name that hasn't been used yet.
				while (nums.contains(Integer.valueOf(index))) {
					++index;
				}
			}
			
			// Reset the default build directory for this opened configuration.
			try {
				IOption optionToSet = holder.getOptionToSet(option, false);
				optionToSet.setValue(valueBase + index);
			} catch (BuildException e) {
				return false;
			}
		}
		
		// The event was not handled, thus return false
		return true;
	}
	
	// IOptionApplicability methods
	
	public boolean isOptionEnabled(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return true;
	}

	public boolean isOptionUsedInCommandLine(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return false;
	}

	public boolean isOptionVisible(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		if (option.getName().equals("includes") || option.getName().equals("symbols"))
			return false;
		return true;
	}


}
