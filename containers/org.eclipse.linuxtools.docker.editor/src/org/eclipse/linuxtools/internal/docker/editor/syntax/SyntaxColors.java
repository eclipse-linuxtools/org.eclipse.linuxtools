/*******************************************************************************
 * Copyright (c) 2015-2016 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor.syntax;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

public class SyntaxColors {

	private static final String COLOR_DEF_INSTRUCTION = "org.eclipse.linuxtools.docker.editor.color.instruction";
	private static final String COLOR_DEF_STRING = "org.eclipse.linuxtools.docker.editor.color.string";
	private static final String COLOR_DEF_COMMENT = "org.eclipse.linuxtools.docker.editor.color.comment";
	private static final String COLOR_DEF_VARIABLE = "org.eclipse.linuxtools.docker.editor.color.variable";
	
	public static Color getInstructionColor() {
		return getTheme().getColorRegistry().get(COLOR_DEF_INSTRUCTION);
	}
	
	public static Color getStringColor() {
		return getTheme().getColorRegistry().get(COLOR_DEF_STRING);
	}

	public static Color getCommentColor() {
		return getTheme().getColorRegistry().get(COLOR_DEF_COMMENT);
	}
	
	public static Color getVariableColor() {
		return getTheme().getColorRegistry().get(COLOR_DEF_VARIABLE);
	}
	
	private static ITheme getTheme() {
		return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
	}
}
