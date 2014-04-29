/*******************************************************************************
 * Copyright (c) 2005, 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *     Red Hat Inc. - modified for use in SystemTap
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ILineRange;


/**
 * Utility that indents a number of lines in a document.
 */
public final class IndentUtil {

    private static final String SLASHES= "//"; //$NON-NLS-1$

    /**
     * The result of an indentation operation. The result may be passed to
     * subsequent calls to
     * {@link IndentUtil#indentLines(IDocument, ILineRange, IProject, IndentUtil.IndentResult) indentLines}
     * to obtain consistent results with respect to the indentation of
     * line-comments.
     */
    public static final class IndentResult {
        private IndentResult(boolean[] commentLines) {
            commentLinesAtColumnZero= commentLines;
        }
        private boolean[] commentLinesAtColumnZero;
    }

    private IndentUtil() {
        // do not instantiate
    }

    /**
     * Indents the line range specified by <code>lines</code> in
     * <code>document</code>. The passed C project may be
     * <code>null</code>, it is used solely to obtain formatter preferences.
     *
     * @param document the document to be changed
     * @param lines the line range to be indented
     * @param project the C project to get the formatter preferences from, or
     *        <code>null</code> if global preferences should be used
     * @param result the result from a previous call to <code>indentLines</code>,
     *        in order to maintain comment line properties, or <code>null</code>.
     *        Note that the passed result may be changed by the call.
     * @return an indent result that may be queried for changes and can be
     *         reused in subsequent indentation operations
     * @throws BadLocationException if <code>lines</code> is not a valid line
     *         range on <code>document</code>
     */
    public static IndentResult indentLines(IDocument document, ILineRange lines, IProject project, IndentResult result) throws BadLocationException {
        int numberOfLines= lines.getNumberOfLines();

        if (numberOfLines < 1)
            return new IndentResult(null);

        result= reuseOrCreateToken(result, numberOfLines);

        STPHeuristicScanner scanner= new STPHeuristicScanner(document);
        STPIndenter indenter= new STPIndenter(document, scanner, project);
        boolean indentInsideLineComments= true;
        for (int line= lines.getStartLine(), last= line + numberOfLines, i= 0; line < last; line++) {
            indentLine(document, line, indenter, scanner, result.commentLinesAtColumnZero, i++, indentInsideLineComments);
        }

        return result;
    }

    /**
     * Returns the indentation of the line <code>line</code> in <code>document</code>.
     * The returned string may contain pairs of leading slashes that are considered
     * part of the indentation.
     *
     * @param document the document
     * @param line the line
     * @param indentInsideLineComments  option whether to indent inside line comments starting at column 0
     * @return the indentation of <code>line</code> in <code>document</code>
     * @throws BadLocationException if the document is changed concurrently
     */
    public static String getCurrentIndent(IDocument document, int line, boolean indentInsideLineComments) throws BadLocationException {
        IRegion region= document.getLineInformation(line);
        int from= region.getOffset();
        int endOffset= region.getOffset() + region.getLength();

        int to= from;
        if (indentInsideLineComments) {
            // go behind line comments
            while (to < endOffset - 2 && document.get(to, 2).equals(SLASHES))
                to += 2;
        }

        while (to < endOffset) {
            char ch= document.getChar(to);
            if (!Character.isWhitespace(ch))
                break;
            to++;
        }

        return document.get(from, to - from);
    }

    private static IndentResult reuseOrCreateToken(IndentResult token, int numberOfLines) {
        if (token == null)
            token= new IndentResult(new boolean[numberOfLines]);
        else if (token.commentLinesAtColumnZero == null)
            token.commentLinesAtColumnZero= new boolean[numberOfLines];
        else if (token.commentLinesAtColumnZero.length != numberOfLines) {
            boolean[] commentBooleans= new boolean[numberOfLines];
            System.arraycopy(token.commentLinesAtColumnZero, 0, commentBooleans, 0, Math.min(numberOfLines, token.commentLinesAtColumnZero.length));
            token.commentLinesAtColumnZero= commentBooleans;
        }
        return token;
    }

    /**
     * Indents a single line using the heuristic scanner. Multiline comments are
     * indented as specified by the <code>CCommentAutoIndentStrategy</code>.
     *
     * @param document the document
     * @param line the line to be indented
     * @param indenter the C indenter
     * @param scanner the heuristic scanner
     * @param commentLines the indent token comment booleans
     * @param lineIndex the zero-based line index
     * @param indentInsideLineComments option whether to indent inside line comments
     *             starting at column 0
     * @throws BadLocationException if the document got changed concurrently
     */
    private static void indentLine(IDocument document, int line, STPIndenter indenter,
            STPHeuristicScanner scanner, boolean[] commentLines, int lineIndex,
            boolean indentInsideLineComments) throws BadLocationException {
        IRegion currentLine= document.getLineInformation(line);
        final int offset= currentLine.getOffset();
        int wsStart= offset; // where we start searching for non-WS; after the "//" in single line comments

        String indent= null;
        if (offset < document.getLength()) {
            ITypedRegion partition= TextUtilities.getPartition(document, STPPartitionScanner.STP_PARTITIONING, offset, true);
            ITypedRegion startingPartition= TextUtilities.getPartition(document, STPPartitionScanner.STP_PARTITIONING, offset, false);
            String type= partition.getType();
            if (type.equals(STPPartitionScanner.STP_MULTILINE_COMMENT)) {
                indent= computeCommentIndent(document, line, scanner, startingPartition);
            } else if (startingPartition.getType().equals(STPPartitionScanner.STP_CONDITIONAL)) {
                indent= computePreprocessorIndent(document, line, startingPartition);
            } else if (!commentLines[lineIndex] && startingPartition.getOffset() == offset && startingPartition.getType().equals(STPPartitionScanner.STP_COMMENT)) {
                return;
            }
        }

        // standard C code indentation
        if (indent == null) {
            StringBuilder computed= indenter.computeIndentation(offset);
            if (computed != null)
                indent= computed.toString();
            else
                indent= ""; //$NON-NLS-1$
        }

        // change document:
        // get current white space
        int lineLength= currentLine.getLength();
        int end= scanner.findNonWhitespaceForwardInAnyPartition(wsStart, offset + lineLength);
        if (end == STPHeuristicScanner.NOT_FOUND)
            end= offset + lineLength;
        int length= end - offset;
        String currentIndent= document.get(offset, length);

        // memorize the fact that a line is a single line comment (but not at column 0) and should be treated like code
        // as opposed to commented out code, which should keep its slashes at column 0
        // if 'indentInsideLineComments' is false, all comment lines are indented with the code
        if (length > 0 || !indentInsideLineComments) {
            ITypedRegion partition= TextUtilities.getPartition(document, STPPartitionScanner.STP_PARTITIONING, end, false);
            if (partition.getOffset() == end && STPPartitionScanner.STP_COMMENT.equals(partition.getType())) {
                commentLines[lineIndex]= true;
            }
        }

        // only change the document if it is a real change
        if (!indent.equals(currentIndent)) {
            document.replace(offset, length, indent);
        }
    }

    /**
     * Computes and returns the indentation for a source line.
     *
     * @param document the document
     * @param line the line in document
     * @param indenter the C indenter
     * @param scanner the scanner
     * @return the indent, never <code>null</code>
     * @throws BadLocationException
     */
    public static String computeIndent(IDocument document, int line, STPIndenter indenter, STPHeuristicScanner scanner) throws BadLocationException {
        IRegion currentLine= document.getLineInformation(line);
        final int offset= currentLine.getOffset();

        String indent= null;
        if (offset < document.getLength()) {
            ITypedRegion partition= TextUtilities.getPartition(document, STPPartitionScanner.STP_PARTITIONING, offset, true);
            ITypedRegion startingPartition= TextUtilities.getPartition(document, STPPartitionScanner.STP_PARTITIONING, offset, false);
            String type= partition.getType();
            if (type.equals(STPPartitionScanner.STP_COMMENT)) {
                indent= computeCommentIndent(document, line, scanner, startingPartition);
            } else if (startingPartition.getType().equals(STPPartitionScanner.STP_CONDITIONAL)) {
                indent= computePreprocessorIndent(document, line, startingPartition);
            }
        }

        // standard C code indentation
        if (indent == null) {
            StringBuilder computed= indenter.computeIndentation(offset);
            if (computed != null) {
                indent= computed.toString();
            } else {
                indent= ""; //$NON-NLS-1$
            }
        }
        return indent;
    }

    /**
     * Computes and returns the indentation for a block comment line.
     *
     * @param document the document
     * @param line the line in document
     * @param scanner the scanner
     * @param partition the comment partition
     * @return the indent, or <code>null</code> if not computable
     * @throws BadLocationException
     */
    public static String computeCommentIndent(IDocument document, int line, STPHeuristicScanner scanner, ITypedRegion partition) throws BadLocationException {
        if (line == 0) { // impossible - the first line is never inside a comment
            return null;
        }

        // don't make any assumptions if the line does not start with \s*\* - it might be
        // commented out code, for which we don't want to change the indent
        final IRegion lineInfo= document.getLineInformation(line);
        final int lineStart= lineInfo.getOffset();
        final int lineLength= lineInfo.getLength();
        final int lineEnd= lineStart + lineLength;
        int nonWS= scanner.findNonWhitespaceForwardInAnyPartition(lineStart, lineEnd);
        if (nonWS == STPHeuristicScanner.NOT_FOUND || document.getChar(nonWS) != '*') {
            if (nonWS == STPHeuristicScanner.NOT_FOUND) {
                return document.get(lineStart, lineLength);
            }
            return document.get(lineStart, nonWS - lineStart);
        }

        // take the indent from the previous line and reuse
        IRegion previousLine= document.getLineInformation(line - 1);
        int previousLineStart= previousLine.getOffset();
        int previousLineLength= previousLine.getLength();
        int previousLineEnd= previousLineStart + previousLineLength;

        StringBuilder buf= new StringBuilder();
        int previousLineNonWS= scanner.findNonWhitespaceForwardInAnyPartition(previousLineStart, previousLineEnd);
        if (previousLineNonWS == STPHeuristicScanner.NOT_FOUND || document.getChar(previousLineNonWS) != '*') {
            // align with the comment start if the previous line is not an asterix line
            previousLine= document.getLineInformationOfOffset(partition.getOffset());
            previousLineStart= previousLine.getOffset();
            previousLineLength= previousLine.getLength();
            previousLineEnd= previousLineStart + previousLineLength;
            previousLineNonWS= scanner.findNonWhitespaceForwardInAnyPartition(previousLineStart, previousLineEnd);
            if (previousLineNonWS == STPHeuristicScanner.NOT_FOUND) {
                previousLineNonWS= previousLineEnd;
            }

            // add the initial space
            // TODO this may be controlled by a formatter preference in the future
            buf.append(' ');
        }

        String indentation= document.get(previousLineStart, previousLineNonWS - previousLineStart);
        buf.insert(0, indentation);
        return buf.toString();
    }

    /**
     * Computes and returns the indentation for a preprocessor line.
     *
     * @param document the document
     * @param line the line in document
     * @param partition the comment partition
     * @return the indent, or <code>null</code> if not computable
     * @throws BadLocationException
     */
    public static String computePreprocessorIndent(IDocument document, int line, ITypedRegion partition)
            throws BadLocationException {
        int ppFirstLine= document.getLineOfOffset(partition.getOffset());
        if (line == ppFirstLine) {
            return ""; //$NON-NLS-1$
        }
        STPHeuristicScanner ppScanner= new STPHeuristicScanner(document, STPPartitionScanner.STP_CONDITIONAL, partition.getType());
        STPIndenter ppIndenter= new STPIndenter(document, ppScanner);
        if (line == ppFirstLine + 1) {
            return ppIndenter.createReusingIndent(new StringBuilder(), ppIndenter.getContinuationLineIndent(), 0).toString();
        }
        StringBuilder computed= ppIndenter.computeIndentation(document.getLineOffset(line), false);
        if (computed != null) {
            return computed.toString();
        }
        // take the indent from the previous line and reuse
        IRegion previousLine= document.getLineInformation(line - 1);
        int previousLineStart= previousLine.getOffset();
        int previousLineLength= previousLine.getLength();
        int previousLineEnd= previousLineStart + previousLineLength;

        int previousLineNonWS= ppScanner.findNonWhitespaceForwardInAnyPartition(previousLineStart, previousLineEnd);
        String previousIndent= document.get(previousLineStart, previousLineNonWS - previousLineStart);
        computed= new StringBuilder(previousIndent);
        return computed.toString();
    }
}
