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
package org.eclipse.linuxtools.rpm.ui.editor.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Before;

public abstract class AScannerTest extends FileTestCase {

    protected RuleBasedScanner rulesBasedScanner;

    protected abstract String getContents();

    protected abstract RuleBasedScanner getScanner();

    public SpecfileEditor editor;

    @Override
    @Before
    public void setUp() throws CoreException {
        super.setUp();
        newFile(getContents());
        testProject.refresh();
        IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage(), testFile,
                "org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");
        editor = (SpecfileEditor) openEditor;
        editor.doRevertToSaved();
        rulesBasedScanner = getScanner();
        rulesBasedScanner.setRange(testDocument, 0, getContents().length());
    }

    protected IToken getNextToken() {
        return rulesBasedScanner.nextToken();
    }

    protected IToken getToken(int nbrOfToken) {
        for (int i = 0; i < nbrOfToken - 1; i++) {
            rulesBasedScanner.nextToken();
        }
        return rulesBasedScanner.nextToken();
    }
}
