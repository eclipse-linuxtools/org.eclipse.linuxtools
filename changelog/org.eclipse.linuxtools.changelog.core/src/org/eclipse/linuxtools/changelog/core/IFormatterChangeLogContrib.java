/*******************************************************************************
 * Copyright (c) 2006, 2018 Phil Muldoon <pkmuldoon@picobot.org>.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core;

import org.eclipse.ui.IEditorPart;

public interface IFormatterChangeLogContrib {

    String formatDateLine(String authorName, String authorEmail);
    String mergeChangelog(String dateLine, String functionGuess,String defaultContent,
            IEditorPart changelog, String changeLogLocation, String fileLocation);
}
