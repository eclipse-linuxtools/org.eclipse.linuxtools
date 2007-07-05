/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecfileElement {
	private Specfile specfile;
	private String name;
	private int lineNumber;
	private int lineStartPosition;
	private int lineEndPosition;
	
	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public SpecfileElement(){
		//weird
	}
	
	public SpecfileElement(String name) {
		setName(name);
	}

	public String getName() {
		return resolve(name);
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

	public int getLineEndPosition() {
		return lineEndPosition;
	}

	public void setLineEndPosition(int lineEndPosition) {
		this.lineEndPosition = lineEndPosition;
	}

	public int getLineStartPosition() {
		return lineStartPosition;
	}

	public void setLineStartPosition(int lineStartPosition) {
		this.lineStartPosition = lineStartPosition;
	}

	public Specfile getSpecfile() {
		return specfile;
	}

	public void setSpecfile(Specfile specfile) {
		this.specfile = specfile;
	}
	
	public String resolve(String toResolve) {
		if (specfile == null) {
			return toResolve;
		}
		String resolved = toResolve;
		
		Pattern variablePattern = Pattern.compile("%\\{(\\S+?)\\}");
		Matcher variableMatcher = variablePattern.matcher(toResolve);
		while (variableMatcher.find()) {
			SpecfileDefine define = specfile
					.getDefine(variableMatcher.group(1));
			if (define != null) {
				resolved = resolved.replaceAll("%\\{"
						+ variableMatcher.group(1) + "\\}", define
						.getStringValue());
			}
		}
		
		return resolved;
	}

}
