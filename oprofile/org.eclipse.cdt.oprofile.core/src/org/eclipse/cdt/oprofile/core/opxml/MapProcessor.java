/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

import java.util.HashMap;

/**
 * A utility class which takes a simple list of tags, adds the tag name to a
 * hash map with the String value given by subsequent characters. The map can then
 * be fetched or queried for values defined by subclasses.
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz <keiths@redhat.com>
 */
public class MapProcessor extends XMLProcessor {
	protected HashMap _map = new HashMap();
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#reset()
	 */
	public void reset(Object callData) {
		_map.clear();
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#endElement(String)
	 */
	public void endElement(String name, Object callData) {
		_map.put(name, _characters);
	}
	
	/**
	 * Get the map associated with the MapProcessor.
	 * @return the map
	 */
	public HashMap getMap() {
		return _map;
	}
}
