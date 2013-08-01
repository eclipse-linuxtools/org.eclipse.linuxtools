/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPIndenter.MatchMode;

/**
 * Very basic auto edit strategy simply completing opening with closing brackets and quotes.
 */
public class STPAutoEditStrategy extends
		DefaultIndentLineAutoEditStrategy {
	private static final String LINE_COMMENT= "//"; //$NON-NLS-1$
	private boolean fCloseBrace = true;

	private String fPartitioning;
	private IProject fProject;

	public STPAutoEditStrategy(String fPartitioning, IProject project) {
		this.fPartitioning = fPartitioning;
		this.fProject = project;
	}

	/**
	 * Returns the block balance, i.e. zero if the blocks are balanced at
	 * <code>offset</code>, a negative number if there are more closing than opening
	 * braces, and a positive number if there are more opening than closing braces.
	 *
	 * @param document
	 * @param offset
	 * @param partitioning
	 * @return the block balance
	 */
	private static int getBlockBalance(IDocument document, int offset, String partitioning) {
		if (offset < 1)
			return -1;
		if (offset >= document.getLength())
			return 1;

		int begin = offset;
		int end = offset - 1;

		STPHeuristicScanner scanner = new STPHeuristicScanner(document);

		while (true) {
			begin = scanner.findOpeningPeer(begin - 1, '{', '}');
			end = scanner.findClosingPeer(end + 1, '{', '}');
			if (begin == -1 && end == -1)
				return 0;
			if (begin == -1)
				return -1;
			if (end == -1)
				return 1;
		}
	}

	@Override
	public void customizeDocumentCommand(IDocument document,
			DocumentCommand command) {
		boolean modified = false;
		boolean isNewLine= command.length == 0 && command.text != null
				&& isLineDelimiter(document, command.text);
		if (isNewLine) {
			smartIndentAfterNewLine(document, command);
		} else if (command.text.length() == 1) {
			smartIndentOnKeypress(document, command);
		} else if (command.text.length() > 1
				&& command.text.trim().length() != 0) {
			smartPaste(document, command); // no smart backspace for paste
		}
		if (command.text.equals("\"") && !inStringOrComment(document, command)) { //$NON-NLS-1$
			command.text = "\"\""; //$NON-NLS-1$
			modified = true;
		} else if (command.text.equals("(") && !inStringOrComment(document, command)) { //$NON-NLS-1$
			command.text = "()"; //$NON-NLS-1$
			modified = true;
		} else if (command.text.equals("[") && !inStringOrComment(document, command)) { //$NON-NLS-1$
			command.text = "[]"; //$NON-NLS-1$
			modified = true;
		}

		if (modified) {
			command.caretOffset = command.offset + 1;
			command.shiftsCaret = false;
		}

		super.customizeDocumentCommand(document, command);
	}

	private boolean inStringOrComment(IDocument d, DocumentCommand c) {
		int docLength = d.getLength();
		if (c.offset == -1 || docLength == 0)
			return false;
		try {
			ITypedRegion partition= TextUtilities.getPartition(d, fPartitioning, c.offset, false);
			String partitionType = partition.getType();
			if (c.offset > 0 &&
					(STPPartitionScanner.STP_COMMENT.equals(partitionType)
							|| STPPartitionScanner.STP_MULTILINE_COMMENT.equals(partitionType)
							|| STPPartitionScanner.STP_STRING.equals(partitionType))) {
				return true;
			}
			IRegion lineInfo = d.getLineInformationOfOffset(c.offset);
			int offset = lineInfo.getOffset();
			boolean inChar = false;
			boolean inString = false;
			boolean inComment = false;
			for (int i = offset; i < c.offset; ++i) {
				char ch = d.getChar(i);
				switch (ch) {
				case '\"':
					if (!inChar)
						inString = !inString;
					break;
				case '\'':
					if (!inString)
						inChar = !inChar;
					break;
				case '\\':
					++i;
					break;
				case '/':
					if (!inString && !inChar) {
						ch = d.getChar(i + 1);
						if (ch == '/')
							return true; // We have a line comment
						else if (ch == '*')
							inComment = true;
					}
					break;
				case '#':
					if (!inString && !inChar)
						return true;
					break;
				case '*':
					if (!inString && !inChar) {
						if (inComment) {
							ch = d.getChar(i + 1);
							if (ch == '/')
								inComment = false;
						}
					}
					break;
				}
			}
			return inString || inChar || inComment;

		} catch (BadLocationException e) {
			IDEPlugin.log(e);
		}
		return false;
	}


	private void smartIndentAfterNewLine(IDocument d, DocumentCommand c) {
		int docLength = d.getLength();
		if (c.offset == -1 || docLength == 0)
			return;

		int addIndent= 0;
		STPHeuristicScanner scanner= new STPHeuristicScanner(d);
		try {
			ITypedRegion partition= TextUtilities.getPartition(d, fPartitioning, c.offset, false);
			if (STPPartitionScanner.STP_CONDITIONAL.equals(partition.getType()) && c.offset > 0 && d.getChar(c.offset-1) == '\\') {
				scanner = new STPHeuristicScanner(d, fPartitioning, STPPartitionScanner.STP_CONDITIONAL);
				addIndent= 1;
			}

			int line = d.getLineOfOffset(c.offset);
			IRegion reg = d.getLineInformation(line);
			int start = reg.getOffset();
			int lineEnd = start + reg.getLength();

			StringBuilder indent= null;
			STPIndenter indenter= new STPIndenter(d, scanner, fProject);
			indent= indenter.computeIndentation(c.offset);
			if (indent == null) {
				indent= new StringBuilder();
			}
			if (addIndent > 0 && indent.length() == 0) {
				indent= indenter.createReusingIndent(indent, addIndent, 0);
			}

			StringBuilder buf = new StringBuilder(c.text + indent);
			int contentStart = findEndOfWhiteSpace(d, c.offset, lineEnd);
			c.length =  Math.max(contentStart - c.offset, 0);

			// insert closing brace on new line after an unclosed opening brace
			if (getBracketCount(d, start, c.offset, true) > 0 && fCloseBrace && !isClosedBrace(d, c.offset, c.length)) {
				c.caretOffset = c.offset + buf.length();
				c.shiftsCaret = false;

				// copy old content of line behind insertion point to new line
				// unless we think we are inserting an anonymous type definition
				if (c.offset == 0 || !(computeAnonymousPosition(d, c.offset - 1, fPartitioning, lineEnd) != -1)) {
					if (lineEnd - contentStart > 0) {
						c.length =  lineEnd - c.offset;
						buf.append(d.get(contentStart, lineEnd - contentStart).toCharArray());
					}
				}

				buf.append(TextUtilities.getDefaultLineDelimiter(d));
				StringBuilder reference = null;
				int nonWS = findEndOfWhiteSpace(d, start, lineEnd);
				if (nonWS < c.offset && d.getChar(nonWS) == '{')
					reference = new StringBuilder(d.get(start, nonWS - start));
				else
					reference = indenter.getReferenceIndentation(c.offset);
				if (reference != null)
					buf.append(reference);
				buf.append('}');
				int bound= c.offset > 200 ? c.offset - 200 : STPHeuristicScanner.UNBOUND;
				int bracePos = scanner.findOpeningPeer(c.offset - 1, bound, '{', '}');
				if (bracePos != STPHeuristicScanner.NOT_FOUND) {
					if (scanner.looksLikeCompositeTypeDefinitionBackward(bracePos, bound) ||
							scanner.previousToken(bracePos - 1, bound) == STPSymbols.TokenEQUAL) {
						buf.append(';');
					}
				}
			}
			// insert extra line upon new line between two braces
			else if (c.offset > start && contentStart < lineEnd && d.getChar(contentStart) == '}') {
				int firstCharPos = scanner.findNonWhitespaceBackward(c.offset - 1, start);
				if (firstCharPos != STPHeuristicScanner.NOT_FOUND && d.getChar(firstCharPos) == '{') {
					c.caretOffset = c.offset + buf.length();
					c.shiftsCaret = false;

					StringBuilder reference = null;
					int nonWS = findEndOfWhiteSpace(d, start, lineEnd);
					if (nonWS < c.offset && d.getChar(nonWS) == '{')
						reference = new StringBuilder(d.get(start, nonWS - start));
					else
						reference = indenter.getReferenceIndentation(c.offset);

					buf.append(TextUtilities.getDefaultLineDelimiter(d));

					if (reference != null)
						buf.append(reference);
				}
			}
			c.text = buf.toString();

		} catch (BadLocationException e) {
			IDEPlugin.log(e);
		}
	}

	private void smartIndentUponE(IDocument doc, DocumentCommand c) {
		if (c.offset < 4 || doc.getLength() == 0)
			return;

		try {
			String content = doc.get(c.offset - 3, 3);
			if (content.equals("els")) { //$NON-NLS-1$
				STPHeuristicScanner scanner = new STPHeuristicScanner(doc);
				int p = c.offset - 3;

				// current line
				int line = doc.getLineOfOffset(p);
				int lineOffset = doc.getLineOffset(line);

				// make sure we don't have any leading comments etc.
				if (doc.get(lineOffset, p - lineOffset).trim().length() != 0)
					return;

				// Line of last C code
				int pos = scanner.findNonWhitespaceBackward(p - 1, STPHeuristicScanner.UNBOUND);
				if (pos == -1)
					return;
				int lastLine = doc.getLineOfOffset(pos);

				// Only shift if the last C line is further up and is a braceless block candidate
				if (lastLine < line) {
					STPIndenter indenter = new STPIndenter(doc, scanner, fProject);
					int ref = indenter.findReferencePosition(p, true, MatchMode.REGULAR);
					if (ref == STPHeuristicScanner.NOT_FOUND)
						return;
					int refLine = doc.getLineOfOffset(ref);
					String indent = getIndentOfLine(doc, refLine);

					if (indent != null) {
						c.text = indent + "else"; //$NON-NLS-1$
						c.length += c.offset - lineOffset;
						c.offset = lineOffset;
					}
				}

				return;
			}

			if (content.equals("cas")) { //$NON-NLS-1$
				STPHeuristicScanner scanner = new STPHeuristicScanner(doc);
				int p = c.offset - 3;

				// current line
				int line = doc.getLineOfOffset(p);
				int lineOffset = doc.getLineOffset(line);

				// make sure we don't have any leading comments etc.
				if (doc.get(lineOffset, p - lineOffset).trim().length() != 0)
					return;

				// Line of last C code
				int pos = scanner.findNonWhitespaceBackward(p - 1, STPHeuristicScanner.UNBOUND);
				if (pos == -1)
					return;
				int lastLine = doc.getLineOfOffset(pos);

				// Only shift if the last C line is further up and is a braceless block candidate
				if (lastLine < line) {
					STPIndenter indenter = new STPIndenter(doc, scanner, fProject);
					int ref = indenter.findReferencePosition(p, false, MatchMode.MATCH_CASE);
					if (ref == STPHeuristicScanner.NOT_FOUND)
						return;
					int refLine = doc.getLineOfOffset(ref);
					int nextToken = scanner.nextToken(ref, STPHeuristicScanner.UNBOUND);
					String indent;
					if (nextToken == STPSymbols.TokenCASE || nextToken == STPSymbols.TokenDEFAULT)
						indent = getIndentOfLine(doc, refLine);
					else // at the brace of the switch
						indent = indenter.computeIndentation(p).toString();

					if (indent != null) {
						c.text = indent.toString() + "case"; //$NON-NLS-1$
						c.length += c.offset - lineOffset;
						c.offset = lineOffset;
					}
				}

				return;
			}
		} catch (BadLocationException e) {
			IDEPlugin.log(e);
		}
	}
	/**
	 * Computes an insert position for an opening brace if <code>offset</code> maps to a position in
	 * <code>document</code> with a expression in parenthesis that will take a block after the closing parenthesis.
	 *
	 * @param document the document being modified
	 * @param offset the offset of the caret position, relative to the line start.
	 * @param partitioning the document partitioning
	 * @param max the max position
	 * @return an insert position relative to the line start if <code>line</code> contains a parenthesized expression that can be followed by a block, -1 otherwise
	 */
	private static int computeAnonymousPosition(IDocument document, int offset, String partitioning,  int max) {
		// find the opening parenthesis for every closing parenthesis on the current line after offset
		// return the position behind the closing parenthesis if it looks like a method declaration
		// or an expression for an if, while, for, catch statement

		STPHeuristicScanner scanner = new STPHeuristicScanner(document);
		int pos = offset;
		int length = max;
		int scanTo = scanner.scanForward(pos, length, '}');
		if (scanTo == -1)
			scanTo = length;

		int closingParen = findClosingParenToLeft(scanner, pos) - 1;

		while (true) {
			int startScan = closingParen + 1;
			closingParen = scanner.scanForward(startScan, scanTo, ')');
			if (closingParen == -1)
				break;

			int openingParen = scanner.findOpeningPeer(closingParen - 1, '(', ')');

			// no way an expression at the beginning of the document can mean anything
			if (openingParen < 1)
				break;

			// only select insert positions for parenthesis currently embracing the caret
			if (openingParen > pos)
				continue;
		}

		return -1;
	}

	/**
	 * Finds a closing parenthesis to the left of <code>position</code> in document, where that parenthesis is only
	 * separated by whitespace from <code>position</code>. If no such parenthesis can be found, <code>position</code> is returned.
	 *
	 * @param scanner the C heuristic scanner set up on the document
	 * @param position the first character position in <code>document</code> to be considered
	 * @return the position of a closing parenthesis left to <code>position</code> separated only by whitespace, or <code>position</code> if no parenthesis can be found
	 */
	private static int findClosingParenToLeft(STPHeuristicScanner scanner, int position) {
		if (position < 1)
			return position;

		if (scanner.previousToken(position - 1, STPHeuristicScanner.UNBOUND) == STPSymbols.TokenRPAREN)
			return scanner.getPosition() + 1;
		return position;
	}

	private boolean isClosedBrace(IDocument document, int offset, int length) {
		return getBlockBalance(document, offset, fPartitioning) <= 0;
	}

	private void smartIndentOnKeypress(IDocument document, DocumentCommand command) {
		switch (command.text.charAt(0)) {
			case '}':
				smartIndentAfterClosingBracket(document, command);
				break;
			case '{':
				smartIndentAfterOpeningBracket(document, command);
				break;
			case 'e':
				smartIndentUponE(document, command);
				break;
			case '#':
				smartIndentAfterHash(document, command);
				break;
			case '\"':
				smartInsertCloseChar(document, command, '\"');
				break;
			case ')':
				smartInsertCloseChar(document, command, ')');
				break;
			case ']':
				smartInsertCloseChar(document, command, ']');
				break;
		}
	}

	/**
	 * A closing char (e.g. right paren ')') is being inserted.  If one already exists
	 * at current location in the document, skip over it and don't do an
	 * insert.
	 *
	 * @param d - document
	 * @param c - insert text
	 * @param ch - closing char being inserted
	 */
	private void smartInsertCloseChar(IDocument d, DocumentCommand c, char ch) {
		if (c.offset < 1 || d.getLength() == 0)
			return;

		try {
			if (d.getChar(c.offset) == ch) {
				int backslashCount = 0;
				int prevOffset = c.offset - 1;
				// Look backwards for backslashes.  We will ignore if there are even number
				while (prevOffset > 0 && d.getChar(prevOffset) == '\\') {
					--prevOffset;
					++backslashCount;
				}
				if ((backslashCount & 1) == 0) {
					c.text = ""; //$NON-NLS-1$
					c.offset++;
				}
			}
		} catch (BadLocationException e) {
			IDEPlugin.log(e);
		}

	}

	private void smartIndentAfterHash(IDocument doc, DocumentCommand c) {
		try {
			ITypedRegion partition= TextUtilities.getPartition(doc, fPartitioning, c.offset, false);
			if (IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())) {
				IRegion startLine= doc.getLineInformationOfOffset(c.offset);
				String indent= doc.get(startLine.getOffset(), c.offset - startLine.getOffset());
				if (indent.trim().length() == 0) {
					c.offset -= indent.length();
					c.length += indent.length();
				}
			}
		} catch (BadLocationException e) {
			IDEPlugin.log(e);
		}
	}

	private void smartIndentAfterOpeningBracket(IDocument d, DocumentCommand c) {
		if (c.offset < 1 || d.getLength() == 0)
			return;

		int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);

		try {
			STPHeuristicScanner scanner= new STPHeuristicScanner(d);
			ITypedRegion partition= TextUtilities.getPartition(d, fPartitioning, p, false);
			if (STPPartitionScanner.STP_CONDITIONAL.equals(partition.getType())) {
				scanner = new STPHeuristicScanner(d, fPartitioning, STPPartitionScanner.STP_CONDITIONAL);
			}
			// current line
			int line = d.getLineOfOffset(c.offset);
			int lineOffset = d.getLineOffset(line);

			// make sure we don't have any leading comments etc.
			if (!d.get(lineOffset, c.offset - lineOffset).trim().isEmpty())
				return;

			// Line of last C code
			int pos = scanner.findNonWhitespaceBackward(p, STPHeuristicScanner.UNBOUND);
			if (pos == -1)
				return;
			int lastLine = d.getLineOfOffset(pos);

			// Only shift if the last C line is further up and is a braceless block candidate
			if (lastLine < line) {
				STPIndenter indenter = new STPIndenter(d, scanner, fProject);
				StringBuilder indent = indenter.computeIndentation(p, true);
				String toDelete = d.get(lineOffset, c.offset - lineOffset);
				if (indent != null && !indent.toString().equals(toDelete)) {
					c.text = indent.append(c.text).toString();
					c.length += c.offset - lineOffset;
					c.offset = lineOffset;
				}
			}
		} catch (BadLocationException e) {
			IDEPlugin.log(e);
		}
	}

	private boolean isLineDelimiter(IDocument document, String text) {
		String[] delimiters = document.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.equals(delimiters, text) > -1;
			return false;
	}

	private int getBracketCount(IDocument d, int start, int end, boolean ignoreCloseBrackets) throws BadLocationException {
		int bracketcount = 0;
		while (start < end) {
			char curr = d.getChar(start);
			start++;
			switch (curr) {
				case '#' :
					if (start < end) {
						// '#'-comment: nothing to do anymore on this line
						start = end;
					}
					break;
				case '/' :
					if (start < end) {
						char next = d.getChar(start);
						if (next == '*') {
							// a comment starts, advance to the comment end
							start = getCommentEnd(d, start + 1, end);
						} else if (next == '/') {
							// '//'-comment: nothing to do anymore on this line
							start = end;
						}
					}
					break;
				case '*' :
					if (start < end) {
						char next = d.getChar(start);
						if (next == '/') {
							// we have been in a comment: forget what we read before
							bracketcount = 0;
							start++;
						}
					}
					break;
				case '{' :
					bracketcount++;
					ignoreCloseBrackets = false;
					break;
				case '}' :
					if (!ignoreCloseBrackets) {
						bracketcount--;
					}
					break;
				case '"' :
				case '\'' :
					start = getStringEnd(d, start, end, curr);
					break;
				default :
			}
		}
		return bracketcount;
	}


	// ----------- bracket counting ------------------------------------------------------

	private int getCommentEnd(IDocument d, int pos, int end) throws BadLocationException {
		while (pos < end) {
			char curr = d.getChar(pos);
			pos++;
			if (curr == '*') {
				if (pos < end && d.getChar(pos) == '/') {
					return pos + 1;
				}
			}
		}
		return end;
	}

	private String getIndentOfLine(IDocument d, int line) throws BadLocationException {
		if (line > -1) {
			int start = d.getLineOffset(line);
			int end = start + d.getLineLength(line) - 1;
			int whiteend = findEndOfWhiteSpace(d, start, end);
			return d.get(start, whiteend - start);
		}
		return ""; //$NON-NLS-1$
	}

	private int getStringEnd(IDocument d, int pos, int end, char ch) throws BadLocationException {
		while (pos < end) {
			char curr = d.getChar(pos);
			pos++;
			if (curr == '\\') {
				// ignore escaped characters
				pos++;
			} else if (curr == ch) {
				return pos;
			}
		}
		return end;
	}
	private void smartIndentAfterClosingBracket(IDocument d, DocumentCommand c) {
		if (c.offset == -1 || d.getLength() == 0)
			return;

		try {
			int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);
			int line = d.getLineOfOffset(p);
			int start = d.getLineOffset(line);
			int whiteend = findEndOfWhiteSpace(d, start, c.offset);

			STPHeuristicScanner scanner= new STPHeuristicScanner(d);
			ITypedRegion partition= TextUtilities.getPartition(d, fPartitioning, p, false);
			if (STPPartitionScanner.STP_CONDITIONAL.equals(partition.getType())) {
				scanner = new STPHeuristicScanner(d, fPartitioning, STPPartitionScanner.STP_CONDITIONAL);
			}
			STPIndenter indenter = new STPIndenter(d, scanner, fProject);

			// shift only when line does not contain any text up to the closing bracket
			if (whiteend == c.offset) {
				// evaluate the line with the opening bracket that matches out closing bracket
				int reference = indenter.findReferencePosition(c.offset, false, MatchMode.MATCH_BRACE);
				int indLine = d.getLineOfOffset(reference);
				if (indLine != -1 && indLine != line) {
					// take the indent of the found line
					StringBuilder replaceText = new StringBuilder(getIndentOfLine(d, indLine));
					// add the rest of the current line including the just added close bracket
					replaceText.append(d.get(whiteend, c.offset - whiteend));
					replaceText.append(c.text);
					// modify document command
					c.length += c.offset - start;
					c.offset = start;
					c.text = replaceText.toString();
				}
			}
		} catch (BadLocationException e) {
			IDEPlugin.log(e);
		}
	}

	/**
	 * Installs a C partitioner with <code>document</code>.
	 *
	 * @param document the document
	 */
	private static void installPartitioner(Document document) {
		String[] types= new String[] {
				IDocument.DEFAULT_CONTENT_TYPE,
				STPPartitionScanner.STP_COMMENT,
				STPPartitionScanner.STP_CONDITIONAL,
		};
		FastPartitioner partitioner= new FastPartitioner(new STPPartitionScanner(), types);
		partitioner.connect(document);
		document.setDocumentPartitioner(STPPartitionScanner.STP_PARTITIONING, partitioner);
	}

	/**
	 * Installs a C partitioner with <code>document</code>.
	 *
	 * @param document the document
	 */
	private static void removePartitioner(Document document) {
		document.setDocumentPartitioner(STPPartitionScanner.STP_PARTITIONING, null);
	}

	private void smartPaste(IDocument document, DocumentCommand command) {
		int newOffset= command.offset;
		int newLength= command.length;
		String newText= command.text;

		try {
			STPHeuristicScanner scanner= new STPHeuristicScanner(document);
			STPIndenter indenter= new STPIndenter(document, scanner, fProject);
			int offset= newOffset;

			// reference position to get the indent from
			int refOffset= indenter.findReferencePosition(offset);
			if (refOffset == STPHeuristicScanner.NOT_FOUND)
				return;
			int peerOffset= getPeerPosition(document, command);
			peerOffset= indenter.findReferencePosition(peerOffset);
			if (peerOffset == STPHeuristicScanner.NOT_FOUND)
				return;
			refOffset= Math.min(refOffset, peerOffset);

			// eat any WS before the insertion to the beginning of the line
			int firstLine= 1; // don't format the first line per default, as it has other content before it
			IRegion line= document.getLineInformationOfOffset(offset);
			String notSelected= document.get(line.getOffset(), offset - line.getOffset());
			if (notSelected.trim().length() == 0) {
				newLength += notSelected.length();
				newOffset= line.getOffset();
				firstLine= 0;
			}

			// Prefix: the part we need for formatting but won't paste.
			// Take up to 100 previous lines to preserve enough context.
			int firstPrefixLine= Math.max(document.getLineOfOffset(refOffset) - 100, 0);
			int prefixOffset= document.getLineInformation(firstPrefixLine).getOffset();
			String prefix= document.get(prefixOffset, newOffset - prefixOffset);

			// Handle the indentation computation inside a temporary document
			Document temp= new Document(prefix + newText);
			DocumentRewriteSession session= temp.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
			scanner= new STPHeuristicScanner(temp);
			indenter= new STPIndenter(temp, scanner, fProject);
			installPartitioner(temp);

			// Indent the first and second line
			// compute the relative indentation difference from the second line
			// (as the first might be partially selected) and use the value to
			// indent all other lines.
			boolean isIndentDetected= false;
			StringBuilder addition= new StringBuilder();
			int insertLength= 0;
			int first= document.computeNumberOfLines(prefix) + firstLine; // don't format first line
			int lines= temp.getNumberOfLines();
			boolean changed= false;
			boolean indentInsideLineComments= IndentUtil.indentInsideLineComments(fProject);

			for (int l= first; l < lines; l++) { // we don't change the number of lines while adding indents
				IRegion r= temp.getLineInformation(l);
				int lineOffset= r.getOffset();
				int lineLength= r.getLength();

				if (lineLength == 0) // don't modify empty lines
					continue;

				if (!isIndentDetected) {
					// indent the first pasted line
					String current= IndentUtil.getCurrentIndent(temp, l, indentInsideLineComments);
					StringBuilder correct= new StringBuilder(IndentUtil.computeIndent(temp, l, indenter, scanner));

					insertLength= subtractIndent(correct, current, addition);
					// workaround for bug 181139
					if (/*l != first && */temp.get(lineOffset, lineLength).trim().length() != 0) {
						isIndentDetected= true;
						if (insertLength == 0) {
							 // no adjustment needed, bail out
							if (firstLine == 0) {
								// but we still need to adjust the first line
								command.offset= newOffset;
								command.length= newLength;
								if (changed)
									break; // still need to get the leading indent of the first line
							}
							return;
						}
						removePartitioner(temp);
					} else {
						changed= insertLength != 0;
					}
				}

				// relatively indent all pasted lines
				if (insertLength > 0)
					addIndent(temp, l, addition, indentInsideLineComments);
				else if (insertLength < 0)
					cutIndent(temp, l, -insertLength, indentInsideLineComments);
			}

			temp.stopRewriteSession(session);
			newText= temp.get(prefix.length(), temp.getLength() - prefix.length());

			command.offset= newOffset;
			command.length= newLength;
			command.text= newText;
		} catch (BadLocationException e) {
			IDEPlugin.log(e);
		}
	}
	/**
	 * Computes the difference of two indentations and returns the difference in
	 * length of current and correct. If the return value is positive, <code>addition</code>
	 * is initialized with a substring of that length of <code>correct</code>.
	 *
	 * @param correct the correct indentation
	 * @param current the current indentation (migth contain non-whitespace)
	 * @param difference a string buffer - if the return value is positive, it will be cleared and set to the substring of <code>current</code> of that length
	 * @return the difference in lenght of <code>correct</code> and <code>current</code>
	 */
	private int subtractIndent(CharSequence correct, CharSequence current, StringBuilder difference) {
		int c1= computeVisualLength(correct);
		int c2= computeVisualLength(current);
		int diff= c1 - c2;
		if (diff <= 0)
			return diff;

		difference.setLength(0);
		int len= 0, i= 0;
		while (len < diff) {
			char c= correct.charAt(i++);
			difference.append(c);
			len += computeVisualLength(c);
		}

		return diff;
	}

	/**
	 * Indents line <code>line</code> in <code>document</code> with <code>indent</code>.
	 * Leaves leading comment signs alone.
	 *
	 * @param document the document
	 * @param line the line
	 * @param indent the indentation to insert
	 * @param indentInsideLineComments option whether to indent inside line comments starting at column 0
	 * @throws BadLocationException on concurrent document modification
	 */
	private static void addIndent(Document document, int line, CharSequence indent, boolean indentInsideLineComments) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int insert= region.getOffset();
		int endOffset= region.getOffset() + region.getLength();

		if (indentInsideLineComments) {
			// go behind line comments
			while (insert < endOffset - 2 && document.get(insert, 2).equals(LINE_COMMENT))
				insert += 2;
		}

		// insert indent
		document.replace(insert, 0, indent.toString());
	}

	/**
	 * Cuts the visual equivalent of <code>toDelete</code> characters out of the
	 * indentation of line <code>line</code> in <code>document</code>. Leaves
	 * leading comment signs alone.
	 *
	 * @param document the document
	 * @param line the line
	 * @param toDelete the number of space equivalents to delete.
	 * @param indentInsideLineComments option whether to indent inside line comments starting at column 0
	 * @throws BadLocationException on concurrent document modification
	 */
	private void cutIndent(Document document, int line, int toDelete, boolean indentInsideLineComments) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int from= region.getOffset();
		int endOffset= region.getOffset() + region.getLength();

		if (indentInsideLineComments) {
			// go behind line comments
			while (from < endOffset - 2 && document.get(from, 2).equals(LINE_COMMENT))
				from += 2;
		}

		int to= from;
		while (toDelete > 0 && to < endOffset) {
			char ch= document.getChar(to);
			if (!Character.isWhitespace(ch))
				break;
			toDelete -= computeVisualLength(ch);
			if (toDelete >= 0)
				to++;
			else
				break;
		}

		document.replace(from, to - from, null);
	}

	/**
	 * Returns the visual length of a given <code>CharSequence</code> taking into
	 * account the visual tabulator length.
	 *
	 * @param seq the string to measure
	 * @return the visual length of <code>seq</code>
	 */
	private int computeVisualLength(CharSequence seq) {
		int size= 0;
		int tablen= getVisualTabLengthPreference();

		for (int i= 0; i < seq.length(); i++) {
			char ch= seq.charAt(i);
			if (ch == '\t') {
				if (tablen != 0)
					size += tablen - size % tablen;
				// else: size stays the same
			} else {
				size++;
			}
		}
		return size;
	}

	/**
	 * Returns the visual length of a given character taking into
	 * account the visual tabulator length.
	 *
	 * @param ch the character to measure
	 * @return the visual length of <code>ch</code>
	 */
	private int computeVisualLength(char ch) {
		if (ch == '\t')
			return getVisualTabLengthPreference();
		return 1;
	}

	/**
	 * The preference setting for the visual tabulator display.
	 *
	 * @return the number of spaces displayed for a tabulator in the editor
	 */
	private int getVisualTabLengthPreference() {
		return CodeFormatterUtil.getTabWidth(fProject);
	}

	private int getPeerPosition(IDocument document, DocumentCommand command) {
		if (document.getLength() == 0)
			return 0;
    	/*
    	 * Search for scope closers in the pasted text and find their opening peers
    	 * in the document.
    	 */
    	Document pasted= new Document(command.text);
    	installPartitioner(pasted);
    	int firstPeer= command.offset;

    	STPHeuristicScanner pScanner= new STPHeuristicScanner(pasted);
    	STPHeuristicScanner dScanner= new STPHeuristicScanner(document);

    	// add scope relevant after context to peer search
    	int afterToken= dScanner.nextToken(command.offset + command.length, STPHeuristicScanner.UNBOUND);
    	try {
			switch (afterToken) {
			case STPSymbols.TokenRBRACE:
				pasted.replace(pasted.getLength(), 0, "}"); //$NON-NLS-1$
				break;
			case STPSymbols.TokenRPAREN:
				pasted.replace(pasted.getLength(), 0, ")"); //$NON-NLS-1$
				break;
			case STPSymbols.TokenRBRACKET:
				pasted.replace(pasted.getLength(), 0, "]"); //$NON-NLS-1$
				break;
			}
		} catch (BadLocationException e) {
			// cannot happen
			Assert.isTrue(false);
		}

    	int pPos= 0; // paste text position (increasing from 0)
    	int dPos= Math.max(0, command.offset - 1); // document position (decreasing from paste offset)
    	while (true) {
    		int token= pScanner.nextToken(pPos, STPHeuristicScanner.UNBOUND);
   			pPos= pScanner.getPosition();
    		switch (token) {
    			case STPSymbols.TokenLBRACE:
    			case STPSymbols.TokenLBRACKET:
    			case STPSymbols.TokenLPAREN:
    				pPos= skipScope(pScanner, pPos, token);
    				if (pPos == STPHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				break; // closed scope -> keep searching
    			case STPSymbols.TokenRBRACE:
    				int peer= dScanner.findOpeningPeer(dPos, '{', '}');
    				dPos= peer - 1;
    				if (peer == STPHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    			case STPSymbols.TokenRBRACKET:
    				peer= dScanner.findOpeningPeer(dPos, '[', ']');
    				dPos= peer - 1;
    				if (peer == STPHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    			case STPSymbols.TokenRPAREN:
    				peer= dScanner.findOpeningPeer(dPos, '(', ')');
    				dPos= peer - 1;
    				if (peer == STPHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching

    			case STPSymbols.TokenCASE:
    			case STPSymbols.TokenDEFAULT:
    			    {
    					STPIndenter indenter= new STPIndenter(document, dScanner, fProject);
    					peer= indenter.findReferencePosition(dPos, false, MatchMode.MATCH_CASE);
    					if (peer == STPHeuristicScanner.NOT_FOUND)
    						return firstPeer;
    					firstPeer= peer;
    				}
    				break; // keep searching

    			case STPSymbols.TokenPUBLIC:
    			case STPSymbols.TokenPROTECTED:
    			case STPSymbols.TokenPRIVATE:
				    {
						STPIndenter indenter= new STPIndenter(document, dScanner, fProject);
						peer= indenter.findReferencePosition(dPos, false, MatchMode.MATCH_ACCESS_SPECIFIER);
						if (peer == STPHeuristicScanner.NOT_FOUND)
							return firstPeer;
						firstPeer= peer;
					}
    				break; // keep searching

    			case STPSymbols.TokenEOF:
    				return firstPeer;
    			default:
    				// keep searching
    		}
    	}
    }
    /**
     * Skips the scope opened by <code>token</code> in <code>document</code>,
     * returns either the position of the
     * @param pos
     * @param token
     * @return the position after the scope
     */
    private static int skipScope(STPHeuristicScanner scanner, int pos, int token) {
    	int openToken= token;
    	int closeToken;
    	switch (token) {
    		case STPSymbols.TokenLPAREN:
    			closeToken= STPSymbols.TokenRPAREN;
    			break;
    		case STPSymbols.TokenLBRACKET:
    			closeToken= STPSymbols.TokenRBRACKET;
    			break;
    		case STPSymbols.TokenLBRACE:
    			closeToken= STPSymbols.TokenRBRACE;
    			break;
    		default:
    			Assert.isTrue(false);
    			return -1; // dummy
    	}

    	int depth= 1;
    	int p= pos;

    	while (true) {
    		int tok= scanner.nextToken(p, STPHeuristicScanner.UNBOUND);
    		p= scanner.getPosition();

    		if (tok == openToken) {
    			depth++;
    		} else if (tok == closeToken) {
    			depth--;
    			if (depth == 0)
    				return p + 1;
    		} else if (tok == STPSymbols.TokenEOF) {
    			return STPHeuristicScanner.NOT_FOUND;
    		}
    	}
    }
}