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
package org.eclipse.linuxtools.systemtap.backup.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class STPPartitionScanner extends RuleBasedPartitionScanner {
	public final static String STP_COMMENT = "__stp_comment";
	public final static String STP_KEYWORD = "__stp_keyword";
	public final static String STP_STRING = "__stp_string";
	public final static String STP_CONDITIONAL = "__stp_conditional";

	public static String[] STP_PARTITION_TYPES = { IDocument.DEFAULT_CONTENT_TYPE, 
		STP_COMMENT, STP_KEYWORD, STP_STRING, STP_CONDITIONAL};

	/**
	 * Detect empty comments
	 */
	static class EmptyCommentDetector implements IWordDetector {
		@Override
		public boolean isWordStart(char c) {
			return (c == '/');
		}

		@Override
		public boolean isWordPart(char c) {
			return (c == '*' || c == '/');
		}
	}

	/**
	 * Cope with the empty comment issue.
	 */
	static class EmptyCommentRule extends WordRule implements IPredicateRule {

		private IToken fSuccessToken;

		public EmptyCommentRule(IToken successToken) {
			super(new EmptyCommentDetector());
			fSuccessToken= successToken;
			addWord("/**/", fSuccessToken); //$NON-NLS-1$
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			return evaluate(scanner);
		}

		@Override
		public IToken getSuccessToken() {
			return fSuccessToken;
		}
	}
	
	public STPPartitionScanner() {
		
		IToken stpComment = new Token(STP_COMMENT);
		IToken stpString = new Token(STP_STRING);
		IToken stpConditional = new Token(STP_CONDITIONAL);
		
		// Add special case word rule.
		EmptyCommentRule emptyCommentRule= new EmptyCommentRule(stpComment);

        setPredicateRules(new IPredicateRule[] {
        		new MultiLineRule("/*", "*/", stpComment),
        		new EndOfLineRule("/*", stpComment),
                new EndOfLineRule("#",  stpComment),
                new EndOfLineRule("//", stpComment),
	            emptyCommentRule,    
                new EndOfLineRule("#if", stpConditional),
                new EndOfLineRule("#else", stpConditional),
                new EndOfLineRule("#endif", stpConditional),
                new EndOfLineRule("#define", stpConditional),                
        		new SingleLineRule("\"", "\"", stpString, '\\'),
                new SingleLineRule("'", "'", stpString, '\\'),
             });

	}
}
