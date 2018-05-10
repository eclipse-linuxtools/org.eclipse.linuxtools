/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
