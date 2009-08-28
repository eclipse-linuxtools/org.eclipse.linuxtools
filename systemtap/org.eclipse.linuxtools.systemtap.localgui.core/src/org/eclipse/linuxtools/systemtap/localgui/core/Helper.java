package org.eclipse.linuxtools.systemtap.localgui.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jface.text.IDocument;
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
	 * @param name : A String that can be found in the console (BE AS SPECIFIC AS POSSIBLE)
	 * @return The text contained within that console
	 */
	public static String getMainConsoleTextByName(String name){
		TextConsole proc = (TextConsole) getConsoleByName(name);
		return ((IDocument)proc.getDocument()).get();
	}
	
	public static IDocument getConsoleDocumentByName(String name) {
		return ((TextConsole)Helper.getConsoleByName(name)).getDocument();
	}
	
	/**
	 * @param absoluteFilePath : the absolute path to the file
	 * @param content : the text to be written
	 */
	public static void writeToFile(String absoluteFilePath, String content){
		try {
			FileWriter fstream;
			fstream = new FileWriter(absoluteFilePath);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content);
			out.close();
		} catch (Exception e) {
			SystemTapUIErrorMessages err = new SystemTapUIErrorMessages(Messages.getString("SystemTapView.31"), //$NON-NLS-1$ 
					Messages.getString("SystemTapView.32"), //$NON-NLS-1$ 
					Messages.getString("SystemTapView.33")); //$NON-NLS-1$
			err.schedule();
			e.printStackTrace();
		}
	}


	public static void appendToFile(String absoluteFilePath, String content) {
		try {
			FileWriter fstream;
			fstream = new FileWriter(absoluteFilePath, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.append(content);
			out.close();
		} catch (Exception e) {
			SystemTapUIErrorMessages err = new SystemTapUIErrorMessages(Messages.getString("SystemTapView.31"), //$NON-NLS-1$ 
					Messages.getString("SystemTapView.32"), //$NON-NLS-1$ 
					Messages.getString("SystemTapView.33")); //$NON-NLS-1$
			err.schedule();
			e.printStackTrace();
		}
	}

	public static String readFile(String absoluteFilePath) {
		
		try {
			String output = "";
			String tmp = "";
			BufferedReader bw = new BufferedReader(new FileReader(new File(absoluteFilePath)));
			while ((tmp = bw.readLine()) != null) {
				output+=tmp + "\n";
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
}
 