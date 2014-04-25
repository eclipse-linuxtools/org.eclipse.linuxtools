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

/**
 * Single warning/error parsed from rpmlint output.
 */
public class RpmlintItem {

    private static final String[] SECTIONS = SpecfileParser.simpleSections;

    private int lineNbr;

    private int severity;

    private String id;

    private String refferedContent;

    private String message;

    private String fileName;

    /**
     * Returns the name of the file rpmlint gives warning for.
     *
     * @return The file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the name of the file rpmlint gives warning for.
     * @param file The File name.
     */
    public void setFileName(String file) {
        this.fileName = file;
    }

    /**
     * Returns the line number where the warning appears if rpmlint gives it.
     * @return The line number.
     */
    public int getLineNbr() {
        return lineNbr;
    }

    /**
     * Sets the line number where the warning appears if rpmlint gives it.
     * @param lineNbr The line number.
     */
    public void setLineNbr(int lineNbr) {
        this.lineNbr = lineNbr;
    }

    /**
     * The id of the warning.
     * @return The id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the warning.
     * @param id The id of the warning.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the message as rpmlint gives it.
     * @return The message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message as rpmlint gives it.
     * @param message The raw message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the referred content - section, tag, etc.
     * @return The referred content.
     */
    public String getRefferedContent() {
        return refferedContent;
    }

    /**
     * Sets the referred content.
     * @param refferedContent The referred content.
     */
    public void setRefferedContent(String refferedContent) {
        for (int i = 0; i < SECTIONS.length; i++) {
            if (refferedContent.startsWith(SECTIONS[i])) {
                this.refferedContent = refferedContent.trim();
                if (this.refferedContent.equals("")) {//$NON-NLS-1$
                    this.refferedContent = SECTIONS[i];
                }
                i = SECTIONS.length;
            } else {
                this.refferedContent = refferedContent;
            }
        }
    }

    /**
     * Returns the severity of the rpmlint item.
     *
     * @return The severity.
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Returns the severity of the rpmlint item.
     *
     * @param severity The severity of the rpmlint item.
     */
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

    /**
     * @see java.lang.Object#toString()
     */
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
