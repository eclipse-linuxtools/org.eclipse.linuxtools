/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

import java.util.ArrayList;

import org.eclipse.cdt.oprofile.core.Sample;
import org.xml.sax.Attributes;


/**
 * XML handler class for opxml's "debug-info".
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz <keiths@redhat.com>
 */
public class DebugInfoProcessor extends XMLProcessor {

	private Sample.DebugInfo _info;
	
	// XML tags recognized by this processor
	public static final String ADDRESS_TAG = "address"; //$NON-NLS-1$
	private static final String _FUNCTION_ATTR = "function"; //$NON-NLS-1$
	private static final String _SOURCEFILE_ATTR = "source-filename"; //$NON-NLS-1$
	private static final String _LINE_ATTR = "line"; //$NON-NLS-1$
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#startElement(java.lang.String, org.xml.sax.Attributes, java.lang.Object)
	 */
	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(ADDRESS_TAG)) {
			// New debug info
			_info = new Sample.DebugInfo();
			String value = attrs.getValue(_FUNCTION_ATTR);
			if (value != null) {
				_info.function = value;
			}
			value = attrs.getValue(_SOURCEFILE_ATTR);
			if (value != null) {
				_info.sourceFilename = value;
			}
			value = attrs.getValue(_LINE_ATTR);
			if (value != null) {
				_info.lineNumber = Integer.parseInt(value);
			}
		}
		
		super.startElement(name, attrs, callData);
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#endElement(String)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(ADDRESS_TAG)) {
			_info.address = _characters;
			ArrayList list = (ArrayList) callData;
			list.add(_info);
		}
		
		super.endElement(name, callData);
	}
}
