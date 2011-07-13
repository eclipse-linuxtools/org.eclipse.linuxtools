/*******************************************************************************
 * Copyright (c) 2006, 2010 Siemens AG.
 * All rights reserved. This content and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Norbert Ploett - Initial implementation
 * Red Hat Inc. - Modified for use with autotools plug-in
 *******************************************************************************/

package org.eclipse.linuxtools.internal.cdt.autotools.core;

import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AutotoolsProblemMarkerInfo {
		
		public static enum Type{PACKAGE, HEADER, PROG, FILE, GENERIC}

		public String libraryInfo;

		private ProblemMarkerInfo problemMarkerInfo;

		private Type type;

		public AutotoolsProblemMarkerInfo(IResource file, int lineNumber, String description, int severity, String variableName, Type type) {
			this(file, lineNumber, description, severity, variableName, null, null, type);
		}

		public ProblemMarkerInfo getProblemMarkerInfo() {
			return problemMarkerInfo;
		}
		
		public AutotoolsProblemMarkerInfo(IResource file, int lineNumber, String description, int severity, String variableName, 
				IPath externalPath, String libraryInfo, Type type) {
			problemMarkerInfo = new ProblemMarkerInfo(file, lineNumber, description, severity, variableName, externalPath);
			this.libraryInfo = libraryInfo;
			this.type = type;
		}
		
		public String getType() {
			return type.name();
		}

}