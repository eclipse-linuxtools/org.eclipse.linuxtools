/*******************************************************************************
 * Copyright (c) 2015 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

public class QueryPageDetails {
	private final boolean needsTitle;

	private final String queryUrlPart;

	private final String pageTitle;

	private final String pageDescription;

	private final String urlPattern;

	private final String queryAttributeName;

	public QueryPageDetails(boolean needsTitle, String queryUrlPart, String pageTitle, String pageDescription,
			String urlPattern, String queryAttributeName) {
		super();
		this.needsTitle = needsTitle;
		this.queryUrlPart = queryUrlPart;
		this.pageTitle = pageTitle;
		this.pageDescription = pageDescription;
		this.urlPattern = urlPattern;
		this.queryAttributeName = queryAttributeName;
	}

	public boolean needsTitle() {
		return needsTitle;
	}

	public String getQueryUrlPart() {
		return queryUrlPart;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public String getPageDescription() {
		return pageDescription;
	}

	public String getUrlPattern() {
		return urlPattern;
	}

	public String getQueryAttributeName() {
		return queryAttributeName;
	}

}
