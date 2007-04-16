/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alphonse Van Assche
 *     Andrew Overholt
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;


/**
 * URL hyperlink with macro detector.
 * derived form the JFace URLHyperlinkDetector class
 *
 */
public class URLHyperlinkWithMacroDetector extends URLHyperlinkDetector {

	private Specfile specfile;
	
	/**
	 * Creates a new URL hyperlink with macro detector.
	 */
	public URLHyperlinkWithMacroDetector(Specfile specfile) {
		this.specfile= specfile;
	}
	
	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		IHyperlink[] returned = super.detectHyperlinks(textViewer, region, canShowMultipleHyperlinks);
		
		if (returned.length > 0) {
		IHyperlink hyperlink = returned[0];
			if (hyperlink instanceof URLHyperlink) {
				URLHyperlink urlHyperlink = (URLHyperlink) hyperlink;
				String newURLString = resolveDefinesInURL(urlHyperlink.getURLString());
				return new IHyperlink[] {new URLHyperlink(urlHyperlink.getHyperlinkRegion(), newURLString)};
			}
		}
		return returned;
	}
	
	
	/**
	 * Resolve defines for a give URL string, if a define is not found or if there 
	 * is some other error, the original string is returned.
	 * 
	 * 
	 * @param urlString
	 * 				to resolve
	 * @return
	 * 				resolved URL String
	 */
	private String resolveDefinesInURL(String urlString) {
		String originalUrlString= urlString;
		SpecfileDefine define;
		try {
			Pattern variablePattern= Pattern.compile("%\\{(\\S+?)\\}");
			Matcher variableMatcher= variablePattern.matcher(urlString);
			while (variableMatcher.find()) {
				define= specfile.getDefine(variableMatcher.group(1));
				urlString= urlString.replaceAll(variableMatcher.group(1), define.getStringValue());
			}
			if (!urlString.equals(originalUrlString))
				urlString= urlString.replaceAll("\\%\\{|\\}", "");
			return urlString;
		} catch (Exception e) {
			// e.printStackTrace();
			return originalUrlString;
		}
		
	}

}
