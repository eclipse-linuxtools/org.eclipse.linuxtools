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

import java.util.function.Consumer;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

/**
 * A utility class to build {@link Styler}
 */
public class StylerBuilder {

	// color fg bg
	// black 30 40
	// red 31 41
	// green 32 42
	// yellow 33 43
	// blue 34 44
	// purple 35 45
	// cyan 36 46
	// gray/white 37 47

	/**
	 * @return a {@link Styler} configured with a {@link SWT#COLOR_BLACK}
	 *         foreground color.
	 */
	public static Styler defaultStyler() {
		return styler(30);
	}

	/**
	 * Returns a {@link Styler} configured with a foreground color based on the
	 * given {@code}:
	 * <ul>
	 * <li>30: {@link SWT#COLOR_BLACK}</li>
	 * <li>31: {@link SWT#COLOR_DARK_RED}</li>
	 * <li>32: {@link SWT#COLOR_DARK_GREEN}</li>
	 * <li>33: {@link SWT#COLOR_DARK_YELLOW}</li>
	 * <li>34: {@link SWT#COLOR_DARK_BLUE}</li>
	 * <li>35: {@link SWT#COLOR_DARK_MAGENTA}</li>
	 * <li>36: {@link SWT#COLOR_DARK_CYAN}</li>
	 * <li>37: {@link SWT#COLOR_DARK_GRAY}</li>
	 * </ul>
	 * If the code is unknown, the {@link SWT#COLOR_BLACK} is used as the
	 * foreground color.
	 * 
	 * @param code
	 *            the code for the foreground color
	 * @return the configured {@link Styler}
	 */
	public static Styler styler(final int code) {
		switch (code) {
		case 31:
			return styler(textStyle -> textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
		case 32:
			return styler(
					textStyle -> textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
		case 33:
			return styler(
					textStyle -> textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW));
		case 34:
			return styler(textStyle -> textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));
		case 35:
			return styler(
					textStyle -> textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA));
		case 36:
			return styler(textStyle -> textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_CYAN));
		case 37:
			return styler(textStyle -> textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		case 30:
		default:
			return styler(textStyle -> textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		}

	}

	/**
	 * @return a {@link Styler} that sets the colors by the help of the given
	 *         {@code colorSetter} what the given
	 * @param colorSetter
	 *            the {@link Consumer} to call to set the colors.
	 */
	private static Styler styler(final Consumer<TextStyle> colorSetter) {
		return new Styler() {

			@Override
			public void applyStyles(final TextStyle textStyle) {
				colorSetter.accept(textStyle);
			}
		};
	}

}
