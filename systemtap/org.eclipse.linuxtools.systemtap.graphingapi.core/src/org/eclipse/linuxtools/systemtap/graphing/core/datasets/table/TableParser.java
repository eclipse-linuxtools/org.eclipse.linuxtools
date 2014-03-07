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

package org.eclipse.linuxtools.systemtap.graphing.core.datasets.table;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSetParser;



public class TableParser implements IDataSetParser {
	public TableParser(String[] regEx, String delimiter) {
		this.regEx = Arrays.copyOf(regEx, regEx.length);
		this.delimiter = delimiter;
		buildPattern();
	}

	private void buildPattern() {
		StringBuilder wholeRegExpr = new StringBuilder();
		for(int i=0; i<regEx.length; i++) {
			wholeRegExpr.append('(' + regEx[i] + ')');
		}
		wholePattern = Pattern.compile(wholeRegExpr.toString());
		delimPattern = Pattern.compile(delimiter);
	}

	@Override
	public IDataEntry parse(StringBuilder s) {
		if(null == s) {
			return null;
		}

		TableEntry e = null;

		Matcher wholeMatcher = wholePattern.matcher(s);
		Matcher delimMatcher = delimPattern.matcher(s);

		Object[] data;
		int end = 0;
		if(delimMatcher.find()) {
			e = new TableEntry();
			end = delimMatcher.start();

			int group, j;
			while(wholeMatcher.find() && wholeMatcher.end() < end) {
				group = 0;
				data = new Object[regEx.length>>1];
				for(int i=0; i<regEx.length; i++) {
					group++;
					for(j=0; j<regEx[i].length(); j++) {
						if(regEx[i].charAt(j) == ')') {
							group++;
						}
					}

					if(0 == (i&1)) {
						data[i>>1] = wholeMatcher.group(group);
					}
				}
				e.add(data);
			}
			s.delete(0, delimMatcher.end());
		}

		return e;
	}

	private String[] regEx;
	private String delimiter;

	private Pattern wholePattern;
	private Pattern delimPattern;
}