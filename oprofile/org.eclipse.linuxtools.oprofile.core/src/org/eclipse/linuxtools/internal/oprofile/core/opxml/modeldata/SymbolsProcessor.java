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

import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelSymbol;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor;
import org.xml.sax.Attributes;

/**
 * A processor for the <symbols> tag from `opxml samples ..`
 */
public class SymbolsProcessor extends XMLProcessor {
	//XML tags parsed by this processor
	private static final String SYMBOLS_TAG = "symbols"; //$NON-NLS-1$
	private static final String SYMBOL_TAG = "symbol";  //$NON-NLS-1$
	private static final String SAMPLE_TAG = "sample"; //$NON-NLS-1$

	//attribute tags
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_FILE = "file"; //$NON-NLS-1$
	private static final String ATTR_COUNT = "count";	 //$NON-NLS-1$
	private static final String ATTR_LINE = "line";	 //$NON-NLS-1$
	
	//the current symbol being constructed
	private OpModelSymbol _symbol;
	//all the symbols in this <symbols> tag, to be returned to the calling ModelDataProcessor
	private ArrayList<OpModelSymbol> _symbols;
	
	//the processor used for individual samples
	private SamplesProcessor _samplesProcessor = new SamplesProcessor();
	
	
	public void reset(Object callData) {
		_symbol = new OpModelSymbol();
		_symbols = new ArrayList<OpModelSymbol>();
	}

	/**
	 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#startElement(String, Attributes)
	 */
	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(SYMBOL_TAG)) {
			_symbol.setName(valid_string(attrs.getValue(ATTR_NAME)));
			_symbol.setCount(Integer.parseInt(attrs.getValue(ATTR_COUNT)));
			_symbol.setFilePath(valid_string(attrs.getValue(ATTR_FILE)));
			_symbol.setLine(Integer.parseInt(attrs.getValue(ATTR_LINE)));
		} else if (name.equals(SAMPLE_TAG)) {
			OprofileSAXHandler.getInstance(callData).push(_samplesProcessor);
		}
	}
	
	/**
	 * @see org.eclipse.linuxtools.internal.oprofile.core.XMLProcessor#endElement(String)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(SYMBOL_TAG)) {
			_symbol.setSamples(_samplesProcessor.getSamples());
			_symbols.add(_symbol);
			_symbol = new OpModelSymbol();
		} else if (name.equals(SYMBOLS_TAG)) {
			OprofileSAXHandler.getInstance(callData).pop(SYMBOLS_TAG);
		}
	}
	
	//return all parsed samples
	public OpModelSymbol[] getSymbols() {
		OpModelSymbol [] symbols = new OpModelSymbol[_symbols.size()];
		_symbols.toArray(symbols);
		return symbols;
	}
}
