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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AutotoolsProblemMarkerInfo extends ProblemMarkerInfo{
		
		public static enum Type{PACKAGE, HEADER, PROG, FILE, GENERIC}

		public AutotoolsProblemMarkerInfo(IResource file, String description, int severity, String name, Type type) {
			this(file, -1, description, severity, null, null, name, type);
		}

		public AutotoolsProblemMarkerInfo(IResource file, int lineNumber, String description, int severity, String variableName, Type type) {
			this(file, lineNumber, description, severity, variableName, null, null, type);
		}

		public AutotoolsProblemMarkerInfo(IResource file, int lineNumber, String description, int severity, String variableName, 
				IPath externalPath, String libraryInfo, Type type) {
			super(file, lineNumber, description, severity, variableName, externalPath);
			this.map = new HashMap<String, String>();

			mySetAttribute(IAutotoolsMarker.MARKER_PROBLEM_TYPE, type.name());
			mySetAttribute(IAutotoolsMarker.MARKER_LIBRARY_INFO, libraryInfo);
			
			this.mySetType (IAutotoolsMarker.AUTOTOOLS_PROBLEM_MARKER);
		}
		
		public String getProblemType() {
			return myGetAttribute(IAutotoolsMarker.MARKER_PROBLEM_TYPE);
		}

		public String getLibraryInfo(){
			return myGetAttribute(IAutotoolsMarker.MARKER_LIBRARY_INFO);
		}

		// The following code should be removed once the patch implementing these functions
		// in ProblemMarkerInfo is in a released version of CDT.
		String type;
		HashMap<String, String> map;

		public void mySetType(String type) {
			try {
				Method method = super.getClass().getMethod("setType", String.class);
				method.invoke(this, type);
			}catch (Exception e) {
				this.type = type;
			}
		}

		public void mySetAttribute (String key, String val){
			try {
				Method method = super.getClass().getMethod("setAttribute", String.class, String.class);
				method.invoke(this, key, val);
			}catch (Exception e) {
				this.map.put(key, val);
			}
		}

		public String myGetAttribute (String key){
			try {
				Method method = super.getClass().getMethod("getAttribute", String.class);
				return (String) method.invoke(this, key);
			}catch (Exception e) {
				return this.map.get(key);
			}
		}

		@SuppressWarnings("unchecked")
		public Map<String, String> myGetAttributes() {
			try {
				Method method = super.getClass().getMethod("getAttributes");
				return (Map<String, String>) method.invoke(this);
			}catch (Exception e) {
				return this.map;
			}
		}
}