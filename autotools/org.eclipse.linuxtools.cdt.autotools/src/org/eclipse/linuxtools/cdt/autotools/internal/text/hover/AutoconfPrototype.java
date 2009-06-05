/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.internal.text.hover;

import java.util.ArrayList;

public class AutoconfPrototype {
	protected String name;
	protected int numPrototypes;
	protected ArrayList minParms;
	protected ArrayList maxParms;
	protected ArrayList parmList;
	
	public AutoconfPrototype() {
		numPrototypes = 0;
		minParms = new ArrayList();
		maxParms = new ArrayList();
		parmList = new ArrayList();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String newName) {
		name = newName;
	}
	
	public int getNumPrototypes() {
		return numPrototypes;
	}
	
	public void setNumPrototypes(int num) {
		numPrototypes = num;
	}
	
	public int getMinParms(int prototypeNum) {
		return ((Integer)minParms.get(prototypeNum)).intValue();
	}
	
	public void setMinParms(int prototypeNum, int value) {
		minParms.add(prototypeNum, new Integer(value));
	}
	
	public int getMaxParms(int prototypeNum) {
		return ((Integer)maxParms.get(prototypeNum)).intValue();
	}
	
	public void setMaxParms(int prototypeNum, int value) {
		maxParms.add(prototypeNum, new Integer(value));
	}
	
	public String getParmName(int prototypeNum, int parmNum) {
		ArrayList parms = (ArrayList)parmList.get(prototypeNum);
		return (String)parms.get(parmNum);
	}

	// This function assumes that parms will be added in order starting
	// with lowest prototype first.
	public void setParmName(int prototypeNum, int parmNum, String value) {
		ArrayList parms;
		if (parmList.size() == prototypeNum) {
			parms = new ArrayList();
			parmList.add(parms);
		}
		else
			parms = (ArrayList)parmList.get(prototypeNum);
		if (parms.size() == parmNum)
			parms.add(value);	
		else
			parms.set(parmNum, value);
	}
	
}
