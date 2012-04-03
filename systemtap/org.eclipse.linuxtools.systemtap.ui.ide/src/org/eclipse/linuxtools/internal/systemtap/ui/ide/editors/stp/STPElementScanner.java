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


import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.linuxtools.systemtap.ui.editor.ColorManager;
import org.eclipse.swt.SWT;

public class STPElementScanner extends BufferedRuleBasedScanner {

	private String[] keywordList= {"probe", "for", "else", "foreach", "exit", "printf", "in", "return",
			"break", "global", "next", "while", "if", "delete", "#include", "function", "do", 
			"print", "error","log", "printd", "printdln", "println", "sprint", "sprintf", "system", "warn"};


// TODO: Not sure if we want these keywords or not. Defer for now.
//			"backtrace", "caller", "caller_addr", "cpu", "egid", "euid", "execname", "gid", "is_return",
//			"pexecname", "pid", "ppid", "tid", "uid", "print_backtrace", "print_regs", "print_stack", 
//			"stack_size", "stack_unused", "stack_used", "stp_pid", "target"};
	
	/**
	 * 
	 * Build Element scanner for Syntax Highlighting for Systemtap Editor
	 *  
	 * @param manager ColorManager to source highlighting.
	 * 
	 */
	public STPElementScanner(ColorManager manager) {
		IToken defaultToken = new Token(new TextAttribute(manager
				.getColor(STPColorConstants.DEFAULT)));

		IToken keywordToken = new Token(new TextAttribute(manager
				.getColor(STPColorConstants.KEYWORD),null,SWT.BOLD));

		IToken commentToken = new Token(new TextAttribute(manager
				.getColor(STPColorConstants.COMMENT)));

		IToken stringToken = new Token(new TextAttribute(manager
				.getColor(STPColorConstants.STP_STRING)));


		// Build keyword scanner
		WordRule keywordsRule = new WordRule(new IWordDetector() {

			public boolean isWordStart(char c) {
				// probe kernel.function("schedule") is a valid name in
				// Systemtap, but we do not want to highlight the function
				// here as a keyword. Same with foo.return and so on.
				if (c == '.') {
					return true;
				}

				return Character.isJavaIdentifierStart(c);
			}

			public boolean isWordPart(char c) {
				// Set isWordStart for . rule.
				if (c == '.') {
					return true;
				}
				
				return  Character.isJavaIdentifierPart(c);
			}

		}, defaultToken, true);

		for (int i=0; i<keywordList.length; i++)
			keywordsRule.addWord(keywordList[i], keywordToken);

        setRules(new IRule[] {
        		new MultiLineRule("/*", "*/", commentToken),
        		new EndOfLineRule("/*", commentToken),
                new EndOfLineRule("#", commentToken),
                new EndOfLineRule("//",  commentToken),
        		new EndOfLineRule("#if", defaultToken),
                new EndOfLineRule("#else", defaultToken),
                new EndOfLineRule("#endif", defaultToken),
                new EndOfLineRule("#define", defaultToken),
                new SingleLineRule("\"", "\"", stringToken, '\\'),
                new SingleLineRule("'", "'", stringToken, '\\'),
        		keywordsRule,
                new WhitespaceRule(new IWhitespaceDetector() {
                   public boolean isWhitespace(char c) {
                      return Character.isWhitespace(c);
                   }
                }),
             });
	}
}
