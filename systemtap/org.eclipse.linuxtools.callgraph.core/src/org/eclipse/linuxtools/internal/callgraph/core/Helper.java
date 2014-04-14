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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.TextConsole;

public class Helper {

	/**
	 * @param name : A String that can be found in the console (BE AS SPECIFIC AS POSSIBLE)
	 * @return The TextConsole having 'name' somewhere within it's name
	 */
	public static TextConsole getConsoleByName(String name) {
		for (int i = 0; i < ConsolePlugin.getDefault().getConsoleManager()
				.getConsoles().length; i++) {
			if (ConsolePlugin.getDefault().getConsoleManager().
					getConsoles()[i].getName().contains(name)) {
				return (TextConsole)ConsolePlugin.getDefault().getConsoleManager().getConsoles()[i];
			}
		}
		return null;
	}

	/**
	 * Read the contents of a file
	 * @param absoluteFilePath : The absolute path of the file from which to read.
	 * @return : The contents of the file as a String.
	 */
	public static String readFile(String absoluteFilePath) {

		try (BufferedReader bw = new BufferedReader(new FileReader(new File(absoluteFilePath)))) {
			String output = ""; //$NON-NLS-1$
			String tmp = ""; //$NON-NLS-1$
			while ((tmp = bw.readLine()) != null) {
				output+=tmp + "\n"; //$NON-NLS-1$
			}
			bw.close();

			return output;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedWriter setBufferedWriter(String absoluteFilePath) {
		try {
			File f = new File(absoluteFilePath);
			f.delete();
			f.createNewFile();
			FileWriter fstream;
			fstream = new FileWriter(absoluteFilePath, true);
			return new BufferedWriter(fstream);
		} catch (IOException e) {
			SystemTapUIErrorMessages err = new SystemTapUIErrorMessages
					(Messages.getString("SystemTapView.FileIOErr"), //$NON-NLS-1$
					Messages.getString("SystemTapView.FileIOErr"), //$NON-NLS-1$
					Messages.getString("SystemTapView.FileIOErrMsg")); //$NON-NLS-1$
			err.schedule();
			e.printStackTrace();
			return null;
		}
	}

}
