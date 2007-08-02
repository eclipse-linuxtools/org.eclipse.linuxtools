/*******************************************************************************
 * Copyright (c) 2006 Phil Muldoon <pkmuldoon@picobot.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core;

import org.eclipse.ui.IEditorPart;

/**
 * @author pmuldoon (Phil Muldoon)
 */
public interface IFormatterChangeLogContrib {
	
	public String formatDateLine(String authorName, String authorEmail);
	public String mergeChangelog(String dateLine, String functionGuess,String defaultContent,
			IEditorPart changelog, String changeLogLocation, String fileLocation);
}
