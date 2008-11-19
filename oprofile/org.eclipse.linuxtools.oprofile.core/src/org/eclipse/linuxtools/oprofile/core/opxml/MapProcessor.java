/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.core.opxml;

import java.util.HashMap;

/**
 * A utility class which takes a simple list of tags, adds the tag name to a
 * hash map with the String value given by subsequent characters. The map can then
 * be fetched or queried for values defined by subclasses.
 * @see org.eclipse.linuxtools.oprofile.core.opxml.OpxmlRunner
 */
public class MapProcessor extends XMLProcessor {
	protected HashMap<String,String> _map = new HashMap<String,String>();
	
	/**
	 * @see org.eclipse.linuxtools.oprofile.core.XMLProcessor#reset()
	 */
	public void reset(Object callData) {
		_map.clear();
	}
	
	/**
	 * @see org.eclipse.linuxtools.oprofile.core.XMLProcessor#endElement(String)
	 */
	public void endElement(String name, Object callData) {
		_map.put(name, _characters);
	}
	
	/**
	 * Get the map associated with the MapProcessor.
	 * @return the map
	 */
	public HashMap<String,String> getMap() {
		return _map;
	}
}
