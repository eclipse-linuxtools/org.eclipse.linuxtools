/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.launch.configuration;

import java.util.HashMap;
import org.eclipse.linuxtools.oprofile.core.Oprofile;

public class EventConfigCache {
	class CheckEventEntry {
		public Integer counterNum, eventNum, maskValue;
		
		public CheckEventEntry(int counterNum, int eventNum, int maskValue) {
			this.counterNum = new Integer(counterNum);
			this.eventNum = new Integer(eventNum);
			this.maskValue = new Integer(maskValue);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CheckEventEntry) {
				CheckEventEntry other = (CheckEventEntry)obj;
				return (counterNum.equals(other.counterNum) && eventNum.equals(other.eventNum) && maskValue.equals(other.maskValue));
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			int prime = 11, result = 3;
			result = prime*result + counterNum.hashCode();
			result = prime*result + eventNum.hashCode();
			result = prime*result + maskValue.hashCode();
			return result;
		}
	}
	
	HashMap<CheckEventEntry, Boolean> validEventCache;
	
	public EventConfigCache() {
		validEventCache = new HashMap<CheckEventEntry, Boolean>();
	}
	
	public boolean checkEvent(int counter, int event, int mask) {
		CheckEventEntry e = new CheckEventEntry(counter, event, mask);
		Boolean result = null;
		
		result = validEventCache.get(e);
		
		if (result == null) {
			//not in the map, get its value and add it in
			result = Oprofile.checkEvent(counter, event, mask);
			validEventCache.put(e, result);
		}

		return result;
	}
}
