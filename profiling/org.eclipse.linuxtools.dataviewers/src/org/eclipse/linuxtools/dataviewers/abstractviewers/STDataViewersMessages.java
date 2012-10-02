/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.abstractviewers;

import org.eclipse.osgi.util.NLS;

public class STDataViewersMessages extends NLS {
	
	private static final String BUNDLE_NAME= "org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersMessages"; 

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, STDataViewersMessages.class);
	}

	public static String exportToCSVAction_title;
	
	public static String expandAllAction_title;
	public static String expandSelectionAction_title;
	public static String collapseAllAction_title;
	public static String collapseSelectionAction_title;
	public static String copyToAction_title;
	
	public static String hideshowDialog_title;
	public static String hideshowAction_title;
	
	public static String togglegraphsAction_title;
	
	public static String sortAction_title;
	
	public static String filtersAction_title;
	public static String filtersAction_tooltip;
	public static String filtersSubMenu_title;

	public static String sortAction_tooltip;
	
	public static String sortDialog_title;
	public static String sortDialog_label;
	public static String sortDialog_columnLabel;

	public static String sortDirectionAscending_text ;
	public static String sortDirectionAscending_text2;
	public static String sortDirectionAscending_text3;
	public static String sortDirectionAscending_text4;

	public static String sortDirectionDescending_text;
	public static String sortDirectionDescending_text2;
	public static String sortDirectionDescending_text3;
	public static String sortDirectionDescending_text4;

	public static String restoreDefaults_text;
	public static String selectAll_text;
	public static String deselectAll_text;
}