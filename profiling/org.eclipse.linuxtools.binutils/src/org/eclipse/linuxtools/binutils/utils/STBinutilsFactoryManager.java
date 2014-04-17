/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.binutils.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.Activator;

/**
 * This class is on charge of managing "org.eclipse.linuxtools.binutils.crossCompilerBinutils" extension point.
 *
 */
public class STBinutilsFactoryManager {

    /**
     * Map of CPU/ISTBinutilsFactory
     */
    private static final Map<String, ISTBinutilsFactory> map = new HashMap<>();

    /**
     * Default factory
     */
    private static DefaultBinutilsFactory defaultFactory;

    /**
     * Private constructor: this class is implemented as a Singleton
     */
    private STBinutilsFactoryManager() {
    }

    /**
     * Try to find an extension point matching the given cpu; Then test availability of the tools. If no match, return
     * default binutils factory
     * @param cpu The cpu identifier.
     * @return The factory.
     */
    private static ISTBinutilsFactory getBinutilsFactoryImpl(String cpu) {
        try {
            IExtensionRegistry reg = Platform.getExtensionRegistry();
            IExtensionPoint ep = reg.getExtensionPoint("org.eclipse.linuxtools.binutils.crossCompilerBinutils"); //$NON-NLS-1$
            IExtension[] exts = ep.getExtensions();
            for (IExtension extension : exts) {
                IConfigurationElement[] elems = extension.getConfigurationElements();
                for (IConfigurationElement configurationElement : elems) {
                    String s = configurationElement.getAttribute("CPU"); //$NON-NLS-1$
                    if (cpu.equals(s)) {
                        ISTBinutilsFactory factory = (ISTBinutilsFactory) configurationElement
                                .createExecutableExtension("binutilsFactory"); //$NON-NLS-1$
                        if (factory.testAvailability())
                            return factory;
                    }
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().getLog().log(e.getStatus());
        }
        if (defaultFactory == null) {
            defaultFactory = new DefaultBinutilsFactory();
        }
        return defaultFactory;
    }

    /**
     * Get a ISTBinutilsFactory matching the given cpu id Returns the default one if no match.
     * @param cpu The cpu identifier.
     * @return THe factory
     */
    public static ISTBinutilsFactory getBinutilsFactory(String cpu) {
        ISTBinutilsFactory factory = map.get(cpu);
        if (factory == null) {
            factory = getBinutilsFactoryImpl(cpu);
            map.put(cpu, factory);
        }
        return factory;
    }

    /**
     * @param cpuType
     * @param programPath
     * @param project
     *            The project to get the path to run addr2line
     * @return an instance of addr2line for the given program
     * @throws IOException
     * @since 5.0
     */
    public static Addr2line getAddr2line(String cpuType, String programPath, IProject project) throws IOException {
        ISTBinutilsFactory factory = getBinutilsFactory(cpuType);
        return factory.getAddr2line(programPath, project);
    }

    /**
     * @param cpuType
     * @param project
     *            The project to get the path to run cppfilt
     * @return an instance of cppfile for the given cpu type
     * @throws IOException
     * @since 5.0
     */
    public static CPPFilt getCPPFilt(String cpuType, IProject project) throws IOException {
        ISTBinutilsFactory factory = getBinutilsFactory(cpuType);
        return factory.getCPPFilt(project);
    }

    /**
     * @param cpuType
     * @param programPath
     * @param handler
     * @param project
     *            The project to get the path to be used to run nm
     * @return an instance of nm for the given program
     * @throws IOException
     * @since 5.0
     */
    public static STNM getNM(String cpuType, String programPath, STNMSymbolsHandler handler, IProject project)
            throws IOException {
        ISTBinutilsFactory factory = STBinutilsFactoryManager.getBinutilsFactory(cpuType);
        return factory.getNM(programPath, handler, project);
    }

}
