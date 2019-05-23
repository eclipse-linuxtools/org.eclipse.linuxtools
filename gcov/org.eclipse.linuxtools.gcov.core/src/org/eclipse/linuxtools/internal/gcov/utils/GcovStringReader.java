/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.utils;

import java.io.DataInput;
import java.io.IOException;

public class GcovStringReader {

    public static String readString(DataInput stream) throws IOException {
        String res = Messages.GcovStringReader_null_string;
        long length = stream.readInt() & MasksGenerator.UNSIGNED_INT_MASK;
        if (length != 0) {
            int ln = ((int) length) << 2;
            byte[] name = new byte[ln];
            stream.readFully(name);
			StringBuilder sb = new StringBuilder();
            for (int j = 0; j < ln; j++) {
                if (name[j] != 0) {
                    char c = (char) name[j];
                    sb.append(c);
                }
            }
            res = sb.toString();
        }
        return res;
    }

}
