/*******************************************************************************
 * Copyright (c) 2010, 2018 STMicroelectronics and others.
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
package org.eclipse.linuxtools.internal.gprof.parser;

import java.io.DataInput;
import java.io.IOException;

public class HistogramDecoder64 extends HistogramDecoder {

    public HistogramDecoder64(GmonDecoder decoder) {
        super(decoder);
    }

    @Override
    protected long readAddress(DataInput stream) throws IOException {
        return stream.readLong();
    }


}
