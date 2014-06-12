/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.man.parser;

import org.eclipse.linuxtools.internal.man.parser.ManParser;

/**
 * Man page bean to ease fetching html-preformatted different parts of a man
 * page.
 */
public class ManPage {

    private StringBuilder rawContent;
    private StringBuilder strippedTextPage;

    /**
     * Creates the man page which includes retrieving the raw content and
     * changing format symbols to html.
     *
     * @param manPage
     *            The man page.
     */
    public ManPage(String manPage) {
        parse(new ManParser().getRawManPage(manPage));
    }

    /**
     * Creates the man page for an executable on the remote machine with the
     * specified credentials.
     *
     * @param manPage
     *            The man page.
     * @param user
     *            The name of the user to access the man page as.
     * @param host
     *            The name of host where the man page is to be fetched from.
     * @param password
     *            The user's login password.
     * @since 1.2
     */
    public ManPage(String manPage, String user, String host, String password) {
        parse(new ManParser()
                .getRemoteRawManPage(manPage, user, host, password));
    }

    // TODO make bold and underline be ranges instead of separate symbols.
    private void parse(StringBuilder rawManPage) {
        StringBuilder sb = new StringBuilder();
        sb.append(rawManPage);

        while (sb.indexOf("_\b") != -1) { //$NON-NLS-1$
            int index = sb.indexOf("_\b"); //$NON-NLS-1$
            sb.replace(index, index + 3,
                    "<u>" + sb.substring(index + 2, index + 3) + "</u>"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        while (sb.indexOf("\b") != -1) { //$NON-NLS-1$
            int index = sb.indexOf("\b"); //$NON-NLS-1$
            sb.replace(index - 1, index + 2,
                    "<b>" + sb.substring(index - 1, index) + "</b>"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        rawContent = sb;
    }

    /**
     * Returns html representation of the man page. The whole man page is kept
     * in one giant &lt;pre&gt; block with bold and underline symbols.
     *
     * @return The whole html man page.
     */
    public StringBuilder getHtmlPage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>").append(rawContent).append("</pre>"); //$NON-NLS-1$ //$NON-NLS-2$
        return sb;
    }

    /**
     * Returns stripped representation of the man page. Stripped parts are:
     * <ul>
     * <li>Header - all the parts before <b>NAME</b></li>
     * <li>Footer - all the parts from <b>AUTHOR</b> till the end</li>
     * </ul>
     *
     * @return The stripped html content of the man page.
     */
    public StringBuilder getStrippedHtmlPage() {
        StringBuilder sb = getStrippedPage();
        sb.insert(0, "<pre>"); //$NON-NLS-1$
        sb.append("</pre>"); //$NON-NLS-1$
        return sb;
    }

    /**
     * Returns stripped representation of the man page in the format it was
     * received from executing man. Stripped parts are:
     * <ul>
     * <li>Header - all the parts before <b>NAME</b></li>
     * <li>Footer - all the parts from <b>AUTHOR</b> till the end</li>
     * </ul>
     *
     * @return The stripped plain text content of the man page.
     * @since 1.1
     */
    public StringBuilder getStrippedPage() {
        StringBuilder sb = new StringBuilder();
        sb.append(rawContent);
        // The raw content may or may not be HTML
        if (sb.indexOf("<b>N</b>") != -1) { //$NON-NLS-1$
            sb.delete(0, sb.indexOf("<b>N</b>")); //$NON-NLS-1$
        } else if (sb.indexOf("NAME") != -1) { //$NON-NLS-1$
            sb.delete(0, sb.indexOf("NAME")); //$NON-NLS-1$
        }

        if (sb.indexOf("<b>A</b><b>U</b><b>T</b><b>H</b><b>O</b><b>R</b>") != -1) { //$NON-NLS-1$
            sb.delete(
                    sb.indexOf("<b>A</b><b>U</b><b>T</b><b>H</b><b>O</b><b>R</b>"), //$NON-NLS-1$
                    sb.length());
        } else if (sb.indexOf("AUTHOR") != -1) { //$NON-NLS-1$
            sb.delete(sb.indexOf("AUTHOR"), //$NON-NLS-1$
                    sb.length());
        }

        return sb;
    }

    /**
     * Removes all HTML markings are returns a text only version.
     *
     * @return a text only version of the manpage
     * @since 1.1
     */
    public StringBuilder getStrippedTextPage() {
        if (this.strippedTextPage == null) {
            this.strippedTextPage = getStrippedPage();
            int index = strippedTextPage.indexOf("<b>"); //$NON-NLS-1$
            while (index != -1) {
                strippedTextPage.replace(index, index + 3, ""); //$NON-NLS-1$
                strippedTextPage.replace(index + 1, index + 5, ""); //$NON-NLS-1$
                index = strippedTextPage.indexOf("<b>"); //$NON-NLS-1$
            }

            index = strippedTextPage.indexOf("<u>"); //$NON-NLS-1$
            while (index != -1) {
                strippedTextPage.replace(index, index + 3, ""); //$NON-NLS-1$
                strippedTextPage.replace(index + 1, index + 5, ""); //$NON-NLS-1$
                index = strippedTextPage.indexOf("<u>"); //$NON-NLS-1$
            }
        }

        return strippedTextPage;
    }
}
