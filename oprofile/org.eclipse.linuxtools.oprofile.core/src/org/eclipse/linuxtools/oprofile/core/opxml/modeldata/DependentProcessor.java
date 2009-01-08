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
package org.eclipse.linuxtools.oprofile.core.opxml.modeldata;

import java.util.ArrayList;

import org.eclipse.linuxtools.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.oprofile.core.opxml.XMLProcessor;
import org.xml.sax.Attributes;


/**
 * XML handler class for dependent images (<image> tags under <dependent>)
 */
public class DependentProcessor extends XMLProcessor {
	//XML tags parsed by this processor
	private static final String IMAGE_TAG = "image";
	private static final String SYMBOLS_TAG = "symbols";
	private static final String DEPENDENT_TAG = "dependent";
	
	//attribute tags
	private static final String ATTR_IMAGENAME = "name";
	private static final String ATTR_COUNT = "count";

	//the current image being constructed
	private OpModelImage _image;
	//a list of all the dependent images
	private ArrayList<OpModelImage> _imageList;

	//processor used for symbols of an image
	private SymbolsProcessor _symbolsProcessor = new SymbolsProcessor();

	public void reset(Object callData) {
		_image = new OpModelImage();
		_imageList = new ArrayList<OpModelImage>();
	}

	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(IMAGE_TAG)) {
			_image._setName(attrs.getValue(ATTR_IMAGENAME));
			_image._setCount(Integer.parseInt(attrs.getValue(ATTR_COUNT)));
		} else if (name.equals(SYMBOLS_TAG)) {
			OprofileSAXHandler.getInstance(callData).push(_symbolsProcessor);
		} else {
			super.startElement(name, attrs, callData);
		}
	}
	/**
	 * @see org.eclipse.linuxtools.oprofile.core.XMLProcessor#endElement(String)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(IMAGE_TAG)) {
			_imageList.add(_image);
			_image = new OpModelImage();
		} else if (name.equals(SYMBOLS_TAG)) {
			_image._setSymbols(_symbolsProcessor.getSymbols());
		} else if (name.equals(DEPENDENT_TAG)) {
			OprofileSAXHandler.getInstance(callData).pop(DEPENDENT_TAG);
		} else {
			super.endElement(name, callData);
		}
	}
	
	public OpModelImage[] getImages() {
		OpModelImage[] images = new OpModelImage[_imageList.size()];
		_imageList.toArray(images);
		return images;
	}

}
