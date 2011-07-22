/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests.hyperlink;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ MailHyperlinkDetectorTest.class,
		SourcesFileHyperlinkDetectorTest.class,
		SpecfileElementHyperlinkDetectorTest.class,
		URLHyperlinkWithMacroDetectorTest.class })
public class HyperlinkAllTests {
}
