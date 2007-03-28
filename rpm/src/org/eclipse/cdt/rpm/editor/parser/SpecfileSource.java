package org.eclipse.cdt.rpm.editor.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;

public class SpecfileSource extends SpecfileElement {
	int number;
	int lineNumber = -1;
	String fileName;
	static final int SOURCE = 0;
	static final int PATCH = 1;
	int sourceType;
	List linesUsed;
	
	public int getSourceType() {
		return sourceType;
	}
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	public SpecfileSource(int number, String fileName) {
		super("source");
		this.number = number;
		this.fileName = fileName;
		this.linesUsed = new ArrayList();
	}
	public String getFileName() {
		return resolve(fileName);
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public void addLineUsed(int lineNumber) {
		linesUsed.add(new Integer(lineNumber));
	}
	public void removeLineUsed(int lineNumber) {
		linesUsed.remove(new Integer(lineNumber));
	}
	public List getLinesUsed() {
		return linesUsed;
	}
	public String toString() {
		if (sourceType == SOURCE)
			return "Source #" + number + " (line #" + lineNumber + ", used on lines " + getLinesUsed() + ") -> " + fileName;
		return "Patch #" + number + " (line #" + lineNumber + ", used on lines " + getLinesUsed() + ") -> " + fileName;
	}
	
	// Note that changeReferences assumes that the number of the source/patch
	// has *already been set*.  If this is not true, it will simply do nothing
	public void changeReferences(int oldPatchNumber) {
		Specfile specfile = this.getSpecfile();
		Pattern patchPattern = Pattern.compile("%patch" + oldPatchNumber);
		for (Iterator lineIter = getLinesUsed().iterator(); lineIter.hasNext();) {
			int lineNumber = ((Integer) lineIter.next()).intValue();
			String line;
			try {
				line = specfile.getLine(lineNumber);
				Matcher patchMatcher = patchPattern.matcher(line);
				if (!patchMatcher.find()) {
					System.out.println("error:  can't match " + patchPattern.pattern());
//					throw new BadLocationException("can't match " + patchPattern);
				}
				specfile.changeLine(lineNumber, line.replaceAll(patchPattern.pattern(), "%patch" + number));
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void changeDeclaration(int oldPatchNumber) {
		Specfile specfile = this.getSpecfile();
		Pattern patchPattern = Pattern.compile("Patch" + oldPatchNumber);
		String line;
		try {
			line = specfile.getLine(lineNumber);
			Matcher patchMatcher = patchPattern.matcher(line);
			if (!patchMatcher.find())
				System.out.println("error");
			specfile.changeLine(lineNumber, line.replaceAll(patchPattern.pattern(), "Patch" + number));
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
}
