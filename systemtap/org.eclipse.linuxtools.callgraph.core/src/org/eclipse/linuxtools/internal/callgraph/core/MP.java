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
package org.eclipse.linuxtools.internal.callgraph.core;

/**
 * Helper class for easily muted prints
 *
 */
public class MP {
	public static boolean mute = false;
	
	/**
	 * Prints the given string. All calls to MP.println can be muted by changing the mute variable
	 * in MP, either by going into the source code or calling MP.setMute(boolean)
	 * 
	 * @param String to print
	 */
	public static void println(String val) {
		if (!mute) System.out.println(val);
	}
	
	public static void print(String val){
		if (!mute) System.out.print(val);
	}
	
	public static void setMute(boolean val) {
		mute = val;
	}
	
	public static void mute() {
		mute = true;
	}
	
	public static void unmute() {
		mute = false;
	}
}
