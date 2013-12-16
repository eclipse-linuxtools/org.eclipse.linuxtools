/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.utils;

import java.io.File;
import java.io.IOException;
import java.util.WeakHashMap;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.binutils.utils.STNMFactory;
import org.eclipse.linuxtools.binutils.utils.STNMSymbolsHandler;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;

public class STGcovProgramChecker implements STNMSymbolsHandler {

    private boolean gcovFound = false;
    private long timestamp;
    private final static WeakHashMap<File, STGcovProgramChecker> map = new WeakHashMap<>();

    /** Private Constructor */
    private STGcovProgramChecker(long timestamp) {
        this.timestamp = timestamp;
    }

    private static STGcovProgramChecker getProgramChecker(IBinaryObject object, IProject project) throws IOException {
        File program = object.getPath().toFile();
        STGcovProgramChecker pg = map.get(program);
        if (pg == null) {
            pg = new STGcovProgramChecker(program.lastModified());
            STNMFactory.getNM(object.getCPU(), object.getPath().toOSString(), pg, project);
            map.put(program, pg);
        } else {
            long fileTime = program.lastModified();
            if (fileTime > pg.timestamp) {
                pg.timestamp = fileTime;
                pg.gcovFound = false;
                STNMFactory.getNM(object.getCPU(), object.getPath().toOSString(), pg, project);
            }
        }
        return pg;
    }

    public static boolean isGCovCompatible(String s, IProject project) throws IOException {
        IBinaryObject object = STSymbolManager.sharedInstance.getBinaryObject(new Path(s));
        if (object == null)
            return false;
        return isGCovCompatible(object, project);
    }

    public static boolean isGCovCompatible(IBinaryObject object, IProject project) throws IOException {
        STGcovProgramChecker pg = getProgramChecker(object, project);
        return pg.gcovFound;
    }

    @Override
    public void foundBssSymbol(String symbol, String address) {
    }

    @Override
    public void foundDataSymbol(String symbol, String address) {
    }

    @Override
    public void foundTextSymbol(String symbol, String address) {
        if ("gcov_read_words".equals(symbol) || "_gcov_read_words".equals(symbol)) { //$NON-NLS-1$//$NON-NLS-2$
            gcovFound = true;
        }
    }

    @Override
    public void foundUndefSymbol(String symbol) {
    }

}
