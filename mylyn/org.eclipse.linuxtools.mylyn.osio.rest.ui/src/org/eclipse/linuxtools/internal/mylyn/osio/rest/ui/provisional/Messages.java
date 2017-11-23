/*******************************************************************************
 * Copyright (c) 2016, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with Openshift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional.messages"; //$NON-NLS-1$

	public static String OSIORestQueryTypeWizardPage_ChooseQueryType;

	public static String OSIORestQueryTypeWizardPage_CreateQueryFromExistingURL;

	public static String OSIORestQueryTypeWizardPage_CreateQueryUsingForm;

	public static String OSIORestQueryTypeWizardPage_Query;

	public static String OSIORestQueryTypeWizardPage_SelectAvailableQueryTypes;

	public static String OSIORestUiUtil_CreateQueryFromForm;

	public static String OSIORestUiUtil_CreateQueryFromURL;

	public static String OSIORestUiUtil_EnterQueryParameter;

	public static String OSIORestUiUtil_EnterQueryParameters;

	public static String OSIORestUiUtil_enterTitleAndFillForm;

	public static String OSIORestUiUtil_EnterTitleAndFillForm;

	public static String OSIORestUiUtil_EnterTitleAndURL;

	public static String OSIORestUiUtil_EnterTitleAndURL1;

	public static String OSIORestUiUtil_fillForm;

	public static String OSIORestUiUtil_FillForm;
	
	public static String OSIORestSearchQueryPage_PropertiesForNewQuery;
	
	public static String OSIORestSearchQueryPage_PropertiesForQuery;
	
	public static String OSIORestSearchQueryPage_WorkitemTypeLabel;
	
	public static String OSIORestQuery_AssigneesLabel;
	
	public static String OSIORestQuery_SpecifyOneField;
	
	public static String OSIORestQuery_EnterValidURL;
	
	public static String OSIORestQuery_EnterValue;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
