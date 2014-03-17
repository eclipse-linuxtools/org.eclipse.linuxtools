/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.preference;

import org.eclipse.jface.preference.PathEditor;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoConstants;
import org.eclipse.swt.widgets.Composite;

/**
 * Custom PathEditor to parse and store preferences the way
 * createrepo plugin does.
 */
public class CreaterepoPathEditor extends PathEditor {

	/**
	 * Default Constructor.
	 *
	 * @param name The name of the preference to save in.
	 * @param labelText The description label.
	 * @param dirChooserLabelText The label shown at the bottom of the directory dialog.
	 * @param parent The parent composite this PathEditor belongs to.
	 */
	public CreaterepoPathEditor(String name, String labelText, String dirChooserLabelText, Composite parent) {
		super(name, labelText, dirChooserLabelText, parent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PathEditor#createList(java.lang.String[])
	 */
	@Override
	protected String createList(String[] items) {
		String preferenceValue = ICreaterepoConstants.EMPTY_STRING;
		if (items.length > 0) {
			for (String str : items) {
				preferenceValue = preferenceValue.concat(str + ICreaterepoConstants.DELIMITER);
			}
			// remove hanging delimiter
			preferenceValue = preferenceValue.substring(0, preferenceValue.length()-1);
		}
		return preferenceValue;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PathEditor#parseString(java.lang.String)
	 */
	@Override
	protected String[] parseString(String stringList) {
		if (!stringList.isEmpty()) {
			return stringList.split(ICreaterepoConstants.DELIMITER);
		}
		return new String[]{};
	}

}
