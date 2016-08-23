/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.consoles;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.custom.StyledText;

/**
 * A builder for {@link StyledText}
 */
public class StyledTextBuilder {

	/** The 'ESC' character that prefixes ANSI code sequences. */
	public static final char ESC = (char) 27;

	/**
	 * Parses the given {@code lineText} and returns a {@link StyledString} with
	 * configured foreground color on specific portions.
	 *
	 * @param lineText
	 *            the line of text to parse
	 * @return the corresponding {@link StyledString}
	 * @see <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape
	 *      codes</a>
	 */
	public static StyledString parse(final String lineText) {
		final StyledString result = new StyledString();
		// looking for blocks of characters starting with 'ESC[xxm' (where 'xx'
		// is
		// a color code)
		// and optionally ending with 'ESC[0m'
		final Pattern colorSequencePattern = Pattern
				.compile("\\x1b\\[(?<colorcode>\\d{1,2})m(?<content>.*)"); //$NON-NLS-1$
		final Matcher colorSequenceMatcher = colorSequencePattern
				.matcher(lineText);
		int lastColorSequencePosition = 0;
		int lastColorSequenceCode = 0; // default color (Black)
		while (colorSequenceMatcher.find(lastColorSequencePosition)) {
			final int currentColorSequenceStartPosition = colorSequenceMatcher
					.start();
			final String colorCode = colorSequenceMatcher.group("colorcode");
			// catch-up with initial in-between content that has no specific
			// color code boundaries or with the last color sequence position
			if (lastColorSequencePosition < currentColorSequenceStartPosition) {
				final String inbetweenContent = lineText.substring(
						lastColorSequencePosition,
						currentColorSequenceStartPosition);
				if (!inbetweenContent.isEmpty()) {
					result.append(inbetweenContent,
							StylerBuilder.styler(lastColorSequenceCode));
				}
			}
			lastColorSequencePosition = currentColorSequenceStartPosition
					// counting the 'ESC' char too
					+ (ESC + "[" + colorCode + "m").length();
			lastColorSequenceCode = Integer.parseInt(colorCode);
		}
		// catch-up with remaining content that has no specific color code
		// boundaries
		if (lastColorSequencePosition < lineText.length()) {
			final String lastSequence = lineText
					.substring(lastColorSequencePosition);
			result.append(new StyledString(lastSequence,
					StylerBuilder.styler(lastColorSequenceCode)));
		}
		return result;
	}

}
