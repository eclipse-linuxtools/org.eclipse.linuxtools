/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.rpmlint.parser;

import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;

public class RpmlintItem {

	private static final String[] sections = SpecfileParser.simpleSections;

	private int lineNbr;

	private int severity;

	private String id;

	private String referedContent;

	private String referedSection;

	private String message;
	
	private String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String file) {
		this.fileName = file;
	}

	public int getLineNbr() {
		return lineNbr;
	}

	public void setLineNbr(int lineNbr) {
		this.lineNbr = lineNbr;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getReferedContent() {
		return referedContent;
	}

	public void setReferedContent(String referedContent) {
		for (int i = 0; i < sections.length; i++) {
			if (referedContent.startsWith(sections[i])) {
				this.referedContent = referedContent.trim();
				if (this.referedContent.equals(""))
					this.referedContent = sections[i];
				this.referedSection = sections[i];
				i = sections.length;
			} else {
				this.referedContent = referedContent;
				this.referedSection = "";
			}
		}
	}

	public String getReferedSection() {
		return referedSection;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		severity = severity.replaceAll(":", "").trim();
		switch (severity.charAt(0)) {
		case 'I':
			this.severity = 0;
			break;
		case 'W':
			this.severity = 1;
			break;
		case 'E':
			this.severity = 2;
			break;
		default:
			this.severity = 0;
			break;
		}
	}

	@Override
	public String toString() {
		return "line number: " + this.lineNbr 
			+ "\nfile name: " + this.fileName
			+ "\nseverity: " + this.severity
			+ "\nId: " + this.id 
			+ "\nrefered content: "	+ this.referedContent 
			+ "\nmessage: " + this.getMessage()
			+ "\n";
	}
	
	
}
