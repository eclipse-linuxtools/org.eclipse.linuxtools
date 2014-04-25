/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Andrew Ferrazzutti
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.core.datasets.row;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSetParser;

/**
 * A DataSetParser for parsing a string, line-by-line, with a preconstructed
 * single-line regex string.
 *
 * @author aferrazz
 * @since 1.1
 *
 */
public class LineParser implements IDataSetParser {
    public LineParser(String regEx) {
        wholePattern = Pattern.compile(regEx, Pattern.MULTILINE);
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
            int groupCount = wholeMatcher.groupCount();
            Object[] data = new Object[groupCount];

            for(int i = 0; i < groupCount; i++) {
                data[i] = wholeMatcher.group(i+1);
            }
            e.putRow(0, data);
            s.delete(0, s.length());
        }

        return e;
    }

    private Pattern wholePattern;
}
