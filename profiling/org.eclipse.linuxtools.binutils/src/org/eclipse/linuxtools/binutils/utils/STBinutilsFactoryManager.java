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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.binutils.Activator;

/**
 * This class is on charge of managing "org.eclipse.linuxtools.binutils.crossCompilerBinutils" extension point.
 * 
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 * 
 */
public class STBinutilsFactoryManager {

    public static final STBinutilsFactoryManager sharedInstance = new STBinutilsFactoryManager();

    /**
     * Map of CPU/ISTBinutilsFactory
     */
    private final Map<String, ISTBinutilsFactory> map = new HashMap<String, ISTBinutilsFactory>();

    /**
     * Default factory
     */
    private DefaultBinutilsFactory defaultFactory;

    /**
     * Private constructor: this class is implemented as a Singleton
     */
    private STBinutilsFactoryManager() {
    }

    /**
     * Try to find an extension point matching the given cpu; Then test availability of the tools. If no match, return
     * default binutils factory
     */
    private ISTBinutilsFactory getBinutilsFactoryImpl(String cpu) {
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
        if (defaultFactory == null)
            defaultFactory = new DefaultBinutilsFactory();
        return defaultFactory;
    }

    /**
     * Get a ISTBinutilsFactory matching the given cpu id Returns the default one if no match.
     */
    public ISTBinutilsFactory getBinutilsFactory(String cpu) {
        ISTBinutilsFactory factory = map.get(cpu);
        if (factory == null) {
            factory = getBinutilsFactoryImpl(cpu);
            map.put(cpu, factory);
        }
        return factory;
    }

}
