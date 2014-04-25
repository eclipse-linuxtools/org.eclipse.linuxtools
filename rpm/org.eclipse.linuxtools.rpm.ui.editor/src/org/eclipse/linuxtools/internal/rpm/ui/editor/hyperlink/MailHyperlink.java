/*******************************************************************************
 * Copyright (c) 2008 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.swt.program.Program;

/**
 * Mail hyperlink.
 */
public class MailHyperlink extends URLHyperlink {


    private String fURLString;


    public MailHyperlink(IRegion region, String urlString) {
        super(region, urlString);
        fURLString = urlString;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.hyperlink.URLHyperlink#open()
     */
    @Override
    public void open() {
        if (fURLString != null) {
            Program.launch(fURLString);
            fURLString= null;
            return;
        }
    }

}