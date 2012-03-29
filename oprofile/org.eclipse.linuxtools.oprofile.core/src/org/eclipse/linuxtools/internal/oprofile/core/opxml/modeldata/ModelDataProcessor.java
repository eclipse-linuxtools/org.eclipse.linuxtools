/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.internal.oprofile.core.opxml.modeldata;

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor;
import org.xml.sax.Attributes;


/**
 * A processor for `opxml samples`.
 */
public class ModelDataProcessor extends XMLProcessor {
	//The resulting image compiled by the processor to be used by the caller.
	public static class CallData {
		public OpModelImage image;
		public CallData(OpModelImage im) { image = im; }
	}

	//XML tags parsed by this processor
	private static final String IMAGE_TAG = "image"; //$NON-NLS-1$
	private static final String SYMBOLS_TAG = "symbols"; //$NON-NLS-1$
	private static final String DEPENDENT_TAG = "dependent"; //$NON-NLS-1$
	
	//attribute tags
	private static final String ATTR_IMAGENAME = "name"; //$NON-NLS-1$
	private static final String ATTR_COUNT = "count"; //$NON-NLS-1$
	private static final String ATTR_DEPCOUNT = "count"; //$NON-NLS-1$
	
	//the current image being constructed
	private OpModelImage _image;
	private int img_seen;	//for ensuring image singleton-ness

	//processors used for symbols and dependent images
	private SymbolsProcessor _symbolsProcessor = new SymbolsProcessor();
	private DependentProcessor _dependentProcessor = new DependentProcessor();
	
	
	public void reset(Object callData) {
		_image = ((CallData) callData).image;
		img_seen = 0;
	}

	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(IMAGE_TAG)) {
			if (img_seen == 0) {
				_image._setName(valid_string(attrs.getValue(ATTR_IMAGENAME)));
				_image._setCount(Integer.parseInt(attrs.getValue(ATTR_COUNT)));
			}

			img_seen++;
		} else if (name.equals(SYMBOLS_TAG)) {
			OprofileSAXHandler.getInstance(callData).push(_symbolsProcessor);
		} else if (name.equals(DEPENDENT_TAG)) {
			_image._setDepCount(Integer.parseInt(attrs.getValue(ATTR_DEPCOUNT)));
			OprofileSAXHandler.getInstance(callData).push(_dependentProcessor);
		} else {
			super.startElement(name, attrs, callData);
		}
	}
	
	public void endElement(String name, Object callData) {
		if (name.equals(IMAGE_TAG)) {
			if (img_seen > 1) {
				//should only ever be one image, otherwise oprofile was run
				// outside of eclipse and the ui would not handle it properly
				_image._setCount(OpModelImage.IMAGE_PARSE_ERROR);
				_image._setDepCount(0);
				_image._setDependents(null);
				_image._setSymbols(null);
				_image._setName(""); //$NON-NLS-1$				
			}
		} else if (name.equals(SYMBOLS_TAG)){
			_image._setSymbols(_symbolsProcessor.getSymbols());
		} else if (name.equals(DEPENDENT_TAG)){
			_image._setDependents(_dependentProcessor.getImages());
		} else {
			super.endElement(name, callData);
		}
	}
}
