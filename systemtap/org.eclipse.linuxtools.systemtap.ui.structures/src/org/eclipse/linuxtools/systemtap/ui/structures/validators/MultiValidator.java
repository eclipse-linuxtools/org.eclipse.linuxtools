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

package org.eclipse.linuxtools.systemtap.ui.structures.validators;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IInputValidator;

public class MultiValidator implements IInputValidator {
	public void addValidator(IInputValidator validator) {
		validators.add(validator);
	}

	
	/**
	 * Determines whether or not the string is valid within the contraints.
	 * 
	 * @param s The string to check.
	 * 
	 * @return The return message.
	 */
	public String isValid(String s) {
		String message = null;
		for(int i=0; i<validators.size(); i++) {
			message = validators.get(i).isValid(s);
			if(null != message) 
				return message;
		}
		return null;
	}
	
	ArrayList<IInputValidator> validators = new ArrayList<IInputValidator>();
}
