/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

/**
 * XML handler class for opxml's "header".
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class HeaderProcessor extends MapProcessor {
	// XML tags recognized by this processor
	public static final String CPU_TYPE  = "cpu_type"; //$NON-NLS-1$
	public static final String EVENT = "event"; //$NON-NLS-1$
	public static final String COUNT = "count"; //$NON-NLS-1$
	public static final String UNIT_MASK = "unit-mask"; //$NON-NLS-1$
	public static final String CPU_SPEED = "cpu-speed"; //$NON-NLS-1$
	public static final String SEPARATE_LIBS = "separate-lib"; //$NON-NLS-1$
	public static final String SEPARATE_KERNEL = "separate-kernel"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#endElement(java.lang.String, java.lang.Object)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(SamplesProcessor.HEADER_TAG)) {
			// Done processing header -- pop ourselves from the processor stack
			OprofileSAXHandler.getInstance(callData).pop(name);
		} else {
			super.endElement(name, callData);
		}
	}
}
