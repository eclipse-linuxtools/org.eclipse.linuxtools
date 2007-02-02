/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

import org.eclipse.cdt.oprofile.core.ProfileImage;
import org.eclipse.cdt.oprofile.core.Sample;
import org.xml.sax.Attributes;


/**
 * XML handler class for samples.
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz <keiths@redhat.com>
 */
public class SampleProcessor extends XMLProcessor {
	
	// An XML processor to handle symbol information output by the request
	private class SymbolProcessor extends XMLProcessor {
		// XML tags recognized by this processor
		private static final String _NAME_TAG ="name"; //$NON-NLS-1$
		
		// The Symbol being constructed by this processor
		private Sample.Symbol _symbol;
		
		/**
		 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#reset()
		 */
		public void reset(Object callData) {
			_symbol = new Sample.Symbol();
		}
		
		/**
		 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#endElement(String)
		 */
		public void endElement(String name, Object callData) {
			if (name.equals(_NAME_TAG)) {
				// Set symbol's name
				_symbol.name = _characters;
			} else if (name.equals(_ADDR_TAG)) {
				// Set symbol's start address
				_symbol.startAddress = _characters;
			} else if (name.equals(_SYMBOL_TAG)) {
				// Done. Pop us off the top, passing _SYMBOL_TAG to SampleProcessor
				OprofileSAXHandler.getInstance(callData).pop(_SYMBOL_TAG);
			}
		}
		
		/**
		 * Returns the <code>Symbol</code> constructed from the request.
		 * @return the symbol information
		 */
		public Sample.Symbol getResult() {
			return _symbol;
		}
	};
	
	// XML tags recognized by this processor
	private static final String _SAMPLE_TAG = "sample"; //$NON-NLS-1$
	private static final String _ADDR_TAG = "addr"; //$NON-NLS-1$
	private static final String _COUNT_TAG = "count"; //$NON-NLS-1$
	private static final String _SYMBOL_TAG = "symbol"; //$NON-NLS-1$

	// An XML processor for symbol information
	private SymbolProcessor _symbolProcessor = new SymbolProcessor();
	
	// The current sample being constructed by the parser
	private Sample _sample;
	
	// The ProfileImage to use when creating new Samples
	private ProfileImage _sampleFile;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#reset(java.lang.Object)
	 */
	public void reset(Object callData) {
		_sample = new Sample(_sampleFile);
	}

	/**
	 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#startElement(String, Attributes)
	 */
	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(_SYMBOL_TAG)) {
			// Symbol -- push symbol processor
			OprofileSAXHandler.getInstance(callData).push(_symbolProcessor);
		} else {
			super.startElement(name, attrs, callData);
		}
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#endElement(String)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(_ADDR_TAG)) {
			// Set sample's address
			_sample.setAddress(_characters);
		} else if (name.equals(_COUNT_TAG)) {
			// Set sample's count
			_sample.setCount(Integer.parseInt(_characters));
		} else if (name.equals(_SYMBOL_TAG)) {
			// Done with symbol -- set it in Sample
			_sample.setSymbol(_symbolProcessor.getResult());
		} else if (name.equals(_SAMPLE_TAG)) {
			OprofileSAXHandler.getInstance(callData).pop(_SAMPLE_TAG);
		} else {
			super.endElement(name, callData);
		}
	}
	
	/**
	 * Returns the result of processing the sample record
	 * @return the sample
	 */
	public Sample getSample() {
		return _sample;
	}
	
	/**
	 * Set the ProfileImage to be used when creating new Samples.
	 * @param sfile the sample file
	 */
	public void setImageFile(ProfileImage sfile) {
		_sampleFile = sfile;
	}
}
