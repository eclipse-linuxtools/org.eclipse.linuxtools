/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.opxml.modeldata;

import java.util.ArrayList;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor;
import org.xml.sax.Attributes;


/**
 * XML handler class for dependent images (<image> tags under <dependent>)
 */
public class DependentProcessor extends XMLProcessor {
    //XML tags parsed by this processor
    private static final String IMAGE_TAG = "image"; //$NON-NLS-1$
    private static final String SYMBOLS_TAG = "symbols"; //$NON-NLS-1$
    private static final String DEPENDENT_TAG = "dependent"; //$NON-NLS-1$

    //attribute tags
    private static final String ATTR_IMAGENAME = "name"; //$NON-NLS-1$
    private static final String ATTR_COUNT = "count"; //$NON-NLS-1$

    /**
     * the current image being constructed
     */
    private OpModelImage image;
    /**
     * a list of all the dependent images
     */
    private ArrayList<OpModelImage> imageList;
    /**
     * processor used for symbols of an image
     */
    private SymbolsProcessor _symbolsProcessor = new SymbolsProcessor();

    @Override
    public void reset(Object callData) {
        image = new OpModelImage();
        imageList = new ArrayList<>();
    }
    @Override
    public void startElement(String name, Attributes attrs, Object callData) {
        if (name.equals(IMAGE_TAG)) {
            image.setName(validString(attrs.getValue(ATTR_IMAGENAME)));
            image.setCount(Integer.parseInt(attrs.getValue(ATTR_COUNT)));
        } else if (name.equals(SYMBOLS_TAG)) {
            OprofileSAXHandler.getInstance(callData).push(_symbolsProcessor);
        }
    }
    @Override
    public void endElement(String name, Object callData) {
        if (name.equals(IMAGE_TAG)) {
            imageList.add(image);
            image = new OpModelImage();
        } else if (name.equals(SYMBOLS_TAG)) {
            image.setSymbols(_symbolsProcessor.getSymbols());
        } else if (name.equals(DEPENDENT_TAG)) {
            OprofileSAXHandler.getInstance(callData).pop(DEPENDENT_TAG);
        }
    }

    public OpModelImage[] getImages() {
        OpModelImage[] images = new OpModelImage[imageList.size()];
        imageList.toArray(images);
        return images;
    }

}
