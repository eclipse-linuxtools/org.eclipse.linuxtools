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

package org.eclipse.linuxtools.oprofile.core.opxml;

import org.eclipse.linuxtools.oprofile.core.model.OpModelImage;
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
	private static final String IMAGE_TAG = "image";
	private static final String SYMBOLS_TAG = "symbols";
	private static final String DEPENDENT_TAG = "dependent";
	
	//attribute tags
	private static final String ATTR_IMAGENAME = "name";
	private static final String ATTR_COUNT = "count";
	private static final String ATTR_DEPCOUNT = "count";
	
	//the current image being constructed
	private OpModelImage _image;

	//processors used for symbols and dependent images
	private SymbolsProcessor _symbolsProcessor = new SymbolsProcessor();
	private DependentProcessor _dependentProcessor = new DependentProcessor();
	
	
	public void reset(Object callData) {
		_image = ((CallData) callData).image;
	}

	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(IMAGE_TAG)) {
			_image._setName(attrs.getValue(ATTR_IMAGENAME));
			_image._setCount(Integer.parseInt(attrs.getValue(ATTR_COUNT)));
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
			//need to do something?
		} else if (name.equals(SYMBOLS_TAG)){
			_image._setSymbols(_symbolsProcessor.getSymbols());
		} else if (name.equals(DEPENDENT_TAG)){
			_image._setDependents(_dependentProcessor.getImages());
		} else {
			super.endElement(name, callData);
		}
		
//		if (name.equals(SAMPLE_TAG)) {
//			OpModelSample sample = _sampleProcessor.getSample();
//			_currentImage.addSample(sample);
//			CallData cdata = (CallData) callData;
//			if (cdata.monitor != null) {
//				cdata.monitor.worked(sample.getSampleCount());
//			}
//		} else if (name.equals(IMAGE_TAG)) {
//			if (_imageStack.isEmpty()) {
//				// Finished image -- add to session
//				_session.addSampleContainer(_currentImage);
//				_currentImage = null;
//			} else {
//				// Dependency -- add to image on stack
//				OpModelImage dep = _currentImage;
//				_currentImage = (OpModelImage) _imageStack.pop();
//				_currentImage.addSampleContainer(dep);
//			}
//		} else if (name.equals(SAMPLEFILE_TAG)) {
//			_currentImage.setSampleFile(_characters);
//		} else {
//			super.endElement(name, callData);
//		}
	}
}
