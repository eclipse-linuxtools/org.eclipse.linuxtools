/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
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

/**
 * Cache class for event configuration. Currently only used to cache the results
 * of an `opxml check-event ..` run, since the isValid() method in the EventConfigTab
 * needlessly spawns opxml dozens of times with the same values. Note that multiple
 * checks of the same value will necessarily return the same result (for a given 
 * processor) so there is no worry of an invalid cache entry.
 */
public class EventConfigCache {
	//a cache entry for an event check, used as the hashmap key 
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

	/**
	 * Default constructor, creates an empty cache.
	 */
	public EventConfigCache() {
		validEventCache = new HashMap<CheckEventEntry, Boolean>();
	}

	/**
	 * Method to be used to check the validity of an event. Will check
	 * the cache for the result of a previous check of the same values,
	 * or otherwise will spawn opxml to check properly.
	 * @param counter counter number
	 * @param event event number
	 * @param mask unit mask value
	 * @return true or false, depending if the event config is valid
	 */
	public boolean checkEvent(int counter, int event, int mask) {
		CheckEventEntry e = new CheckEventEntry(counter, event, mask);
		Boolean result = null;
		
		result = validEventCache.get(e);
		
		if (result == null) {
			//not in the map, get its value and add it in
			result = Oprofile.checkEvent(counter, event, mask);
			
			//possible to be null if there is no opxmlProvider
			if (result != null) {
				validEventCache.put(e, result);
			}
		}

		return (result == null ? false : result);
	}
}
