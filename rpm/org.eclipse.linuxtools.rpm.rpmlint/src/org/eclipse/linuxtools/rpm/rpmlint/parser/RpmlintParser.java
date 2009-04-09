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
package org.eclipse.linuxtools.rpm.rpmlint.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.rpmlint.Activator;
import org.eclipse.linuxtools.rpm.rpmlint.RpmlintLog;
import org.eclipse.linuxtools.rpm.rpmlint.builder.RpmlintBuilder;
import org.eclipse.linuxtools.rpm.rpmlint.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.rpmlint.resolutions.RpmlintMarkerResolutionGenerator;
import org.eclipse.linuxtools.rpm.ui.editor.Utils;

public class RpmlintParser {
	
	
	private static final String COLON = ":"; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static RpmlintParser rpmlintParser;

	// default constructor
	private RpmlintParser() {
		// Empty constructor for making it a singleton.
	}
	
	public static RpmlintParser getInstance() {
		if (rpmlintParser == null) {
			rpmlintParser = new RpmlintParser();
		}
		return rpmlintParser;
	}
		
	/**
	 * Parse visited resources.
	 * @param visitedResources The list of resources to parse.
	 * 
	 * @return
	 * 		a <code>RpmlintItem</code> ArrayList.
	 */
	public ArrayList<RpmlintItem> parseVisisted(ArrayList<String> visitedResources) {
		ArrayList<RpmlintItem> retList = new ArrayList<RpmlintItem>();
		// workaround "Argument list too long" xargs limitation. 
		ArrayList<List<String>> commandsList = splitArrayList(visitedResources, 500);
		Iterator<List<String>> iterator = commandsList.iterator();
		while (iterator.hasNext()) {
			if (commandsList.size() > 1)
				retList.addAll(parseRpmlintOutput(runRpmlintCommand(iterator.next())));
			else
				return parseRpmlintOutput(runRpmlintCommand(visitedResources));
		}
		return retList;

	}
	
	public void addMarker(IFile file, String message, int lineNumber, int charStart, int charEnd,
			int severity, String rpmlintID, String rpmlintReferedContent) {
		try {
			IMarker marker = file
					.createMarker(RpmlintBuilder.MARKER_ID);
			marker.setAttribute(IMarker.LOCATION, file.getFullPath().toString());
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.CHAR_START, charStart);
			marker.setAttribute(IMarker.CHAR_END, charEnd);
			marker.setAttribute(RpmlintMarkerResolutionGenerator.RPMLINT_ERROR_ID, rpmlintID);
			marker.setAttribute(RpmlintMarkerResolutionGenerator.RPMLINT_REFERED_CONTENT, rpmlintReferedContent);

		} catch (CoreException e) {
			RpmlintLog.logError(e);
		}
	}
	
	public void addMarker(IFile file, String message, int severity,
			String rpmlintID, String rpmlintReferedContent) {
		try {
			IMarker marker = file.createMarker(RpmlintBuilder.MARKER_ID);
			marker
					.setAttribute(IMarker.LOCATION, file.getFullPath()
							.toString());
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(
					RpmlintMarkerResolutionGenerator.RPMLINT_ERROR_ID,
					rpmlintID);
			marker.setAttribute(
					RpmlintMarkerResolutionGenerator.RPMLINT_REFERED_CONTENT,
					rpmlintReferedContent);

		} catch (CoreException e) {
			RpmlintLog.logError(e);
		}
	}
	
	public void deleteMarkers(IResource resource) {
		try {
			resource.deleteMarkers(RpmlintBuilder.MARKER_ID, false, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			RpmlintLog.logError(e);
		}
	}
	
	/**
	 * Parse a given rpmlint <code>InputStream</code>
	 * 
	 * @param 
	 * 		rpmlint <code>InputStream</code> to parse.
	 * @return
	 * 		a <code>RpmlintItem</code> ArrayList.
	 */
	private ArrayList<RpmlintItem> parseRpmlintOutput(BufferedInputStream in) {
		RpmlintItem item =  new RpmlintItem();
		ArrayList<RpmlintItem> rpmlintItems = new ArrayList<RpmlintItem>();
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
		String line;
		boolean firtItemLine = true;
		String[] lineItems;
		String description = EMPTY_STRING;
		try {
			while ((line = reader.readLine()) != null) {
				if (firtItemLine) {
					firtItemLine=false;
					lineItems = line.split(COLON, 4);
					item.setFileName(lineItems[0]);
					int lineNbr;
					
					
					// FIXME: last rpmlint version (0.83) contain a summary 
					// line at the bottom of it output, so if we 
					// detected this line we can safely return rpmlintItems, 
					// maybe we can find a better way to detect this line. 
					try {
						Integer.parseInt(line.split(SPACE)[0]);
						return rpmlintItems;
					} catch (NumberFormatException e) {
						// this line is not the summary
					}					
					
					// TODO: ask rpmlint upstream to display always the same output.
					// at the moment the line number is not always displayed.
					// If the same output is always used, all the workarounds for the line number can be
					// removed.
					try {
						lineNbr = Integer.parseInt(lineItems[1]);
						item.setSeverity(lineItems[2]);
						lineItems = lineItems[3].trim().split(SPACE, 2);						
					} catch (NumberFormatException e) {
						// No line number showed for this rpmlint warning.
						lineItems = line.split(COLON, 3);
						lineNbr = -1;
						item.setSeverity(lineItems[1]);
						lineItems = lineItems[2].trim().split(SPACE, 2);
					} 
					item.setLineNbr(lineNbr);					
					item.setId(lineItems[0]);
					// get content
					if (lineItems.length > 1)
						item.setReferedContent(lineItems[1]);
					else
						item.setReferedContent(EMPTY_STRING);
				} else {
					description += line + '\n';
				}
				
				if (line.equals(EMPTY_STRING)) {
					item.setMessage(description.substring(0, description.length() - 2));
					int useOfTabsAndSpaces = getMixedUseOfTabsAndSpaces(item.getReferedContent());
					if (useOfTabsAndSpaces != -1) 
						item.setLineNbr(useOfTabsAndSpaces);
					rpmlintItems.add(item);
					item = new RpmlintItem();
					firtItemLine=true;
					description = EMPTY_STRING;
				}

			}
			// Close the input stream
			in.close();
		} catch (IOException e) {
			RpmlintLog.logError(e);
		}
		return rpmlintItems;
	}
	
	/**
	 * Run rpmlint command on given visitedResources.
	 * 
	 * @param specContent
	 *            the specfile content
	 * @return the rpmlint command <code>InputStream</code>
	 * @throws IOException
	 */
	private BufferedInputStream runRpmlintCommand(List<String> visitedResources) {
		BufferedInputStream in = null;
		int i = 2;
		String[] cmd = new String[visitedResources.size() + i];
		cmd[0] = Activator.getRpmlintPath();
		cmd[1] = "-i"; //$NON-NLS-1$
		Iterator<String> iterator = visitedResources.iterator();
		while(iterator.hasNext()) {
			cmd[i] = iterator.next();
			i++;
		}
		try {
			in = Utils.runCommandToInputStream(cmd);
		} catch (IOException e) {
			// FIXME: rpmlint is not installed in the default place -> ask user to open the prefs page.
			RpmlintLog.logError(e);
		}
		return in;
	}
	
	/**
	 * 
	 * Return the line number for given specContent and strToFind, it returns -1
	 * if the string to find is not found.
	 * 
	 * @param specContent The content of the spec file.
	 * 
	 * @param strToFind The string we are looking for.
	 * @return the line number
	 */
	public int getRealLineNbr(String specContent, String strToFind) {
		int ret = -1;
		if (strToFind.equals(EMPTY_STRING)) {
			return ret;
		}
		String line;
		LineNumberReader reader = new LineNumberReader(new StringReader(
				specContent));
		try {
			while ((line = reader.readLine()) != null) {
				if (line.replaceAll("\t| ", EMPTY_STRING).indexOf( //$NON-NLS-1$
						strToFind.replaceAll("\t| ", EMPTY_STRING)) > -1) { //$NON-NLS-1$
					ret = reader.getLineNumber();
				}
			}
		} catch (IOException e) {
			// return -1 if an I/O Exception occure.
		}
		return ret;
	}
	
	private int getMixedUseOfTabsAndSpaces(String referedContent){
		int lineNbr = -1;
		if (referedContent.indexOf("(spaces: line") > -1) { //$NON-NLS-1$
			String tabsAndSpacesPref = Activator
			.getDefault()
			.getPreferenceStore()
			.getString(PreferenceConstants.P_RPMLINT_TABS_AND_SPACES);
			String[] spacesAndTabs = referedContent.split("line"); //$NON-NLS-1$
			if (tabsAndSpacesPref == PreferenceConstants.P_RPMLINT_SPACES)
				lineNbr = Integer.parseInt(spacesAndTabs[1]
				                                         .split(",")[0].trim()); //$NON-NLS-1$
			else
				lineNbr = Integer.parseInt(spacesAndTabs[2]
				                                         .replaceFirst("\\)", EMPTY_STRING).trim()); //$NON-NLS-1$
		}
		return lineNbr;
	}
	
	private ArrayList<List<String>> splitArrayList(ArrayList<String> list, int listSize) {
		ArrayList<List<String>> resultList = new ArrayList<List<String>>();
		if (list.size() <= listSize) {
			resultList.add(list);
			return resultList;
		}
		for (int i = 0; i < getNumberOfIterations(list, listSize); i++) {
			int maxLength = ((i + 1) * listSize > list.size()) ? list
					.size() : (i + 1) * listSize;
			List<List<String>> sublist = new ArrayList<List<String>>();
			sublist.add(list.subList(i * listSize, maxLength));
			resultList.addAll(sublist);
		}
		return resultList;		
	}
 
	private int getNumberOfIterations(List<String> list, int subCollectionSize) {
		return list.size() % subCollectionSize == 0 ? list.size()
				/ subCollectionSize : (list.size() / subCollectionSize) + 1;
	}


}
