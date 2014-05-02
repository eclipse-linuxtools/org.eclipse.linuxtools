/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *      - Note: the original SamplesProcessor class was removed, this is a new
 *        implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.opxml.modeldata;

import java.util.ArrayList;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSample;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor;


/**
 * XML handler class for <sample> tags (individual samples).
 */
public class SamplesProcessor extends XMLProcessor {
    //XML tags parsed by this processor
    private static final String SAMPLE_TAG = "sample"; //$NON-NLS-1$
    private static final String COUNT_TAG = "count"; //$NON-NLS-1$
    private static final String LINE_TAG = "line";  //$NON-NLS-1$
    private static final String SYMBOL_TAG = "symbol";  //$NON-NLS-1$
    private static final String FILE_TAG = "file";  //$NON-NLS-1$

    //the current sample being constructed
    private OpModelSample sample;
    //a list of all samples (for this symbol)
    private ArrayList<OpModelSample> sampleList;

    /**
     * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#reset()
     */
    @Override
    public void reset(Object callData) {
        sample = new OpModelSample();
        sampleList = new ArrayList<>();
    }

    /**
     * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#endElement(String)
     */
    @Override
    public void endElement(String name, Object callData) {
        if (name.equals(COUNT_TAG)) {
            sample.setCount(Integer.parseInt(characters));
        } else if (name.equals(LINE_TAG)) {
            sample.setLine(Integer.parseInt(characters));
        } else if (name.equals(FILE_TAG)) {
            sample.setFilePath(characters);
        } else if (name.equals(SAMPLE_TAG)) {
            sampleList.add(sample);
            sample = new OpModelSample();
        } else if (name.equals(SYMBOL_TAG)) {
            OprofileSAXHandler.getInstance(callData).pop(SYMBOL_TAG);
        }
    }

    /**
     * Return oprofile samples
     * @return samples An array of oprofile samples
     */
    public OpModelSample[] getSamples() {
        OpModelSample[] samples = new OpModelSample[sampleList.size()];
        sampleList.toArray(samples);
        return samples;
    }
}
