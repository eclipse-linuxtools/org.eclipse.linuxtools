/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import java.util.HashMap;

import org.eclipse.linuxtools.man.parser.ManPage;

public class ManpageCacher {
    private static final String SPLITTER = "::"; //$NON-NLS-1$
    private static final String NO_MAN_ENTRY = "No manual entry for"; //$NON-NLS-1$
    private static HashMap<String, String> pages = new HashMap<>();

    public static boolean isEmptyDocumentation(String documentation) {
        return documentation != null && documentation.startsWith(ManpageCacher.NO_MAN_ENTRY);
    }

    public static synchronized void clear() {
        pages.clear();
    }

    /**
     * Returns the documentation for the given probe, function, or tapset. Will never be <code>null</code>.
     */
    public static synchronized String getDocumentation(TapsetItemType prefix, String ...elements) {
        String fullElement = createFullElement(prefix, elements);
        String documentation = pages.get(fullElement);
        if (documentation == null) {
            // If the requested element is a probe variable,
            // fetch the documentation for the parent probe then check the map
            if (prefix == TapsetItemType.PROBEVAR) {
                getDocumentation(TapsetItemType.PROBE, elements[0]);
                documentation = pages.get(fullElement);
                if (documentation == null) {
                    documentation = ManpageCacher.NO_MAN_ENTRY + " " + fullElement; //$NON-NLS-1$
                    pages.put(fullElement, documentation);
                }
                return documentation;
            }

            // Otherwise, get the documentation for the requested element.
            documentation = (new ManPage(fullElement)).getStrippedTextPage().toString();
            pages.put(fullElement, documentation);

            // If the requested element is a probe and a documentation page was
            // found for it, parse the documentation for the variables if present.
            if (!isEmptyDocumentation(documentation) && prefix == TapsetItemType.PROBE) {
                getProbeVariableDocumentation(documentation, elements[0]);
            }
        }
        return documentation;
    }

    private static void getProbeVariableDocumentation(String documentation, String probe) {
        // Parse out the variables
        String[] sections = documentation.split("VALUES"); //$NON-NLS-1$
        if (sections.length > 1) {
            // Discard any other sections
            String variablesString = sections[1].split("CONTEXT|DESCRIPTION|SystemTap Tapset Reference")[0].trim(); //$NON-NLS-1$
            String[] variables = variablesString.split("\n"); //$NON-NLS-1$
            int i = 0;
            if (!variables[0].equals("None")) { //$NON-NLS-1$
                while (i < variables.length) {
                    String variableName = variables[i].trim();
                    StringBuilder variableDocumentation = new StringBuilder();
                    i++;
                    while (i < variables.length && !variables[i].isEmpty()) {
                        variableDocumentation.append(variables[i].trim());
                        variableDocumentation.append("\n"); //$NON-NLS-1$
                        i++;
                    }

                    pages.put(createFullElement(TapsetItemType.PROBEVAR, probe, variableName),
                            variableDocumentation.toString().trim());
                    i++;
                }
            }
        }
    }

    private static String createFullElement(TapsetItemType prefix, String ...elements) {
        String fullElement = prefix.toString();
        for (String element : elements) {
            fullElement += SPLITTER + element;
        }
        return fullElement;
    }
}
