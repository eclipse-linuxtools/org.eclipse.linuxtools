package org.eclipse.cdt.rpm.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
/**
 * This class is used specifically of the syntax coloring of the %changelog
 * section of a spec file, which has completely different syntax than 
 * the rest of the file.
 * 
 */
public class SpecfileChangelogScanner extends RuleBasedScanner {

	private static String[] sections = { "%changelog" };
	private IToken fLastToken;
	
	public SpecfileChangelogScanner(ColorManager manager) {
		IToken sectionToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.SECTIONS), null, SWT.ITALIC));
		
		IToken authorEmail = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.AUTHOR_MAIL), null, SWT.NONE));

		IToken versionRelease = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.VER_REL), null, SWT.NONE));
		
		List rules = new ArrayList();

		// %prep, %build, ...
		WordRule wordRule = new WordRule(new KeywordWordDetector(), Token.UNDEFINED);
		for (int i = 0; i < sections.length; i++)
			wordRule.addWord(sections[i], sectionToken);
		rules.add(wordRule);

	
		AuthorEmailRule emailRule= new AuthorEmailRule(authorEmail);
		rules.add(emailRule);
		
		VersionReleaseRule verRelRule = new VersionReleaseRule(versionRelease, authorEmail, this);
		rules.add(verRelRule);
		
		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
	
	protected IToken getLastToken (){
		return fLastToken;
	}

	public IToken nextToken() {
		fLastToken = super.nextToken();
		return fLastToken;
	}
}
