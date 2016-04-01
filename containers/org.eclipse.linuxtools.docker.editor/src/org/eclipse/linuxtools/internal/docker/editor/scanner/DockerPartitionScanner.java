/*******************************************************************************
 * Copyright (c) 2015-2016 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor.scanner;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * This scanner partitions the document in two things:
 * <ul>
 * <li>Comments, of type TYPE_COMMENT</li>
 * <li>Everything else, of type {@link IDocument#DEFAULT_CONTENT_TYPE}</li>
 * </ul>
 */
public class DockerPartitionScanner extends RuleBasedPartitionScanner {

	public final static String TYPE_COMMENT = "__docker_comment";

	public final static String[] ALLOWED_CONTENT_TYPES = { TYPE_COMMENT };

	public DockerPartitionScanner() {
		IToken commentToken = new Token(TYPE_COMMENT);
		IPredicateRule[] rules = new IPredicateRule[1];
		rules[0] = new EndOfLineRule(DockerCommentScanner.COMMENT_SEQUENCE, commentToken);
		setPredicateRules(rules);
	}

}
