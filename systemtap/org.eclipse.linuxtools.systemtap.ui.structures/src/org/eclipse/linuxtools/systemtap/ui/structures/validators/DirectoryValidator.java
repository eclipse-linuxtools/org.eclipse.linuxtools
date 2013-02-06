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

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.linuxtools.internal.systemtap.ui.structures.Localization;



public class DirectoryValidator implements IInputValidator {
	
	/**
	 * Determines whether or not the string is valid within the contraints.
	 * 
	 * @param s The string to check.
	 * 
	 * @return The return message.
	 */
	@Override
	public String isValid(String s) {
		if(null == s)
			return Localization.getString("DirectoryValidator.NotNull");
		if(s.length() < 1)
			return Localization.getString("DirectoryValidator.LongerFile");
		if(!s.endsWith("/"))
			return Localization.getString("DirectoryValidator.MustEndWith");
		if(s.contains("//"))
			return Localization.getString("DirectoryValidator.CanNotContain");
		return null;
	}
}
