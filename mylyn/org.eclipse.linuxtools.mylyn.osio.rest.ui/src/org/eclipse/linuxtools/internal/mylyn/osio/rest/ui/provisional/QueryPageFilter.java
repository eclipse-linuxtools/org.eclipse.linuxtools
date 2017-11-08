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

import java.util.ArrayList;
import java.util.List;

public class QueryPageFilter {

	private final String key;

	private final List<String> values = new ArrayList<String>();

	public QueryPageFilter(String key, String value) {
		this.key = key;
		values.add(value);
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return values.get(0);
	}

	public List<String> getValues() {
		return values;
	}

	public void setValue(String value) {
		values.clear();
		values.add(value);
	}

	public void setValues(List<String> value) {
		values.clear();
		values.addAll(value);
	}

	public void addValue(String value) {
		values.add(value);
	}

}
