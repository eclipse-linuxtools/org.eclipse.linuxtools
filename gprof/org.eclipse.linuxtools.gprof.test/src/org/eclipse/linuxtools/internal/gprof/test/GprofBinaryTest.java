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
package org.eclipse.linuxtools.internal.gprof.test;

import static org.eclipse.linuxtools.internal.gprof.test.STJunitUtils.BINARY_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.osgi.framework.FrameworkUtil;

@RunWith(Parameterized.class)
public class GprofBinaryTest {

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> params = new ArrayList<>();
        for (File testDir : STJunitUtils.getTestDirs()) {
            params.add(new Object[]{testDir.getName()+File.separator+BINARY_FILE});
        }
        return params;
    }

    private String path;
    public GprofBinaryTest(String path){
        this.path = path;
    }

    @Test
    public void testValidBinary() {
        STJunitUtils.getAbsolutePath(FrameworkUtil.getBundle(GprofBinaryTest.class).getSymbolicName(), path);
    }

}

