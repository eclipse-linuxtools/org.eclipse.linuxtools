/*******************************************************************************
 * Copyright (c) 2008 Phil Muldoon <pkmuldoon@picobot.org>.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API and implementation. 
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * 
 * Build and hold completion metadata fo Systemtap. This originally is generated from stap coverage data
 * 
 *
 */

// TODO: Generate a strategy to determine when meta-data is older than what is currently available. Right now
// the generation of new meta-data is too slow to do this efficiently.
public class STPMetadataSingleton {


	private static STPMetadataSingleton instance = null;
	private static HashMap<String, ArrayList<String>> builtMetadata = new HashMap<String, ArrayList<String>>();

	private static boolean barLookups = false;
	
	// Not a true singleton, but enough for the simplistic purpose
	// it has to serve.
	protected STPMetadataSingleton() {
	}

	public static STPMetadataSingleton getInstance() {
		if (instance == null) {
			instance = new STPMetadataSingleton();
		}
		return instance;
	}

	/**
	 * Given the parameter return the completion proposals that best match the data. 
	 * 
	 * @param match - completion hint.
	 * 
	 * @return - completion proposals.
	 * 
	 */
	public static String[] getCompletionResults(String match) {
		
		ArrayList<String> data = new ArrayList<String>();
	
		// TODO: Until an error strategy is devised to better inform
		// the user that there was a problem compiling completions other than
		// a modal error dialog, or a log message use this.
		if (barLookups)
			return new String[] {"No completion data found."};
		
		// Check to see if the proposal hint included a <tapset>.<partialprobe>
		// or just a <probe>. (ie syscall. or syscall.re).
		boolean tapsetAndProbeIncluded = isTapsetAndProbe(match);

		// If the result is a tapset and partial probe, get the tapset, then 
		/// narrow down the list with partial probe matches.
		if (tapsetAndProbeIncluded) {
			ArrayList<String> temp = builtMetadata.get(getTapset(match));
			String probe = getTapsetProbe(match);
			for (int i=0; i<temp.size(); i++) {
				if (temp.get(i).startsWith(probe)) {
					data.add(temp.get(i));
				}
			}
		}
		// If the result was a <tapset>, return all <probe> matches.
		else
			data = builtMetadata.get(match);
		
		if (data == null)
			return new String[] {};
		else
			return data.toArray(new String[0]);
	}

	/**
	 * 
	 * From the file, read the metadata. The data follows the format of
	 * 
	 * <tapset>.<probe>(<parameter list>)
	 * 
	 * ie
	 * 
	 * tcp.disconnect(name:string,sock:long,flags:long)
	 * @param fileURL 

	 * @throws IOException 
	 * 
	 */
	private void readCompletionMetadata(URL fileURL) throws IOException {
		try {
			BufferedReader input = new BufferedReader(new FileReader(new File(fileURL.getFile())));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					String tapset = "";
					String probe = "";
					try {
						tapset = getTapset(line);
						probe = getTapsetProbe(line);
					} catch (Exception e) {
						continue;
					}
					ArrayList<String> data = builtMetadata.get(tapset);
					if (data == null)
						data = new ArrayList<String>();

					data.add(probe);
					builtMetadata.put(tapset, data);
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			throw ex;
		}
	}

	/** 
	 * Given data, decide whether it is comprised of a <tapset>.<probe>
	 * hint, or just a <tapset>.
	 * 
	 * @param data - hint data
	 * @return
	 */
	private static boolean isTapsetAndProbe(String data) {
		if (data.indexOf('.') >= 0)
			return true;
		
		return false;
	}

	/** 
	 * Given data, extract <tapset>
	 * 
	 * @param data - hint data
	 * @return
	 */
	private static String getTapset(String data) {
		int i = data.indexOf('.');
		if (i < 0)
			throw new StringIndexOutOfBoundsException();
		return data.substring(0, data.indexOf('.'));
	}

	/** 
	 * Given data, extract <probe>
	 * 
	 * @param data - hint data
	 * @return
	 */
	private static String getTapsetProbe(String data) {
		int i = data.indexOf('.');
		if (i < 0)
			throw new StringIndexOutOfBoundsException();
		return data.substring(data.indexOf('.') + 1, data.length());
	}

	
	/**
	 * 
	 * Decide whether cached metadata exists on disk.
	 * 
	 * @return - whether metadata exists.
	 */
	/*private boolean haveMetadata(String location) {
		File fileExists = new File(location);
		if ((fileExists.canRead()) && fileExists.exists())
			return true; 

		return false;
	}*/

	/**
	 * 
	 * Build the metadata from visiting the tapsets in turn and
	 * requesting coverage data from each one.
	 * 
	 * @throws FileNotFoundException 
	 * 
	 */
	/*private void buildCompletionMetadata(String location) throws FileNotFoundException {
		String[] tapsets = { "syscall", "signal", "netdev", "ioblock",
				"ioscheduler", "nd_syscall", "vm", "nfsd", "process", "sunrpc",
				"scheduler", "scsi", "socket", "tcp", "udp", "generic.fop" };
		ArrayList<StringBuffer> processedMetadata = new ArrayList<StringBuffer>();
		boolean openingBracket = false;

		// Execute each tapset, then convert the output from stdin
		// to a format more acceptable to completion.
		for (int i = 0; i < tapsets.length; i++) {
			StringBuffer[] data = executeSystemTap(tapsets[i]);
			for (int z = 0; z < data.length; z++) {
				openingBracket = false;
				for (int c = 0; c < data[z].length(); c++) {
					if (data[z].charAt(c) == ' ')
						if (openingBracket == false) {
							openingBracket = true;
							data[z].setCharAt(c, '(');
						} else {
							data[z].setCharAt(c, ',');
						}
				}
				data[z].append(')');
				processedMetadata.add(data[z]);
			}

		}

		// Output massaged data from stdout to a text file.
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(location));
		} catch (FileNotFoundException e) {
			throw e;
		}
		Iterator<StringBuffer> i = processedMetadata.iterator();
		while (i.hasNext()) {
			StringBuffer line = i.next();
			out.println(line.toString().trim());
		}
		out.close();
	}*/

	/**
	 * 
	 * Execute Systemtap binary, cpature stdout and return.
	 * 
	 * @param tapset to request coverage data from.
	 * @return
	 */
	
	// TODO: This could stand to be completely rewritten to be safer,
	// and be tolerant of faults. As it is we ship default meta-data
	// so this should never be executed in the user context. But eventually
	// an option will be made available to the user to regenerate the data.
	/*private StringBuffer[] executeSystemTap(String tapset) {

		ArrayList<StringBuffer> data = new ArrayList<StringBuffer>();

		try {
			String a;
			Process p = Runtime.getRuntime().exec(
					"stap -L " + tapset + ".*");
			BufferedReader in = new BufferedReader(new InputStreamReader(p
					.getInputStream()), 5000);
			while ((a = in.readLine()) != null) {
				data.add(new StringBuffer(a));
			}
			p.waitFor();
			in.close();
		} catch (IOException e) {
			return new StringBuffer[] {};
		}

		catch (java.lang.InterruptedException ie) {
			return new StringBuffer[] {};
		}

		return data.toArray(new StringBuffer[0]);
	}

	public void parse(ResourceBundle bundle) {
		System.out.println(System.getProperty("user.pwd"));

		System.out.println(System.getProperties());
	//	System.out.println(bundle.containsKey("syscall"));
	//	System.out.println(bundle.containsKey("syscall."));
		System.out.println(bundle.getKeys());
	}*/

	public void build(URL fileURL) {
		try {
//			if (!haveMetadata(location))
//				buildCompletionMetadata(location);
			readCompletionMetadata(fileURL);
		} catch (IOException e) {
			barLookups = true;
		}		
	}
}
