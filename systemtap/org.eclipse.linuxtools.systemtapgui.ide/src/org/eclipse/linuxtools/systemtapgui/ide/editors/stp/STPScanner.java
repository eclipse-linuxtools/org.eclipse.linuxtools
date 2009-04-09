/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtapgui.ide.editors.stp;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.text.rules.*;
import org.eclipse.linuxtools.systemtapgui.editor.ColorManager;
import org.eclipse.linuxtools.systemtapgui.editor.WhitespaceDetector;
import org.eclipse.linuxtools.systemtapgui.editor.WordDetector;
import org.eclipse.linuxtools.systemtapgui.ide.internal.IDEPlugin;
import org.eclipse.linuxtools.systemtapgui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtapgui.logging.LogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;



public class STPScanner extends RuleBasedScanner {
	private static String[] fgKeywords= { /*"global",*/ "function", "probe", "begin", "for", "foreach", "if", "else", "while", /*"return",*/ "break", "next", "continue", "in", "delete"};
	private static String[] fgTypes=  { "global", "string", "long" }; 
	private static String[] fgConstants= { "true", "false", "null", "THIS" }; 
	
	public STPScanner(ColorManager manager) {
		LogManager.logDebug("Start STPScanner: manager-" + manager, this);
		this.manager = manager;
		initializeScanner();
		LogManager.logDebug("End STPScanner:", this);
	}
	
	/**
	 * Specifies colors taken from preferences to string types. Sets up single line comment rules,
	 * rules governing text surrounded in quotes, the generic whitespace rule, and rules for keywords,
	 * types, and constants.
	 */
	public void initializeScanner() {
		LogManager.logDebug("Start initializeScanner:", this);
		IPreferenceStore store = IDEPlugin.getDefault().getPreferenceStore();
		RGB keywordColor, typeColor, stringColor, commentColor, defaultColor;
		
		keywordColor = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_STP_KEYWORD_COLOR);
		typeColor = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_STP_TYPE_COLOR);
		stringColor = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_STP_STRING_COLOR);
		commentColor = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_STP_COMMENT_COLOR);
		defaultColor = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_STP_DEFAULT_COLOR);

		IToken keyword = new Token(new TextAttribute(manager.getColor(keywordColor), null, SWT.BOLD));
		IToken type = new Token(new TextAttribute(manager.getColor(typeColor)));
		IToken string = new Token(new TextAttribute(manager.getColor(stringColor)));
		IToken comment = new Token(new TextAttribute(manager.getColor(commentColor)));
		IToken other = new Token(new TextAttribute(manager.getColor(defaultColor)));
		
		List<IRule> rules= new ArrayList<IRule>();
		
		// Add rules for single line comments.
		rules.add(new EndOfLineRule("#", comment)); //$NON-NLS-1$
		rules.add(new EndOfLineRule("//", comment)); //$NON-NLS-1$
		
		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
	
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new WhitespaceDetector()));
		
		// Add word rule for keywords, types, and constants.
		WordRule wordRule= new WordRule(new WordDetector(), other);
		for (int i= 0; i < fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], keyword);
		for (int i= 0; i < fgTypes.length; i++)
			wordRule.addWord(fgTypes[i], type);
		for (int i= 0; i < fgConstants.length; i++)
			wordRule.addWord(fgConstants[i], type);
		rules.add(wordRule);
				
		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
		LogManager.logDebug("End initializeScanner:", this);
	}
	
	private ColorManager manager;
}
