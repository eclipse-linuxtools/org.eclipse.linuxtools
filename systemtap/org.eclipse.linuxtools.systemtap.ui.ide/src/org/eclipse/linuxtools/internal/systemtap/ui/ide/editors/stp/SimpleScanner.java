/*******************************************************************************
 * Copyright (c) 2004, 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Anton Leherbauer - adding tokens for preprocessing directives
 *     Markus Schorn - classification of preprocessing directives.
 *     Red Hat Inc. - modified for use in SystemTap
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * A C/C++ lexical scanner, which does no preprocessing,
 * but tokenizes preprocessor directives, whitespace and comments.
 *
 * @since 4.0
 */
public class SimpleScanner {
	private static final int EOFCHAR= -1;
	protected static HashMap<String, Integer> fgKeywords= new HashMap<>();

	protected ScannerContext fContext;
	protected StringBuilder fTokenBuffer= new StringBuilder();
	private final StringBuilder fUniversalCharBuffer= new StringBuilder();

	protected void init(Reader reader) {
	    fContext = new ScannerContext().initialize(reader);
	}

	private int getChar(boolean insideString) {
	    int c = EOFCHAR;

	    if (fContext.undoStackSize() != 0) {
	        c = fContext.popUndo();
	    } else {
	        try {
	            c = fContext.read();
	        } catch (IOException e) {
	            c = EOFCHAR;
	        }
	    }

	    fTokenBuffer.append((char) c);

	    if (!insideString && c == '\\') {
	        c = getChar(false);
	        if (c == '\r') {
	            c = getChar(false);
	            if (c == '\n') {
	                c = getChar(false);
	            }
	        } else if (c == '\n') {
	            c = getChar(false);
	        } else if (c == 'U' || c == 'u') {
	    		fUniversalCharBuffer.setLength(0);
	    		fUniversalCharBuffer.append('\\').append((char) c);
	        	c = getUniversalCharacter();
	        } else {
	        	ungetChar(c);
	        	c = '\\';
	        }
	    }

	    return c;
	}

	private int getUniversalCharacter() {
		int unicode = 0;
        do {
            int c = getChar(true);
            int digit;
            switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            	digit = c - '0';
            	break;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            	digit = c - 'a' + 10;
            	break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            	digit = c - 'A' + 10;
            	break;
            default:
            	internalUngetChar(c);
            	return unicode;
            }
            fUniversalCharBuffer.append((char) c);
            unicode <<= 4;
            unicode += digit;
        } while (true);
	}

	private void internalUngetChar(int c) {
		fTokenBuffer.deleteCharAt(fTokenBuffer.length() - 1);
		fContext.pushUndo(c);
	}

	private void ungetChar(int c) {
		if (c < 256 || c == fTokenBuffer.charAt(fTokenBuffer.length() - 1)) {
        	internalUngetChar(c);
		} else if (fUniversalCharBuffer.length() > 0) {
			char[] chs = fUniversalCharBuffer.toString().toCharArray();
			for (int i = chs.length - 1; i >= 0; --i) {
            	internalUngetChar(chs[i]);
			}
		} else {
        	internalUngetChar(c);
		}
	}

	static {
        fgKeywords.put("and", new Integer(Token.t_and)); //$NON-NLS-1$
        fgKeywords.put("and_eq", new Integer(Token.t_and_eq)); //$NON-NLS-1$
        fgKeywords.put("asm", new Integer(Token.t_asm)); //$NON-NLS-1$
        fgKeywords.put("auto", new Integer(Token.t_auto)); //$NON-NLS-1$
        fgKeywords.put("bitand", new Integer(Token.t_bitand)); //$NON-NLS-1$
        fgKeywords.put("bitor", new Integer(Token.t_bitor)); //$NON-NLS-1$
        fgKeywords.put("bool", new Integer(Token.t_bool)); //$NON-NLS-1$
        fgKeywords.put("break", new Integer(Token.t_break)); //$NON-NLS-1$
        fgKeywords.put("case", new Integer(Token.t_case)); //$NON-NLS-1$
        fgKeywords.put("catch", new Integer(Token.t_catch)); //$NON-NLS-1$
        fgKeywords.put("char", new Integer(Token.t_char)); //$NON-NLS-1$
        fgKeywords.put("class", new Integer(Token.t_class)); //$NON-NLS-1$
        fgKeywords.put("compl", new Integer(Token.t_compl)); //$NON-NLS-1$
        fgKeywords.put("const", new Integer(Token.t_const)); //$NON-NLS-1$
        fgKeywords.put("const_cast", new Integer(Token.t_const_cast)); //$NON-NLS-1$
        fgKeywords.put("continue", new Integer(Token.t_continue)); //$NON-NLS-1$
        fgKeywords.put("default", new Integer(Token.t_default)); //$NON-NLS-1$
        fgKeywords.put("delete", new Integer(Token.t_delete)); //$NON-NLS-1$
        fgKeywords.put("do", new Integer(Token.t_do)); //$NON-NLS-1$
        fgKeywords.put("double", new Integer(Token.t_double)); //$NON-NLS-1$
        fgKeywords.put("dynamic_cast", new Integer(Token.t_dynamic_cast)); //$NON-NLS-1$
        fgKeywords.put("else", new Integer(Token.t_else)); //$NON-NLS-1$
        fgKeywords.put("enum", new Integer(Token.t_enum)); //$NON-NLS-1$
        fgKeywords.put("explicit", new Integer(Token.t_explicit)); //$NON-NLS-1$
        fgKeywords.put("export", new Integer(Token.t_export)); //$NON-NLS-1$
        fgKeywords.put("extern", new Integer(Token.t_extern)); //$NON-NLS-1$
        fgKeywords.put("false", new Integer(Token.t_false)); //$NON-NLS-1$
        fgKeywords.put("float", new Integer(Token.t_float)); //$NON-NLS-1$
        fgKeywords.put("for", new Integer(Token.t_for)); //$NON-NLS-1$
        fgKeywords.put("friend", new Integer(Token.t_friend)); //$NON-NLS-1$
        fgKeywords.put("goto", new Integer(Token.t_goto)); //$NON-NLS-1$
        fgKeywords.put("if", new Integer(Token.t_if)); //$NON-NLS-1$
        fgKeywords.put("inline", new Integer(Token.t_inline)); //$NON-NLS-1$
        fgKeywords.put("int", new Integer(Token.t_int)); //$NON-NLS-1$
        fgKeywords.put("long", new Integer(Token.t_long)); //$NON-NLS-1$
        fgKeywords.put("mutable", new Integer(Token.t_mutable)); //$NON-NLS-1$
        fgKeywords.put("namespace", new Integer(Token.t_namespace)); //$NON-NLS-1$
        fgKeywords.put("new", new Integer(Token.t_new)); //$NON-NLS-1$
        fgKeywords.put("not", new Integer(Token.t_not)); //$NON-NLS-1$
        fgKeywords.put("not_eq", new Integer(Token.t_not_eq)); //$NON-NLS-1$
        fgKeywords.put("operator", new Integer(Token.t_operator)); //$NON-NLS-1$
        fgKeywords.put("or", new Integer(Token.t_or)); //$NON-NLS-1$
        fgKeywords.put("or_eq", new Integer(Token.t_or_eq)); //$NON-NLS-1$
        fgKeywords.put("private", new Integer(Token.t_private)); //$NON-NLS-1$
        fgKeywords.put("protected", new Integer(Token.t_protected)); //$NON-NLS-1$
        fgKeywords.put("public", new Integer(Token.t_public)); //$NON-NLS-1$
        fgKeywords.put("register", new Integer(Token.t_register)); //$NON-NLS-1$
        fgKeywords.put("reinterpret_cast", new Integer(Token.t_reinterpret_cast)); //$NON-NLS-1$
        fgKeywords.put("return", new Integer(Token.t_return)); //$NON-NLS-1$
        fgKeywords.put("short", new Integer(Token.t_short)); //$NON-NLS-1$
        fgKeywords.put("signed", new Integer(Token.t_signed)); //$NON-NLS-1$
        fgKeywords.put("sizeof", new Integer(Token.t_sizeof)); //$NON-NLS-1$
        fgKeywords.put("static", new Integer(Token.t_static)); //$NON-NLS-1$
        fgKeywords.put("static_cast", new Integer(Token.t_static_cast)); //$NON-NLS-1$
        fgKeywords.put("struct", new Integer(Token.t_struct)); //$NON-NLS-1$
        fgKeywords.put("switch", new Integer(Token.t_switch)); //$NON-NLS-1$
        fgKeywords.put("template", new Integer(Token.t_template)); //$NON-NLS-1$
        fgKeywords.put("this", new Integer(Token.t_this)); //$NON-NLS-1$
        fgKeywords.put("throw", new Integer(Token.t_throw)); //$NON-NLS-1$
        fgKeywords.put("true", new Integer(Token.t_true)); //$NON-NLS-1$
        fgKeywords.put("try", new Integer(Token.t_try)); //$NON-NLS-1$
        fgKeywords.put("typedef", new Integer(Token.t_typedef)); //$NON-NLS-1$
        fgKeywords.put("typeid", new Integer(Token.t_typeid)); //$NON-NLS-1$
        fgKeywords.put("typename", new Integer(Token.t_typename)); //$NON-NLS-1$
        fgKeywords.put("union", new Integer(Token.t_union)); //$NON-NLS-1$
        fgKeywords.put("unsigned", new Integer(Token.t_unsigned)); //$NON-NLS-1$
        fgKeywords.put("using", new Integer(Token.t_using)); //$NON-NLS-1$
        fgKeywords.put("virtual", new Integer(Token.t_virtual)); //$NON-NLS-1$
        fgKeywords.put("void", new Integer(Token.t_void)); //$NON-NLS-1$
        fgKeywords.put("volatile", new Integer(Token.t_volatile)); //$NON-NLS-1$
        fgKeywords.put("wchar_t", new Integer(Token.t_wchar_t)); //$NON-NLS-1$
        fgKeywords.put("while", new Integer(Token.t_while)); //$NON-NLS-1$
        fgKeywords.put("xor", new Integer(Token.t_xor)); //$NON-NLS-1$
        fgKeywords.put("xor_eq", new Integer(Token.t_xor_eq)); //$NON-NLS-1$

        // additional java keywords
        fgKeywords.put("abstract", new Integer(Token.t_abstract)); //$NON-NLS-1$
        fgKeywords.put("boolean", new Integer(Token.t_boolean)); //$NON-NLS-1$
        fgKeywords.put("byte", new Integer(Token.t_byte)); //$NON-NLS-1$
        fgKeywords.put("extends", new Integer(Token.t_extends)); //$NON-NLS-1$
        fgKeywords.put("final", new Integer(Token.t_final)); //$NON-NLS-1$
        fgKeywords.put("finally", new Integer(Token.t_finally)); //$NON-NLS-1$
        fgKeywords.put("implements", new Integer(Token.t_implements)); //$NON-NLS-1$
        fgKeywords.put("import", new Integer(Token.t_import)); //$NON-NLS-1$
        fgKeywords.put("interface", new Integer(Token.t_interface)); //$NON-NLS-1$
        fgKeywords.put("instanceof", new Integer(Token.t_instanceof)); //$NON-NLS-1$
        fgKeywords.put("native", new Integer(Token.t_native)); //$NON-NLS-1$
        fgKeywords.put("null", new Integer(Token.t_null)); //$NON-NLS-1$
        fgKeywords.put("package", new Integer(Token.t_package)); //$NON-NLS-1$
        fgKeywords.put("super", new Integer(Token.t_super)); //$NON-NLS-1$
        fgKeywords.put("synchronized", new Integer(Token.t_synchronized)); //$NON-NLS-1$
        fgKeywords.put("throws", new Integer(Token.t_throws)); //$NON-NLS-1$
        fgKeywords.put("transient", new Integer(Token.t_transient)); //$NON-NLS-1$
    }
}