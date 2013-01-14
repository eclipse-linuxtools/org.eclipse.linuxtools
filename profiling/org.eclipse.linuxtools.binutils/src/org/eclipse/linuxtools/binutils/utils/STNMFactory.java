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

import org.eclipse.core.resources.IProject;

import org.eclipse.linuxtools.binutils.utils.STNM;

public class STNMFactory {

    /**
     * @param cpuType
     * @param programPath
     * @return an instance of nm for the given program
     * @throws IOException
     */
    public static STNM getNM(String cpuType, String programPath, STNMSymbolsHandler handler) throws IOException {
        return getNM(cpuType, programPath, handler, null);
    }

    /**
     * @param cpuType
     * @param programPath
     * @param project
     *            The project to get the path to be used to run nm
     * @return an instance of nm for the given program
     * @throws IOException
     */
    public static STNM getNM(String cpuType, String programPath, STNMSymbolsHandler handler, IProject project)
            throws IOException {
        ISTBinutilsFactory factory = STBinutilsFactoryManager.sharedInstance.getBinutilsFactory(cpuType);
        return factory.getNM(programPath, handler, project);
    }

}
