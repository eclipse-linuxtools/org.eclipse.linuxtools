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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.OSIORestUIPlugin;
import org.eclipse.mylyn.commons.core.StatusHandler;

@SuppressWarnings("nls")
public class QueryPageSearch {

	/** Stores search criteria in the order entered by the user. */
	private final Map<String, QueryPageFilter> filterByFieldName = new LinkedHashMap<String, QueryPageFilter>();

	public QueryPageSearch() {
	}

	public QueryPageSearch(String queryParameter) {
		fromUrl(queryParameter);
	}

	public void addFilter(String key, String value) {
		QueryPageFilter filter = filterByFieldName.get(key);
		if (filter == null) {
			filter = new QueryPageFilter(key, value);
			filterByFieldName.put(key, filter);
		}
	}

	public void addFilter(QueryPageFilter filter) {
		filterByFieldName.put(filter.getKey(), filter);
	}

	public List<QueryPageFilter> getFilters() {
		return new ArrayList<QueryPageFilter>(filterByFieldName.values());
	}

	public QueryPageFilter getFilter(String key) {
		return filterByFieldName.get(key);
	}

	public void fromUrl(String url) {
		int idx = url.indexOf('?');
		StringTokenizer t = new StringTokenizer(idx != -1 ? url.substring(idx + 1) : url, "&"); //$NON-NLS-1$
		while (t.hasMoreTokens()) {
			String token = t.nextToken();
			int i = token.indexOf("="); //$NON-NLS-1$
			if (i != -1) {
				try {
					String key = URLDecoder.decode(token.substring(0, i), "UTF-8");
					String value = URLDecoder.decode(token.substring(i + 1), "UTF-8");
					QueryPageFilter filter = filterByFieldName.get(key);
					if (filter == null) {
						addFilter(key, value);
					} else {
						filter.addValue(value);
					}
				} catch (UnsupportedEncodingException e) {
					StatusHandler.log(new Status(IStatus.WARNING, OSIORestUIPlugin.PLUGIN_ID,
							"Unexpected exception while decoding URL", e)); //$NON-NLS-1$
				}
			}
		}
	}

	public String toQuery() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (QueryPageFilter filter : filterByFieldName.values()) {
			for (String actualValue : filter.getValues()) {
				String encodedValue = null;
				try {
					encodedValue = URLEncoder.encode(actualValue, "UTF-8");
					if (count++ > 0) {
						sb.append("&");
					}
					sb.append(filter.getKey());
					sb.append("=");
					sb.append(encodedValue);
				} catch (UnsupportedEncodingException e) {
					StatusHandler.log(new Status(IStatus.WARNING, OSIORestUIPlugin.PLUGIN_ID,
							"Unexpected exception while encoding URL", e)); //$NON-NLS-1$
				}
			}
		}
		return sb.toString();
	}

}
