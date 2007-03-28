package org.eclipse.cdt.rpm.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.rpm.editor.parser.Specfile;
import org.eclipse.cdt.rpm.editor.parser.SpecfileDefine;
import org.eclipse.cdt.rpm.editor.parser.SpecfilePatchMacro;
import org.eclipse.cdt.rpm.editor.parser.SpecfileSource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class SpecfileHover implements ITextHover, ITextHoverExtension {



	private SpecfileEditor editor;


	public SpecfileHover(SpecfileEditor editor) {
		this.editor = editor;
	}

	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion == null || hoverRegion.getLength() == 0)
			return null;
		
		Specfile spec = editor.getSpecfile();
		
		
		String macroName;
		try {
			macroName = textViewer.getDocument().get(hoverRegion.getOffset() + 1, hoverRegion.getLength() - 1);
		} catch (BadLocationException e) {
			return null;
		}
		
                
                // First we try to get a define based on the given name
		SpecfileDefine define = spec.getDefine(macroName);
		
                String value = macroName + ": ";
                
		if (define != null) {
                    value += define.getStringValue();
                    return value;
                }
                
		String macroLower = macroName.toLowerCase();
                
                // If there's no such define we try to see if it corresponds to
                // a Source or Patch declaration
       
                Pattern p = Pattern.compile("(source|patch)(\\d*)");
                Matcher m = p.matcher(macroLower);
                
                if (m.matches()){
                    String digits = m.group(2);
                    
                    SpecfileSource source = null;
                    int number = -1;
                    
                    if (digits != null && digits.equals("")){ 
                        number = 0;
                    }else if (digits != null && !digits.equals("")){
                        number =Integer.parseInt(digits);
                    }
                    
                    if (number != -1){
                        if( m.group(1).equals("source"))
                            source = spec.getSource(number);
                        else if (m.group(1).equals("patch"))
                            source = spec.getPatch(number);
                        
                        if (source != null){
                            value += source.getFileName();
                            
                            return value;
                        }
                    }

                }
               
                
		return value;
	}

	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		
		if (textViewer != null) {
			/*
			 * If the hover offset falls within the selection range return the
			 * region for the whole selection.
			 */
			Point selectedRange = textViewer.getSelectedRange();
			if (selectedRange.x >= 0 && selectedRange.y	 > 0
					&& offset >= selectedRange.x
					&& offset <= selectedRange.x + selectedRange.y)
				return new Region(selectedRange.x, selectedRange.y);
			else {
				 return findWord(textViewer.getDocument(), offset);
				 
			}
		}
		return null;
	}

	public IInformationControlCreator getHoverControlCreator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public static IRegion findWord(IDocument document, int offset) {
		int start = -1;
		int end = -1;
		boolean beginsWithBrace = false;

		try {
			int pos = offset;
			char c;

			while (pos >= 0) {
				c = document.getChar(pos);
				if (c == '%') {
					if (document.getChar(pos + 1) == '{')
						beginsWithBrace = true;
					break;
				}
				else if (c == '\n' || c == '}'){
					// if we hit the beginning of the line, it's not a macro
					return new Region(offset, 0);
				}
				--pos;
			}
			
			if (!beginsWithBrace)
				--pos;

			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (beginsWithBrace && (c == '}')) {
					break;
				}
				else if (c == '\n' || c == '%'){
					return new Region(offset, 0);
				} else if (!beginsWithBrace && c == ' ') {
					break;
				}
				++pos;
			}

			end = pos;

		} catch (BadLocationException x) {
		}

		if (start > -1 && end > -1) {
			if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}

}
