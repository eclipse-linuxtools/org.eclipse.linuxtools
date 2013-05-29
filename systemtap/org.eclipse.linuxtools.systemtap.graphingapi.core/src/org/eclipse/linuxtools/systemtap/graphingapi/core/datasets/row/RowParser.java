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

package org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;



public class RowParser implements IDataSetParser {
	public RowParser(String[] regEx) {
		this.regEx = Arrays.copyOf(regEx, regEx.length);
		buildPattern();
	}

	private void buildPattern() {
		StringBuilder wholeRegExpr = new StringBuilder();
		for(int i=0; i<regEx.length; i++) {
			wholeRegExpr.append('(' + regEx[i] + ')');
		}
		wholePattern = Pattern.compile(wholeRegExpr.toString());
	}

	@Override
	public IDataEntry parse(StringBuilder s) {
		if(null == s) {
			return null;
		}

		RowEntry e = null;
		Matcher wholeMatcher = wholePattern.matcher(s);

		if(wholeMatcher.find()) {
			e = new RowEntry();
			Object[] data = new Object[regEx.length>>1];

			int group=0, j;

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
			e.putRow(0, data);
			s.delete(0, wholeMatcher.end());
		}

		return e;
	}

	private String[] regEx;
	private Pattern wholePattern;
}
