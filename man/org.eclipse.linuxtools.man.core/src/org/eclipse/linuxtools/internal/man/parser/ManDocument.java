/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.man.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;

/**
 * IDocument for a given man page.
 */
// TODO consider bold and underline symbols to handle ranges.
public class ManDocument extends Document {

	private static final String BOLD = "\b"; //$NON-NLS-1$
	private List<Integer> boldSymbols = new ArrayList<>();
	private List<Integer> underlineSymbols = new ArrayList<>();

	/**
	 * Creates an IDocument for the given man page and taking care for marking
	 * bold and underline symbols.
	 *
	 * @param manPage
	 *            The man page to create document for.
	 */
	public ManDocument(String manPage) {
		StringBuilder sb = new ManParser().getRawManPage(manPage);
		while (sb.indexOf(BOLD) != -1) {
			int index = sb.indexOf(BOLD);
			if (sb.charAt(index - 1) == '_') {
				sb.replace(index - 1, index + 2,
						sb.substring(index + 1, index + 2));
				underlineSymbols.add(index - 1);
			} else {
				sb.replace(index - 1, index + 1,
						sb.substring(index - 1, index - 1));
				boldSymbols.add(index - 1);
			}
		}
		set(sb.toString());

	}

	/**
	 * Returns the indexes of bold symbols.
	 *
	 * @return List of bold symbols.
	 */
	public List<Integer> getBoldSymbols() {
		return boldSymbols;
	}

	/**
	 * Returns the indexes of underline symbols.
	 *
	 * @return List of underline symbols.
	 */
	public List<Integer> getUnderlinedSymbols() {
		return underlineSymbols;
	}
}
