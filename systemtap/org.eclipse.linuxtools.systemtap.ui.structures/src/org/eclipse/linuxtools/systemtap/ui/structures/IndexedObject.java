/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.structures;

public class IndexedObject implements Comparable{
	public int index;
	public Object val;
	
	public IndexedObject(int i, Object v) {
		index = i;
		val = v;
	}

	public String toString() {
		return val.toString();
	}
	
	public int compareTo(Object o) {
		if(o instanceof IndexedObject) {
			Comparable thisVal = (val instanceof Comparable) ? (Comparable)val : val.toString();
			IndexedObject t = (IndexedObject)o;
			Comparable otherVal = (t.val instanceof Comparable) ? (Comparable)t.val : t.val.toString();
			return thisVal.compareTo(otherVal);
		}
		return 0;
	}
}
