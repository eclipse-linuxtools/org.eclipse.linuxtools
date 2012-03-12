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
package org.eclipse.linuxtools.internal.rpm.rpmlint.parser;

import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;

public class RpmlintItem {

	private static final String[] sections = SpecfileParser.simpleSections;

	private int lineNbr;

	private int severity;

	private String id;

	private String refferedContent;

	private String refferedSection;

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

	public String getRefferedContent() {
		return refferedContent;
	}

	public void setRefferedContent(String refferedContent) {
		for (int i = 0; i < sections.length; i++) {
			if (refferedContent.startsWith(sections[i])) {
				this.refferedContent = refferedContent.trim();
				if (this.refferedContent.equals("")) //$NON-NLS-1$
					this.refferedContent = sections[i];
				this.refferedSection = sections[i];
				i = sections.length;
			} else {
				this.refferedContent = refferedContent;
				this.refferedSection = ""; //$NON-NLS-1$
			}
		}
	}

	public String getrefferedSection() {
		return refferedSection;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		severity = severity.replaceAll(":", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
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
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("line number: "); //$NON-NLS-1$
		stringBuilder.append(this.lineNbr);
		stringBuilder.append("\nfile name: "); //$NON-NLS-1$
		stringBuilder.append(this.fileName);
		stringBuilder.append("\nseverity: "); //$NON-NLS-1$
		stringBuilder.append(this.severity);
		stringBuilder.append("\nId: "); //$NON-NLS-1$
		stringBuilder.append(this.id);
		stringBuilder.append("\nrefered content: "); //$NON-NLS-1$
		stringBuilder.append(this.refferedContent);
		stringBuilder.append("\nmessage: "); //$NON-NLS-1$
		stringBuilder.append(this.getMessage());
		stringBuilder.append("\n"); //$NON-NLS-1$
		return stringBuilder.toString(); 
	}
	
	
}
