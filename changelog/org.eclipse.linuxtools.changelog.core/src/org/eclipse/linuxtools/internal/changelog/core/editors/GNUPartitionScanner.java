/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class GNUPartitionScanner extends RuleBasedPartitionScanner {
	// We partition into sections we want to parse from the beginning every time
	// a change occurs within the area.  In this case, we are only interested in
	// email entries which are surrounded by < and > and source entries which
	// have one or more file names which may extend across lines.
	public static final String CHANGELOG_EMAIL = "changelog_email"; //$NON-NLS-1$
	public static final String CHANGELOG_SRC_ENTRY = "changelog_src_entry"; //$NON-NLS-1$
	public static final String[] CHANGELOG_PARTITION_TYPES= 
		new String[] { CHANGELOG_EMAIL, CHANGELOG_SRC_ENTRY };
	
	/**
	 * Creates the partitioner and sets up the appropriate rules.
	 */
	public GNUPartitionScanner() {
		super();
		
		List<IPredicateRule> rules= new ArrayList<IPredicateRule>();
		Token email = new Token(CHANGELOG_EMAIL);
		Token srcEntry = new Token(CHANGELOG_SRC_ENTRY);
		
		rules.add(new SingleLineRule("<", ">", email, '\\'));
		rules.add(new MultiLineRule("* ", ":", srcEntry, '\\', true));
		
		IPredicateRule[] result= new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
	
}
