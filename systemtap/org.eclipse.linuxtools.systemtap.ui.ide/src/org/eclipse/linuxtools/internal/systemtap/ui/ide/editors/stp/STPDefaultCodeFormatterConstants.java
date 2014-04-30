/*******************************************************************************
 * Copyright (c) 2000, 2011, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Anton Leherbauer (Wind River Systems)
 *     Red Hat Inc. - modified for use in SystemTap
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;


/**
 * Constants used to set up the options of the code formatter.
 *
 * @since 4.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class STPDefaultCodeFormatterConstants {

    /**
     * <pre>
     * FORMATTER / Value to set an option to false.
     * </pre>
     */
    public static final String FALSE = "false"; //$NON-NLS-1$

    /**
     * <pre>
     * FORMATTER / Option for alignment of arguments in method invocation
     *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_arguments_in_method_invocation"
     *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
     *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
     * </pre>
     * @see #createAlignmentValue(boolean, int, int)
     */
    public static final String FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION = IDEPlugin.PLUGIN_ID + ".formatter.alignment_for_arguments_in_method_invocation";     //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option for alignment of expressions in initializer list
     *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_expressions_in_array_initializer"
     *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
     *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
     * </pre>
     * @see #createAlignmentValue(boolean, int, int)
     */
    public static final String FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_INITIALIZER_LIST = IDEPlugin.PLUGIN_ID + ".formatter.alignment_for_expressions_in_array_initializer";     //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option for alignment of parameters in method declaration
     *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_parameters_in_method_declaration"
     *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
     *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
     * </pre>
     * @see #createAlignmentValue(boolean, int, int)
     */
    public static final String FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION = IDEPlugin.PLUGIN_ID + ".formatter.alignment_for_parameters_in_method_declaration"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to position the braces of initializer list
     *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_array_initializer"
     *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
     *     - default:           END_OF_LINE
     * </pre>
     * @see #END_OF_LINE
     * @see #NEXT_LINE
     * @see #NEXT_LINE_SHIFTED
     * @see #NEXT_LINE_ON_WRAP
     */
    public static final String FORMATTER_BRACE_POSITION_FOR_INITIALIZER_LIST = IDEPlugin.PLUGIN_ID + ".formatter.brace_position_for_array_initializer"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to position the braces of a block
     *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_block"
     *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
     *     - default:           END_OF_LINE
     * </pre>
     * @see #END_OF_LINE
     * @see #NEXT_LINE
     * @see #NEXT_LINE_SHIFTED
     * @see #NEXT_LINE_ON_WRAP
     */
    public static final String FORMATTER_BRACE_POSITION_FOR_BLOCK = IDEPlugin.PLUGIN_ID + ".formatter.brace_position_for_block"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to position the braces of a method declaration
     *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_method_declaration"
     *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
     *     - default:           END_OF_LINE
     * </pre>
     * @see #END_OF_LINE
     * @see #NEXT_LINE
     * @see #NEXT_LINE_SHIFTED
     * @see #NEXT_LINE_ON_WRAP
     */
    public static final String FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION = IDEPlugin.PLUGIN_ID + ".formatter.brace_position_for_method_declaration"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to position the braces of a type declaration
     *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_type_declaration"
     *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
     *     - default:           END_OF_LINE
     * </pre>
     * @see #END_OF_LINE
     * @see #NEXT_LINE
     * @see #NEXT_LINE_SHIFTED
     * @see #NEXT_LINE_ON_WRAP
     */
    public static final String FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION = IDEPlugin.PLUGIN_ID + ".formatter.brace_position_for_type_declaration"; //$NON-NLS-1$

    /**
     * <pre>
     * FORMATTER / Option to set the continuation indentation
     *     - option id:         "org.eclipse.cdt.core.formatter.continuation_indentation"
     *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
     *     - default:           "2"
     * </pre>
     */
    public static final String FORMATTER_CONTINUATION_INDENTATION = IDEPlugin.PLUGIN_ID + ".formatter.continuation_indentation"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to indent 'public:', 'protected:', 'private:' access specifiers relative to class declaration.
     *     - option id:         "org.eclipse.cdt.core.formatter.indent_access_specifier_compare_to_type_header"
     *     - possible values:   { TRUE, FALSE }
     *     - default:           FALSE
     * </pre>
     * @see #TRUE
     * @see #FALSE
     */
    public static final String FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER = IDEPlugin.PLUGIN_ID + ".formatter.indent_access_specifier_compare_to_type_header";  //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Number of extra spaces in front of 'public:', 'protected:', 'private:' access specifiers.
     *             Enables fractional indent of access specifiers. Does not affect indentation of body declarations.
     *     - option id:         "org.eclipse.cdt.core.formatter.indent_access_specifier_extra_spaces"
     *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
     *     - default:           "0"
     * </pre>
     * @since 5.2
     */
    public static final String FORMATTER_INDENT_ACCESS_SPECIFIER_EXTRA_SPACES = IDEPlugin.PLUGIN_ID + ".formatter.indent_access_specifier_extra_spaces"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to indent body declarations relative to access specifiers (visibility labels)
     *     - option id:         "org.eclipse.cdt.core.formatter.indent_body_declarations_compare_to_access_specifier"
     *     - possible values:   { TRUE, FALSE }
     *     - default:           TRUE
     * </pre>
     * @see #TRUE
     * @see #FALSE
     */
    public static final String FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER = IDEPlugin.PLUGIN_ID + ".formatter.indent_body_declarations_compare_to_access_specifier"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to indent body declarations compare to its enclosing namespace header
     *     - option id:         "org.eclipse.cdt.core.formatter.indent_body_declarations_compare_to_namespace_header"
     *     - possible values:   { TRUE, FALSE }
     *     - default:           TRUE
     * </pre>
     * @see #TRUE
     * @see #FALSE
     */
    public static final String FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_NAMESPACE_HEADER = IDEPlugin.PLUGIN_ID + ".formatter.indent_body_declarations_compare_to_namespace_header"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to indent statements inside a block
     *     - option id:         "org.eclipse.cdt.core.formatter.indent_statements_compare_to_block"
     *     - possible values:   { TRUE, FALSE }
     *     - default:           TRUE
     * </pre>
     * @see #TRUE
     * @see #FALSE
     */
    public static final String FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BLOCK = IDEPlugin.PLUGIN_ID + ".formatter.indent_statements_compare_to_block"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to indent statements inside the body of a method or a constructor
     *     - option id:         "org.eclipse.cdt.core.formatter.indent_statements_compare_to_body"
     *     - possible values:   { TRUE, FALSE }
     *     - default:           TRUE
     * </pre>
     * @see #TRUE
     * @see #FALSE
     */
    public static final String FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BODY = IDEPlugin.PLUGIN_ID + ".formatter.indent_statements_compare_to_body"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to indent switch statements compare to cases
     *     - option id:         "org.eclipse.cdt.core.formatter.indent_switchstatements_compare_to_cases"
     *     - possible values:   { TRUE, FALSE }
     *     - default:           TRUE
     * </pre>
     * @see #TRUE
     * @see #FALSE
     */
    public static final String FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES = IDEPlugin.PLUGIN_ID + ".formatter.indent_switchstatements_compare_to_cases"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to indent switch statements compare to switch
     *     - option id:         "org.eclipse.cdt.core.formatter.indent_switchstatements_compare_to_switch"
     *     - possible values:   { TRUE, FALSE }
     *     - default:           TRUE
     * </pre>
     * @see #TRUE
     * @see #FALSE
     */
    public static final String FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH = IDEPlugin.PLUGIN_ID + ".formatter.indent_switchstatements_compare_to_switch"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Option to specify the tabulation size
     *     - option id:         "org.eclipse.cdt.core.formatter.tabulation.char"
     *     - possible values:   { TAB, SPACE, MIXED }
     *     - default:           TAB
     * </pre>
     * More values may be added in the future.
     *
     * @see IDEPlugin#TAB
     * @see IDEPlugin#SPACE
     * @see #MIXED
     */
    public static final String FORMATTER_TAB_CHAR = IDEPlugin.PLUGIN_ID + ".formatter.tabulation.char"; //$NON-NLS-1$

    /**
     * <pre>
     * FORMATTER / The wrapping is done by indenting by one compare to the current indentation.
     * </pre>
     */
    public static final int INDENT_BY_ONE= 2;

    /**
     * <pre>
     * FORMATTER / The wrapping is done by using the current indentation.
     * </pre>
     */
    public static final int INDENT_DEFAULT= 0;
    /**
     * <pre>
     * FORMATTER / The wrapping is done by indenting on column under the splitting location.
     * </pre>
     */
    public static final int INDENT_ON_COLUMN = 1;

    /**
     * <pre>
     * FORMATTER / Possible value for the option FORMATTER_TAB_CHAR
     * </pre>
     * @see IDEPlugin#TAB
     * @see IDEPlugin#SPACE
     * @see #FORMATTER_TAB_CHAR
     */
    public static final String MIXED = "mixed"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Value to set a brace location at the start of the next line with
     *             an extra indentation.
     * </pre>
     * @see #FORMATTER_BRACE_POSITION_FOR_INITIALIZER_LIST
     * @see #FORMATTER_BRACE_POSITION_FOR_BLOCK
      * @see #FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION
      * @see #FORMATTER_BRACE_POSITION_FOR_SWITCH
     * @see #FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION
     */
    public static final String NEXT_LINE_SHIFTED = "next_line_shifted"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / Value to set an option to true.
     * </pre>
     */
    public static final String TRUE = "true"; //$NON-NLS-1$
    /**
     * <pre>
     * FORMATTER / The wrapping is done using as few lines as possible.
     * </pre>
     */
    public static final int WRAP_COMPACT= 1;
    /**
     * <pre>
     * FORMATTER / The wrapping is done putting the first element on a new
     *             line and then wrapping next elements using as few lines as possible.
     * </pre>
     */
    public static final int WRAP_COMPACT_FIRST_BREAK= 2;
    /**
     * <pre>
     * FORMATTER / The wrapping is done by putting each element on its own line
     *             except the first element.
     * </pre>
     */
    public static final int WRAP_NEXT_PER_LINE= 5;
    /**
     * <pre>
     * FORMATTER / The wrapping is done by putting each element on its own line.
     *             All elements are indented by one except the first element.
     * </pre>
     */
    public static final int WRAP_NEXT_SHIFTED= 4;

    /**
     * <pre>
     * FORMATTER / Value to disable alignment.
     * </pre>
     */
    public static final int WRAP_NO_SPLIT= 0;
    /**
     * <pre>
     * FORMATTER / The wrapping is done by putting each element on its own line.
     * </pre>
     */
    public static final int WRAP_ONE_PER_LINE= 3;

    /*
     * Private constants.
     */
    private static final IllegalArgumentException WRONG_ARGUMENT = new IllegalArgumentException();

    /**
     * <p>Return the indentation style of the given alignment value.
     * The given alignment value should be created using the <code>createAlignmentValue(boolean, int, int)</code>
     * API.
     * </p>
     *
     * @param value the given alignment value
     * @return the indentation style of the given alignment value
     * @see #createAlignmentValue(boolean, int, int)
     * @exception IllegalArgumentException if the given alignment value is null, or if it
     * doesn't have a valid format.
     */
    public static int getIndentStyle(String value) {
        if (value == null) {
            throw WRONG_ARGUMENT;
        }
        try {
            int existingValue = Integer.parseInt(value);
            if ((existingValue & STPAlignment.M_INDENT_BY_ONE) != 0) {
                return INDENT_BY_ONE;
            } else if ((existingValue & STPAlignment.M_INDENT_ON_COLUMN) != 0) {
                return INDENT_ON_COLUMN;
            } else {
                return INDENT_DEFAULT;
            }
        } catch (NumberFormatException e) {
            throw WRONG_ARGUMENT;
        }
    }

    /**
     * <p>Return the wrapping style of the given alignment value.
     * The given alignment value should be created using the <code>createAlignmentValue(boolean, int, int)</code>
     * API.
     * </p>
     *
     * @param value the given alignment value
     * @return the wrapping style of the given alignment value
     * @see #createAlignmentValue(boolean, int, int)
     * @exception IllegalArgumentException if the given alignment value is null, or if it
     * doesn't have a valid format.
     */
    public static int getWrappingStyle(String value) {
        if (value == null) {
            throw WRONG_ARGUMENT;
        }
        try {
            int existingValue = Integer.parseInt(value) & STPAlignment.SPLIT_MASK;
            switch (existingValue) {
                case STPAlignment.M_COMPACT_SPLIT:
                    return WRAP_COMPACT;
                case STPAlignment.M_COMPACT_FIRST_BREAK_SPLIT:
                    return WRAP_COMPACT_FIRST_BREAK;
                case STPAlignment.M_NEXT_PER_LINE_SPLIT:
                    return WRAP_NEXT_PER_LINE;
                case STPAlignment.M_NEXT_SHIFTED_SPLIT:
                    return WRAP_NEXT_SHIFTED;
                case STPAlignment.M_ONE_PER_LINE_SPLIT:
                    return WRAP_ONE_PER_LINE;
                default:
                    return WRAP_NO_SPLIT;
            }
        } catch (NumberFormatException e) {
            throw WRONG_ARGUMENT;
        }
    }
}
