package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class SpecfilePartitionScanner extends RuleBasedPartitionScanner {

	public final static String SPEC_SCRIPT = "__spec_script";
	public final static String SPEC_FILES = "__spec_files";
	public final static String SPEC_CHANGELOG = "__spec_changelog";
	public final static String SPEC_PACKAGES = "__spec_packages";
	
	public static String[] SPEC_PARTITION_TYPES = { IDocument.DEFAULT_CONTENT_TYPE, SPEC_SCRIPT,
			SPEC_FILES, SPEC_CHANGELOG, SPEC_PACKAGES};
	
	/** All possible headers for sections of the type SPEC_SCRIPT */
	private static String[] sectionHeaders = { "%prep", "%build", "%install", "%pre",
		"%preun", "%post", "%postun", "%clean"};

	/** All possible headers for section that can come after sections of the type SPEC_SCRIPT */
	private static String[] sectionEndingHeaders = { "%prep", "%build", "%install", "%pre",
		"%preun", "%post", "%postun", "%clean", "%files"};
	
	public SpecfilePartitionScanner() {
		// FIXME:  do we need this?
		super();
		
		IToken specScript = new Token(SPEC_SCRIPT);
		IToken specFiles = new Token(SPEC_FILES);
		IToken specChangelog = new Token(SPEC_CHANGELOG);
		IToken specPackages = new Token(SPEC_PACKAGES);
		
		List rules = new ArrayList();
		
		// RPM packages
		for (int i = 0; i < SpecfilePackagesScanner.PACKAGES_TAGS.length; i++) 
			rules.add(new SingleLineRule(SpecfilePackagesScanner.PACKAGES_TAGS[i], "", specPackages, (char)0 , true));			
		
		// %changelog
		rules.add(new MultiLineRule("%changelog", "", specChangelog, (char)0 , true));
		
		// "%prep", "%build", "%install", "%pre", "%preun", "%post", "%postun"
		for (int i = 0; i < sectionHeaders.length; i++)
			rules.add(new SectionRule(sectionHeaders[i], sectionEndingHeaders, specScript));

		// %files
		rules.add(new SectionRule("%files", new String[] { "%files",
				"%changelog" }, specFiles));
		
		IPredicateRule[] result= new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
