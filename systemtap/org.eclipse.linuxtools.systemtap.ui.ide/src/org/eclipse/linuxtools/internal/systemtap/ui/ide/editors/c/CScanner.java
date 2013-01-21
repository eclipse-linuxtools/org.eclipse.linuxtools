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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.text.rules.*;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.editor.ColorManager;
import org.eclipse.linuxtools.systemtap.ui.editor.WhitespaceDetector;
import org.eclipse.linuxtools.systemtap.ui.editor.WordDetector;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;



/**
 * This class provides syntax highlighting functionality to <code>CEditor</code>. It uses a simple rule-based
 * scanning system.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class CScanner extends RuleBasedScanner {

	private static String[] fgKeywords= {"auto","break","case","const","continue","default","do","else","extern","for","goto","if","return","signed","sizeof","static","switch","typedef","unsigned","volatile","while" };
	private static String[] fgTypes=  { 	"char","double","enum","float","int","long","register","short","struct","union","void",
											"char*","double*","float*","int*","long*","short*","void*","u8","u16","u32","u64",
											"s8","s16","s32",""};
	private static String[] fgConstants= { "NULL" };
	private ColorManager manager;
	public CScanner(ColorManager manager) {
		LogManager.logDebug("Start CScanner: manager-" + manager, this); //$NON-NLS-1$
		this.manager = manager;
		initializeScanner();
		LogManager.logDebug("End CScanner:", this); //$NON-NLS-1$
	}

	/**
	 * This method initializes this instance of <code>CScanner</code>, allowing it to be used
	 * for syntax highlighting purposes by the editor.
	 */
	public void initializeScanner()
	{
		IPreferenceStore store = IDEPlugin.getDefault().getPreferenceStore();
		RGB cKeyword, cType, cString, cComment, cDefault, cPreprocessor;

		cKeyword = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_C_KEYWORD_COLOR);
		cType = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_C_TYPE_COLOR);
		cString = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_C_STRING_COLOR);
		cComment = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_C_COMMENT_COLOR);
		cDefault = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_C_DEFAULT_COLOR);
		cPreprocessor = PreferenceConverter.getColor(store, IDEPreferenceConstants.P_C_PREPROCESSOR_COLOR);

		IToken keyword = new Token(new TextAttribute(manager.getColor(cKeyword), null, SWT.BOLD));
		IToken type = new Token(new TextAttribute(manager.getColor(cType)));
		IToken string = new Token(new TextAttribute(manager.getColor(cString)));
		IToken comment = new Token(new TextAttribute(manager.getColor(cComment)));
		IToken other = new Token(new TextAttribute(manager.getColor(cDefault)));
		IToken preprocessor = new Token(new TextAttribute(manager.getColor(cPreprocessor)));

		List<IRule> rules= new ArrayList<IRule>();

		// Add rules for single line comments.
		rules.add(new EndOfLineRule("//", comment)); //$NON-NLS-1$
		rules.add(new SingleLineRule("#", " ", preprocessor));

		// Add rule for multiple-line comment.
		rules.add(new PatternRule("/*", "*/", comment, '\\', false));

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new WhitespaceDetector()));

		// Add word rule for keywords, types, and constants.
		WordRule wordRule= new WordRule(new WordDetector(), other);
		for (String fgKeyword: fgKeywords) {
			wordRule.addWord(fgKeyword, keyword);
		}
		for (String fgType: fgTypes) {
			wordRule.addWord(fgType, type);
		}
		for (String fgConstant: fgConstants) {
			wordRule.addWord(fgConstant, type);
		}
		rules.add(wordRule);

		IRule[] result= new IRule[rules.size()];
		setDefaultReturnToken(other);
		rules.toArray(result);
		setRules(result);
	}
}
