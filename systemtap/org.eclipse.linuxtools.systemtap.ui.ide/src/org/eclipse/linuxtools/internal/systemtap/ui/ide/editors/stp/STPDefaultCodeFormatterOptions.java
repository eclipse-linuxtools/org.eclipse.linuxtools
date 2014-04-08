/*******************************************************************************
 * Copyright (c) 2000, 2012, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Anton Leherbauer (Wind River Systems)
 *     Red Hat Inc. - modified for use in SystemTap
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import java.util.Map;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;

/**
 * Code formatter options.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 5.4
 */
public class STPDefaultCodeFormatterOptions {
	public static final int TAB = 1;
	public static final int SPACE = 2;
	public static final int MIXED = 4;

	public int alignment_for_arguments_in_method_invocation;
	public int alignment_for_assignment;
	public int alignment_for_base_clause_in_type_declaration;
	public int alignment_for_binary_expression;
	public int alignment_for_compact_if;
	public int alignment_for_conditional_expression_chain;
	public int alignment_for_conditional_expression;
	public int alignment_for_declarator_list;
	public int alignment_for_enumerator_list;
	public int alignment_for_expression_list;
	public int alignment_for_expressions_in_initializer_list;
	public int alignment_for_member_access;
	public int alignment_for_overloaded_left_shift_chain;
	public int alignment_for_parameters_in_method_declaration;
	public int alignment_for_throws_clause_in_method_declaration;
	public int alignment_for_constructor_initializer_list;

	public String brace_position_for_block;
	public String brace_position_for_block_in_case;
	public String brace_position_for_initializer_list;
	public String brace_position_for_method_declaration;
	public String brace_position_for_namespace_declaration;
	public String brace_position_for_switch;
	public String brace_position_for_type_declaration;

	public int comment_min_distance_between_code_and_line_comment;
	public boolean comment_preserve_white_space_between_code_and_line_comment;
	public boolean never_indent_line_comments_on_first_column;

	public int continuation_indentation;
	public int continuation_indentation_for_initializer_list;

	public boolean indent_statements_compare_to_block;
	public boolean indent_statements_compare_to_body;
	public boolean indent_body_declarations_compare_to_access_specifier;
	public boolean indent_access_specifier_compare_to_type_header;
	public int indent_access_specifier_extra_spaces;
	public boolean indent_body_declarations_compare_to_namespace_header;
	public boolean indent_declaration_compare_to_template_header;
	public boolean indent_breaks_compare_to_cases;
	public boolean indent_empty_lines;
	public boolean indent_switchstatements_compare_to_cases;
	public boolean indent_switchstatements_compare_to_switch;
	public int indentation_size;

	public boolean insert_new_line_after_opening_brace_in_initializer_list;
	public boolean insert_new_line_after_template_declaration;
	public boolean insert_new_line_at_end_of_file_if_missing;
	public boolean insert_new_line_before_catch_in_try_statement;
	public boolean insert_new_line_before_closing_brace_in_initializer_list;
	public boolean insert_new_line_before_colon_in_constructor_initializer_list;
	public boolean insert_new_line_before_else_in_if_statement;
	public boolean insert_new_line_before_while_in_do_statement;
	public boolean insert_new_line_before_identifier_in_function_declaration;
	public boolean insert_new_line_in_empty_block;
	public boolean insert_space_after_assignment_operator;
	public boolean insert_space_after_binary_operator;
	public boolean insert_space_after_closing_angle_bracket_in_template_arguments;
	public boolean insert_space_after_closing_angle_bracket_in_template_parameters;
	public boolean insert_space_after_closing_paren_in_cast;
	public boolean insert_space_after_closing_brace_in_block;
	public boolean insert_space_after_colon_in_base_clause;
	public boolean insert_space_after_colon_in_case;
	public boolean insert_space_after_colon_in_conditional;
	public boolean insert_space_after_colon_in_labeled_statement;
	public boolean insert_space_after_comma_in_initializer_list;
	public boolean insert_space_after_comma_in_enum_declarations;
	public boolean insert_space_after_comma_in_method_invocation_arguments;
	public boolean insert_space_after_comma_in_method_declaration_parameters;
	public boolean insert_space_after_comma_in_method_declaration_throws;
	public boolean insert_space_after_comma_in_declarator_list;
	public boolean insert_space_after_comma_in_expression_list;
	public boolean insert_space_after_comma_in_base_types;
	public boolean insert_space_after_comma_in_template_arguments;
	public boolean insert_space_after_comma_in_template_parameters;
	public boolean insert_space_after_opening_angle_bracket_in_template_arguments;
	public boolean insert_space_after_opening_angle_bracket_in_template_parameters;
	public boolean insert_space_after_opening_bracket;
	public boolean insert_space_after_opening_brace_in_initializer_list;
	public boolean insert_space_after_opening_paren_in_cast;
	public boolean insert_space_after_opening_paren_in_catch;
	public boolean insert_space_after_opening_paren_in_for;
	public boolean insert_space_after_opening_paren_in_if;
	public boolean insert_space_after_opening_paren_in_method_declaration;
	public boolean insert_space_after_opening_paren_in_method_invocation;
	public boolean insert_space_after_opening_paren_in_exception_specification;
	public boolean insert_space_after_opening_paren_in_parenthesized_expression;
	public boolean insert_space_after_opening_paren_in_switch;
	public boolean insert_space_after_opening_paren_in_while;
	public boolean insert_space_after_postfix_operator;
	public boolean insert_space_after_prefix_operator;
	public boolean insert_space_after_question_in_conditional;
	public boolean insert_space_after_semicolon_in_for;
	public boolean insert_space_after_unary_operator;
	public boolean insert_space_before_assignment_operator;
	public boolean insert_space_before_binary_operator;
	public boolean insert_space_before_closing_angle_bracket_in_template_arguments;
	public boolean insert_space_before_closing_angle_bracket_in_template_parameters;
	public boolean insert_space_before_closing_brace_in_initializer_list;
	public boolean insert_space_before_closing_bracket;
	public boolean insert_space_before_closing_paren_in_cast;
	public boolean insert_space_before_closing_paren_in_catch;
	public boolean insert_space_before_closing_paren_in_for;
	public boolean insert_space_before_closing_paren_in_if;
	public boolean insert_space_before_closing_paren_in_method_declaration;
	public boolean insert_space_before_closing_paren_in_method_invocation;
	public boolean insert_space_before_closing_paren_in_exception_specification;
	public boolean insert_space_before_closing_paren_in_parenthesized_expression;
	public boolean insert_space_before_closing_paren_in_switch;
	public boolean insert_space_before_closing_paren_in_while;
	public boolean insert_space_before_colon_in_base_clause;
	public boolean insert_space_before_colon_in_case;
	public boolean insert_space_before_colon_in_conditional;
	public boolean insert_space_before_colon_in_default;
	public boolean insert_space_before_colon_in_labeled_statement;
	public boolean insert_space_before_comma_in_initializer_list;
	public boolean insert_space_before_comma_in_enum_declarations;
	public boolean insert_space_before_comma_in_method_invocation_arguments;
	public boolean insert_space_before_comma_in_method_declaration_parameters;
	public boolean insert_space_before_comma_in_method_declaration_throws;
	public boolean insert_space_before_comma_in_declarator_list;
	public boolean insert_space_before_comma_in_expression_list;
	public boolean insert_space_before_comma_in_base_types;
	public boolean insert_space_before_comma_in_template_arguments;
	public boolean insert_space_before_comma_in_template_parameters;
	public boolean insert_space_before_opening_angle_bracket_in_template_arguments;
	public boolean insert_space_before_opening_angle_bracket_in_template_parameters;
	public boolean insert_space_before_opening_brace_in_initializer_list;
	public boolean insert_space_before_opening_brace_in_block;
	public boolean insert_space_before_opening_brace_in_method_declaration;
	public boolean insert_space_before_opening_brace_in_type_declaration;
	public boolean insert_space_before_opening_brace_in_namespace_declaration;
	public boolean insert_space_before_opening_bracket;
	public boolean insert_space_before_opening_paren_in_catch;
	public boolean insert_space_before_opening_paren_in_for;
	public boolean insert_space_before_opening_paren_in_if;
	public boolean insert_space_before_opening_paren_in_method_invocation;
	public boolean insert_space_before_opening_paren_in_exception_specification;
	public boolean insert_space_before_opening_paren_in_method_declaration;
	public boolean insert_space_before_opening_paren_in_switch;
	public boolean insert_space_before_opening_brace_in_switch;
	public boolean insert_space_before_opening_paren_in_parenthesized_expression;
	public boolean insert_space_before_opening_paren_in_while;
	public boolean insert_space_before_postfix_operator;
	public boolean insert_space_before_prefix_operator;
	public boolean insert_space_before_question_in_conditional;
	public boolean insert_space_before_semicolon;
	public boolean insert_space_before_semicolon_in_for;
	public boolean insert_space_before_unary_operator;
	public boolean insert_space_between_empty_braces_in_initializer_list;
	public boolean insert_space_between_empty_brackets;
	public boolean insert_space_between_empty_parens_in_method_declaration;
	public boolean insert_space_between_empty_parens_in_method_invocation;
	public boolean insert_space_between_empty_parens_in_exception_specification;
	public boolean compact_else_if;
	public boolean keep_guardian_clause_on_one_line;
	public boolean keep_else_statement_on_same_line;
	public boolean keep_empty_initializer_list_on_one_line;
	public boolean keep_simple_if_on_one_line;
	public boolean keep_then_statement_on_same_line;
	public int number_of_empty_lines_to_preserve;
	public boolean join_wrapped_lines;
	public boolean put_empty_statement_on_new_line;
	public int tab_size;
	public int page_width;
	public int tab_char = TAB;
	public boolean use_tabs_only_for_leading_indentations;
	public int initial_indentation_level;
	public String line_separator;

	public STPDefaultCodeFormatterOptions(Map<String, String> settings) {
		setDefaultSettings();
		if (settings == null) return;
		set(settings);
	}

	private void set(Map<String, String> settings) {
		final String alignmentForArgumentsInMethodInvocationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION);
		if (alignmentForArgumentsInMethodInvocationOption != null) {
			try {
				this.alignment_for_arguments_in_method_invocation = Integer.parseInt(alignmentForArgumentsInMethodInvocationOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_arguments_in_method_invocation = STPAlignment.M_COMPACT_SPLIT;
			}
		}
		final String alignmentForAssignmentOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ASSIGNMENT);
		if (alignmentForAssignmentOption != null) {
			try {
				this.alignment_for_assignment = Integer.parseInt(alignmentForAssignmentOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_assignment =  STPAlignment.M_COMPACT_SPLIT;
			}
		}
		final String alignmentForBinaryExpressionOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_BINARY_EXPRESSION);
		if (alignmentForBinaryExpressionOption != null) {
			try {
				this.alignment_for_binary_expression = Integer.parseInt(alignmentForBinaryExpressionOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_binary_expression =  STPAlignment.M_COMPACT_SPLIT;
			}
		}
		final String alignmentForCompactIfOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_COMPACT_IF);
		if (alignmentForCompactIfOption != null) {
			try {
				this.alignment_for_compact_if = Integer.parseInt(alignmentForCompactIfOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_compact_if = STPAlignment.M_ONE_PER_LINE_SPLIT | STPAlignment.M_INDENT_BY_ONE;
			}
		}
		final String alignmentForConditionalExpressionOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_CONDITIONAL_EXPRESSION);
		if (alignmentForConditionalExpressionOption != null) {
			try {
				this.alignment_for_conditional_expression = Integer.parseInt(alignmentForConditionalExpressionOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_conditional_expression = STPAlignment.M_COMPACT_FIRST_BREAK_SPLIT | STPAlignment.M_INDENT_ON_COLUMN;
			}
		}
		final String alignmentForConditionalExpressionChainOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_CONDITIONAL_EXPRESSION_CHAIN);
		if (alignmentForConditionalExpressionChainOption != null) {
			try {
				this.alignment_for_conditional_expression_chain = Integer.parseInt(alignmentForConditionalExpressionChainOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_conditional_expression_chain = STPAlignment.M_COMPACT_SPLIT | STPAlignment.M_INDENT_ON_COLUMN;
			}
		}
		final String alignmentForDeclaratorListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_DECLARATOR_LIST);
		if (alignmentForDeclaratorListOption != null) {
			try {
				this.alignment_for_declarator_list = Integer.parseInt(alignmentForDeclaratorListOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_declarator_list = STPAlignment.M_COMPACT_SPLIT;
			}
		}
		final String alignmentForEnumeratorListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUMERATOR_LIST);
		if (alignmentForEnumeratorListOption != null) {
			try {
				this.alignment_for_enumerator_list = Integer.parseInt(alignmentForEnumeratorListOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_enumerator_list = STPAlignment.M_ONE_PER_LINE_SPLIT;
			}
		}
		final String alignmentForExpressionsInInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_INITIALIZER_LIST);
		if (alignmentForExpressionsInInitializerListOption != null) {
			try {
				this.alignment_for_expressions_in_initializer_list = Integer.parseInt(alignmentForExpressionsInInitializerListOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_expressions_in_initializer_list = STPAlignment.M_COMPACT_SPLIT;
			}
		}
		final String alignmentForExpressionListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_EXPRESSION_LIST);
		if (alignmentForExpressionListOption != null) {
			try {
				this.alignment_for_expression_list = Integer.parseInt(alignmentForExpressionListOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_expression_list = STPAlignment.M_COMPACT_SPLIT;
			}
		}
		final String alignmentForMemberAccessOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_MEMBER_ACCESS);
		if (alignmentForMemberAccessOption != null) {
			try {
				this.alignment_for_member_access = Integer.parseInt(alignmentForMemberAccessOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_member_access =  STPAlignment.M_ONE_PER_LINE_SPLIT;
			}
		}
		final String alignmentForOverloadedLeftShiftChainOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_OVERLOADED_LEFT_SHIFT_CHAIN);
		if (alignmentForOverloadedLeftShiftChainOption != null) {
			try {
				this.alignment_for_overloaded_left_shift_chain = Integer.parseInt(alignmentForOverloadedLeftShiftChainOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_overloaded_left_shift_chain = STPAlignment.M_COMPACT_SPLIT;
			}
		}
		final String alignmentForParametersInMethodDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION);
		if (alignmentForParametersInMethodDeclarationOption != null) {
			try {
				this.alignment_for_parameters_in_method_declaration = Integer.parseInt(alignmentForParametersInMethodDeclarationOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_parameters_in_method_declaration = STPAlignment.M_COMPACT_SPLIT;
			}
		}
		final String alignmentForBaseClauseInTypeDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_BASE_CLAUSE_IN_TYPE_DECLARATION);
		if (alignmentForBaseClauseInTypeDeclarationOption != null) {
			try {
				this.alignment_for_base_clause_in_type_declaration = Integer.parseInt(alignmentForBaseClauseInTypeDeclarationOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_base_clause_in_type_declaration = STPAlignment.M_NEXT_SHIFTED_SPLIT;
			}
		}
		final String alignmentForConstructorInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_CONSTRUCTOR_INITIALIZER_LIST);
		if (alignmentForConstructorInitializerListOption != null) {
			try {
				this.alignment_for_constructor_initializer_list = Integer.parseInt(alignmentForConstructorInitializerListOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_constructor_initializer_list = STPAlignment.M_COMPACT_SPLIT;
			}
		}
		final String alignmentForThrowsClauseInMethodDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_THROWS_CLAUSE_IN_METHOD_DECLARATION);
		if (alignmentForThrowsClauseInMethodDeclarationOption != null) {
			try {
				this.alignment_for_throws_clause_in_method_declaration = Integer.parseInt(alignmentForThrowsClauseInMethodDeclarationOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.alignment_for_throws_clause_in_method_declaration = STPAlignment.M_COMPACT_SPLIT;
			}
		}
		final String bracePositionForInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_INITIALIZER_LIST);
		if (bracePositionForInitializerListOption != null) {
			try {
				this.brace_position_for_initializer_list = bracePositionForInitializerListOption;
			} catch (ClassCastException e) {
				this.brace_position_for_initializer_list = STPDefaultCodeFormatterConstants.END_OF_LINE;
			}
		}
		final String bracePositionForBlockOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_BLOCK);
		if (bracePositionForBlockOption != null) {
			try {
				this.brace_position_for_block = bracePositionForBlockOption;
			} catch (ClassCastException e) {
				this.brace_position_for_block = STPDefaultCodeFormatterConstants.END_OF_LINE;
			}
		}
		final String bracePositionForBlockInCaseOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_BLOCK_IN_CASE);
		if (bracePositionForBlockInCaseOption != null) {
			try {
				this.brace_position_for_block_in_case = bracePositionForBlockInCaseOption;
			} catch (ClassCastException e) {
				this.brace_position_for_block_in_case = STPDefaultCodeFormatterConstants.END_OF_LINE;
			}
		}
		final String bracePositionForMethodDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION);
		if (bracePositionForMethodDeclarationOption != null) {
			try {
				this.brace_position_for_method_declaration = bracePositionForMethodDeclarationOption;
			} catch (ClassCastException e) {
				this.brace_position_for_method_declaration = STPDefaultCodeFormatterConstants.END_OF_LINE;
			}
		}
		final String bracePositionForSwitchOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_SWITCH);
		if (bracePositionForSwitchOption != null) {
			try {
				this.brace_position_for_switch = bracePositionForSwitchOption;
			} catch (ClassCastException e) {
				this.brace_position_for_switch = STPDefaultCodeFormatterConstants.END_OF_LINE;
			}
		}
		final String bracePositionForTypeDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION);
		if (bracePositionForTypeDeclarationOption != null) {
			try {
				this.brace_position_for_type_declaration = bracePositionForTypeDeclarationOption;
			} catch (ClassCastException e) {
				this.brace_position_for_type_declaration = STPDefaultCodeFormatterConstants.END_OF_LINE;
			}
		}
		final String bracePositionForNamespaceDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_NAMESPACE_DECLARATION);
		if (bracePositionForNamespaceDeclarationOption != null) {
			try {
				this.brace_position_for_namespace_declaration = bracePositionForNamespaceDeclarationOption;
			} catch (ClassCastException e) {
				this.brace_position_for_namespace_declaration = STPDefaultCodeFormatterConstants.END_OF_LINE;
			}
		}
		final String commentMinDistanceBetweenCodeAndLineCommentOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_COMMENT_MIN_DISTANCE_BETWEEN_CODE_AND_LINE_COMMENT);
		if (commentMinDistanceBetweenCodeAndLineCommentOption != null) {
			try {
				this.comment_min_distance_between_code_and_line_comment = Integer.parseInt(commentMinDistanceBetweenCodeAndLineCommentOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.comment_min_distance_between_code_and_line_comment = 1;
			}
		}
		final String commentPreserveWhiteSpaceBetweenCodeAndLineCommentOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_COMMENT_PRESERVE_WHITE_SPACE_BETWEEN_CODE_AND_LINE_COMMENT);
		if (commentPreserveWhiteSpaceBetweenCodeAndLineCommentOption != null) {
			this.comment_preserve_white_space_between_code_and_line_comment = STPDefaultCodeFormatterConstants.TRUE.equals(commentPreserveWhiteSpaceBetweenCodeAndLineCommentOption);
		}
		final String neverIndentLineCommentsOnFirstColumn = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_COMMENT_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN);
		if (neverIndentLineCommentsOnFirstColumn != null) {
			this.never_indent_line_comments_on_first_column = STPDefaultCodeFormatterConstants.TRUE.equals(neverIndentLineCommentsOnFirstColumn);
		}
		final String continuationIndentationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION);
		if (continuationIndentationOption != null) {
			try {
				this.continuation_indentation = Integer.parseInt(continuationIndentationOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.continuation_indentation = 2;
			}
		}
		final String continuationIndentationForInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION_FOR_INITIALIZER_LIST);
		if (continuationIndentationForInitializerListOption != null) {
			try {
				this.continuation_indentation_for_initializer_list = Integer.parseInt(continuationIndentationForInitializerListOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.continuation_indentation_for_initializer_list = 2;
			}
		}
		final String indentStatementsCompareToBlockOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BLOCK);
		if (indentStatementsCompareToBlockOption != null) {
			this.indent_statements_compare_to_block = STPDefaultCodeFormatterConstants.TRUE.equals(indentStatementsCompareToBlockOption);
		}
		final String indentStatementsCompareToBodyOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BODY);
		if (indentStatementsCompareToBodyOption != null) {
			this.indent_statements_compare_to_body = STPDefaultCodeFormatterConstants.TRUE.equals(indentStatementsCompareToBodyOption);
		}
		final String indentAccessSpecifierCompareToTypeHeaderOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER);
		if (indentAccessSpecifierCompareToTypeHeaderOption != null) {
			this.indent_access_specifier_compare_to_type_header = STPDefaultCodeFormatterConstants.TRUE.equals(indentAccessSpecifierCompareToTypeHeaderOption);
		}
		final String indentAccessSpecifierExtraSpaces = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_EXTRA_SPACES);
		if (indentAccessSpecifierExtraSpaces != null) {
			try {
				this.indent_access_specifier_extra_spaces = Integer.parseInt(indentAccessSpecifierExtraSpaces);
			} catch (NumberFormatException|ClassCastException e) {
				this.indent_access_specifier_extra_spaces = 0;
			}
		}
		final String indentBodyDeclarationsCompareToAccessSpecifierOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER);
		if (indentBodyDeclarationsCompareToAccessSpecifierOption != null) {
			this.indent_body_declarations_compare_to_access_specifier = STPDefaultCodeFormatterConstants.TRUE.equals(indentBodyDeclarationsCompareToAccessSpecifierOption);
		}
		final String indentDeclarationCompareToTemplateHeaderOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_DECLARATION_COMPARE_TO_TEMPLATE_HEADER);
		if (indentDeclarationCompareToTemplateHeaderOption != null) {
			this.indent_declaration_compare_to_template_header = STPDefaultCodeFormatterConstants.TRUE.equals(indentDeclarationCompareToTemplateHeaderOption);
		}
		final String indentBodyDeclarationsCompareToNamespaceHeaderOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_NAMESPACE_HEADER);
		if (indentBodyDeclarationsCompareToNamespaceHeaderOption != null) {
			this.indent_body_declarations_compare_to_namespace_header = STPDefaultCodeFormatterConstants.TRUE.equals(indentBodyDeclarationsCompareToNamespaceHeaderOption);
		}
		final String indentBreaksCompareToCasesOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_BREAKS_COMPARE_TO_CASES);
		if (indentBreaksCompareToCasesOption != null) {
			this.indent_breaks_compare_to_cases = STPDefaultCodeFormatterConstants.TRUE.equals(indentBreaksCompareToCasesOption);
		}
		final String indentEmptyLinesOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES);
		if (indentEmptyLinesOption != null) {
			this.indent_empty_lines = STPDefaultCodeFormatterConstants.TRUE.equals(indentEmptyLinesOption);
		}
		final String indentSwitchstatementsCompareToCasesOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES);
		if (indentSwitchstatementsCompareToCasesOption != null) {
			this.indent_switchstatements_compare_to_cases = STPDefaultCodeFormatterConstants.TRUE.equals(indentSwitchstatementsCompareToCasesOption);
		}
		final String indentSwitchstatementsCompareToSwitchOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH);
		if (indentSwitchstatementsCompareToSwitchOption != null) {
			this.indent_switchstatements_compare_to_switch = STPDefaultCodeFormatterConstants.TRUE.equals(indentSwitchstatementsCompareToSwitchOption);
		}
		final String indentationSizeOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE);
		if (indentationSizeOption != null) {
			try {
				this.indentation_size = Integer.parseInt(indentationSizeOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.indentation_size = 4;
			}
		}
		final String insertNewLineAfterOpeningBraceInInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_INITIALIZER_LIST);
		if (insertNewLineAfterOpeningBraceInInitializerListOption != null) {
			this.insert_new_line_after_opening_brace_in_initializer_list = IDEPlugin.INSERT.equals(insertNewLineAfterOpeningBraceInInitializerListOption);
		}
		final String insertNewLineAfterTemplateDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_TEMPLATE_DECLARATION);
		if (insertNewLineAfterOpeningBraceInInitializerListOption != null) {
			this.insert_new_line_after_template_declaration = IDEPlugin.INSERT.equals(insertNewLineAfterTemplateDeclarationOption);
		}
		final String insertNewLineAtEndOfFileIfMissingOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AT_END_OF_FILE_IF_MISSING);
		if (insertNewLineAtEndOfFileIfMissingOption != null) {
			this.insert_new_line_at_end_of_file_if_missing = IDEPlugin.INSERT.equals(insertNewLineAtEndOfFileIfMissingOption);
		}
		final String insertNewLineBeforeCatchInTryStatementOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CATCH_IN_TRY_STATEMENT);
		if (insertNewLineBeforeCatchInTryStatementOption != null) {
			this.insert_new_line_before_catch_in_try_statement = IDEPlugin.INSERT.equals(insertNewLineBeforeCatchInTryStatementOption);
		}
		final String insertNewLineBeforeClosingBraceInInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_INITIALIZER_LIST);
		if (insertNewLineBeforeClosingBraceInInitializerListOption != null) {
			this.insert_new_line_before_closing_brace_in_initializer_list = IDEPlugin.INSERT.equals(insertNewLineBeforeClosingBraceInInitializerListOption);
		}
		final String insertNewLineBeforeColonInConstructorInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_COLON_IN_CONSTRUCTOR_INITIALIZER_LIST);
		if (insertNewLineBeforeColonInConstructorInitializerListOption != null) {
			this.insert_new_line_before_colon_in_constructor_initializer_list = IDEPlugin.INSERT.equals(insertNewLineBeforeColonInConstructorInitializerListOption);
		}
		final String insertNewLineBeforeElseInIfStatementOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_ELSE_IN_IF_STATEMENT);
		if (insertNewLineBeforeElseInIfStatementOption != null) {
			this.insert_new_line_before_else_in_if_statement = IDEPlugin.INSERT.equals(insertNewLineBeforeElseInIfStatementOption);
		}
		final String insertNewLineBeforeWhileInDoStatementOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_WHILE_IN_DO_STATEMENT);
		if (insertNewLineBeforeWhileInDoStatementOption != null) {
			this.insert_new_line_before_while_in_do_statement = IDEPlugin.INSERT.equals(insertNewLineBeforeWhileInDoStatementOption);
		}
		final String insertNewLineBeforeIdentifierInFunctionDefinitionOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_IDENTIFIER_IN_FUNCTION_DECLARATION);
		if (insertNewLineBeforeWhileInDoStatementOption != null) {
			this.insert_new_line_before_identifier_in_function_declaration = IDEPlugin.INSERT.equals(insertNewLineBeforeIdentifierInFunctionDefinitionOption);
		}
		final String insertNewLineInEmptyBlockOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK);
		if (insertNewLineInEmptyBlockOption != null) {
			this.insert_new_line_in_empty_block = IDEPlugin.INSERT.equals(insertNewLineInEmptyBlockOption);
		}
		final String insertSpaceAfterAssignmentOperatorOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR);
		if (insertSpaceAfterAssignmentOperatorOption != null) {
			this.insert_space_after_assignment_operator = IDEPlugin.INSERT.equals(insertSpaceAfterAssignmentOperatorOption);
		}
		final String insertSpaceAfterBinaryOperatorOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR);
		if (insertSpaceAfterBinaryOperatorOption != null) {
			this.insert_space_after_binary_operator = IDEPlugin.INSERT.equals(insertSpaceAfterBinaryOperatorOption);
		}
		final String insertSpaceAfterClosingAngleBracketInTemplateArgumentsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS);
		if (insertSpaceAfterClosingAngleBracketInTemplateArgumentsOption != null) {
			this.insert_space_after_closing_angle_bracket_in_template_arguments = IDEPlugin.INSERT.equals(insertSpaceAfterClosingAngleBracketInTemplateArgumentsOption);
		}
		final String insertSpaceAfterClosingAngleBracketInTemplateParametersOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS);
		if (insertSpaceAfterClosingAngleBracketInTemplateParametersOption != null) {
			this.insert_space_after_closing_angle_bracket_in_template_parameters = IDEPlugin.INSERT.equals(insertSpaceAfterClosingAngleBracketInTemplateParametersOption);
		}
		final String insertSpaceAfterClosingParenInCastOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST);
		if (insertSpaceAfterClosingParenInCastOption != null) {
			this.insert_space_after_closing_paren_in_cast = IDEPlugin.INSERT.equals(insertSpaceAfterClosingParenInCastOption);
		}
		final String insertSpaceAfterClosingBraceInBlockOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_BRACE_IN_BLOCK);
		if (insertSpaceAfterClosingBraceInBlockOption != null) {
			this.insert_space_after_closing_brace_in_block = IDEPlugin.INSERT.equals(insertSpaceAfterClosingBraceInBlockOption);
		}
		final String insertSpaceAfterColonInBaseClauseOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_BASE_CLAUSE);
		if (insertSpaceAfterColonInBaseClauseOption != null) {
			this.insert_space_after_colon_in_base_clause = IDEPlugin.INSERT.equals(insertSpaceAfterColonInBaseClauseOption);
		}
		final String insertSpaceAfterColonInCaseOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CASE);
		if (insertSpaceAfterColonInCaseOption != null) {
			this.insert_space_after_colon_in_case = IDEPlugin.INSERT.equals(insertSpaceAfterColonInCaseOption);
		}
		final String insertSpaceAfterColonInConditionalOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL);
		if (insertSpaceAfterColonInConditionalOption != null) {
			this.insert_space_after_colon_in_conditional = IDEPlugin.INSERT.equals(insertSpaceAfterColonInConditionalOption);
		}
		final String insertSpaceAfterColonInLabeledStatementOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT);
		if (insertSpaceAfterColonInLabeledStatementOption != null) {
			this.insert_space_after_colon_in_labeled_statement = IDEPlugin.INSERT.equals(insertSpaceAfterColonInLabeledStatementOption);
		}
		final String insertSpaceAfterCommaInInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_INITIALIZER_LIST);
		if (insertSpaceAfterCommaInInitializerListOption != null) {
			this.insert_space_after_comma_in_initializer_list = IDEPlugin.INSERT.equals(insertSpaceAfterCommaInInitializerListOption);
		}
		final String insertSpaceAfterCommaInEnumDeclarationsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ENUM_DECLARATIONS);
		if (insertSpaceAfterCommaInEnumDeclarationsOption != null) {
			this.insert_space_after_comma_in_enum_declarations = IDEPlugin.INSERT.equals(insertSpaceAfterCommaInEnumDeclarationsOption);
		}
		final String insertSpaceAfterCommaInMethodInvocationArgumentsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_INVOCATION_ARGUMENTS);
		if (insertSpaceAfterCommaInMethodInvocationArgumentsOption != null) {
			this.insert_space_after_comma_in_method_invocation_arguments = IDEPlugin.INSERT.equals(insertSpaceAfterCommaInMethodInvocationArgumentsOption);
		}
		final String insertSpaceAfterCommaInMethodDeclarationParametersOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_PARAMETERS);
		if (insertSpaceAfterCommaInMethodDeclarationParametersOption != null) {
			this.insert_space_after_comma_in_method_declaration_parameters = IDEPlugin.INSERT.equals(insertSpaceAfterCommaInMethodDeclarationParametersOption);
		}
		final String insertSpaceAfterCommaInMethodDeclarationThrowsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_THROWS);
		if (insertSpaceAfterCommaInMethodDeclarationThrowsOption != null) {
			this.insert_space_after_comma_in_method_declaration_throws = IDEPlugin.INSERT.equals(insertSpaceAfterCommaInMethodDeclarationThrowsOption);
		}
		final String insertSpaceAfterCommaInMultipleFieldDeclarationsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_DECLARATOR_LIST);
		if (insertSpaceAfterCommaInMultipleFieldDeclarationsOption != null) {
			this.insert_space_after_comma_in_declarator_list = IDEPlugin.INSERT.equals(insertSpaceAfterCommaInMultipleFieldDeclarationsOption);
		}
		final String insertSpaceAfterCommaInExpressionListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPRESSION_LIST);
		if (insertSpaceAfterCommaInExpressionListOption != null) {
			this.insert_space_after_comma_in_expression_list = IDEPlugin.INSERT.equals(insertSpaceAfterCommaInExpressionListOption);
		}
		final String insertSpaceAfterCommaInBaseTypesOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_BASE_TYPES);
		if (insertSpaceAfterCommaInBaseTypesOption != null) {
			this.insert_space_after_comma_in_base_types = IDEPlugin.INSERT.equals(insertSpaceAfterCommaInBaseTypesOption);
		}
		final String insertSpaceAfterCommaInTemplateArgumentsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_TEMPLATE_ARGUMENTS);
		if (insertSpaceAfterCommaInTemplateArgumentsOption != null) {
			this.insert_space_after_comma_in_template_arguments = IDEPlugin.INSERT.equals(insertSpaceAfterCommaInTemplateArgumentsOption);
		}
		final String insertSpaceAfterCommaInTemplateParametersOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_TEMPLATE_PARAMETERS);
		if (insertSpaceAfterCommaInTemplateParametersOption != null) {
			this.insert_space_after_comma_in_template_parameters = IDEPlugin.INSERT.equals(insertSpaceAfterCommaInTemplateParametersOption);
		}
		final String insertSpaceAfterOpeningAngleBracketInTemplateArgumentsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS);
		if (insertSpaceAfterOpeningAngleBracketInTemplateArgumentsOption != null) {
			this.insert_space_after_opening_angle_bracket_in_template_arguments = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningAngleBracketInTemplateArgumentsOption);
		}
		final String insertSpaceAfterOpeningAngleBracketInTemplateParametersOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS);
		if (insertSpaceAfterOpeningAngleBracketInTemplateParametersOption != null) {
			this.insert_space_after_opening_angle_bracket_in_template_parameters = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningAngleBracketInTemplateParametersOption);
		}
		final String insertSpaceAfterOpeningBracketInArrayReferenceOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACKET);
		if (insertSpaceAfterOpeningBracketInArrayReferenceOption != null) {
			this.insert_space_after_opening_bracket = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningBracketInArrayReferenceOption);
		}
		final String insertSpaceAfterOpeningBraceInInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_INITIALIZER_LIST);
		if (insertSpaceAfterOpeningBraceInInitializerListOption != null) {
			this.insert_space_after_opening_brace_in_initializer_list = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningBraceInInitializerListOption);
		}
		final String insertSpaceAfterOpeningParenInCastOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST);
		if (insertSpaceAfterOpeningParenInCastOption != null) {
			this.insert_space_after_opening_paren_in_cast = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningParenInCastOption);
		}
		final String insertSpaceAfterOpeningParenInCatchOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CATCH);
		if (insertSpaceAfterOpeningParenInCatchOption != null) {
			this.insert_space_after_opening_paren_in_catch = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningParenInCatchOption);
		}
		final String insertSpaceAfterOpeningParenInForOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_FOR);
		if (insertSpaceAfterOpeningParenInForOption != null) {
			this.insert_space_after_opening_paren_in_for = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningParenInForOption);
		}
		final String insertSpaceAfterOpeningParenInIfOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_IF);
		if (insertSpaceAfterOpeningParenInIfOption != null) {
			this.insert_space_after_opening_paren_in_if = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningParenInIfOption);
		}
		final String insertSpaceAfterOpeningParenInMethodDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_DECLARATION);
		if (insertSpaceAfterOpeningParenInMethodDeclarationOption != null) {
			this.insert_space_after_opening_paren_in_method_declaration = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningParenInMethodDeclarationOption);
		}
		final String insertSpaceAfterOpeningParenInExceptionSpecificationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_EXCEPTION_SPECIFICATION);
		if (insertSpaceAfterOpeningParenInExceptionSpecificationOption != null) {
			this.insert_space_after_opening_paren_in_exception_specification = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningParenInExceptionSpecificationOption);
		}
		final String insertSpaceAfterOpeningParenInMethodInvocationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION);
		if (insertSpaceAfterOpeningParenInMethodInvocationOption != null) {
			this.insert_space_after_opening_paren_in_method_invocation = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningParenInMethodInvocationOption);
		}
		final String insertSpaceAfterOpeningParenInParenthesizedExpressionOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION);
		if (insertSpaceAfterOpeningParenInParenthesizedExpressionOption != null) {
			this.insert_space_after_opening_paren_in_parenthesized_expression = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningParenInParenthesizedExpressionOption);
		}
		final String insertSpaceAfterOpeningParenInSwitchOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SWITCH);
		if (insertSpaceAfterOpeningParenInSwitchOption != null) {
			this.insert_space_after_opening_paren_in_switch = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningParenInSwitchOption);
		}
		final String insertSpaceAfterOpeningParenInWhileOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_WHILE);
		if (insertSpaceAfterOpeningParenInWhileOption != null) {
			this.insert_space_after_opening_paren_in_while = IDEPlugin.INSERT.equals(insertSpaceAfterOpeningParenInWhileOption);
		}
		final String insertSpaceAfterPostfixOperatorOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POSTFIX_OPERATOR);
		if (insertSpaceAfterPostfixOperatorOption != null) {
			this.insert_space_after_postfix_operator = IDEPlugin.INSERT.equals(insertSpaceAfterPostfixOperatorOption);
		}
		final String insertSpaceAfterPrefixOperatorOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR);
		if (insertSpaceAfterPrefixOperatorOption != null) {
			this.insert_space_after_prefix_operator = IDEPlugin.INSERT.equals(insertSpaceAfterPrefixOperatorOption);
		}
		final String insertSpaceAfterQuestionInConditionalOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL);
		if (insertSpaceAfterQuestionInConditionalOption != null) {
			this.insert_space_after_question_in_conditional = IDEPlugin.INSERT.equals(insertSpaceAfterQuestionInConditionalOption);
		}
		final String insertSpaceAfterSemicolonInForOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR);
		if (insertSpaceAfterSemicolonInForOption != null) {
			this.insert_space_after_semicolon_in_for = IDEPlugin.INSERT.equals(insertSpaceAfterSemicolonInForOption);
		}
		final String insertSpaceAfterUnaryOperatorOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_UNARY_OPERATOR);
		if (insertSpaceAfterUnaryOperatorOption != null) {
			this.insert_space_after_unary_operator = IDEPlugin.INSERT.equals(insertSpaceAfterUnaryOperatorOption);
		}
		final String insertSpaceBeforeAssignmentOperatorOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR);
		if (insertSpaceBeforeAssignmentOperatorOption != null) {
			this.insert_space_before_assignment_operator = IDEPlugin.INSERT.equals(insertSpaceBeforeAssignmentOperatorOption);
		}
		final String insertSpaceBeforeBinaryOperatorOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR);
		if (insertSpaceBeforeBinaryOperatorOption != null) {
			this.insert_space_before_binary_operator = IDEPlugin.INSERT.equals(insertSpaceBeforeBinaryOperatorOption);
		}
		final String insertSpaceBeforeClosingAngleBracketInTemplateArgumentsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS);
		if (insertSpaceBeforeClosingAngleBracketInTemplateArgumentsOption != null) {
			this.insert_space_before_closing_angle_bracket_in_template_arguments = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingAngleBracketInTemplateArgumentsOption);
		}
		final String insertSpaceBeforeClosingAngleBracketInTemplateParametersOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS);
		if (insertSpaceBeforeClosingAngleBracketInTemplateParametersOption != null) {
			this.insert_space_before_closing_angle_bracket_in_template_parameters = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingAngleBracketInTemplateParametersOption);
		}
		final String insertSpaceBeforeClosingBraceInInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_INITIALIZER_LIST);
		if (insertSpaceBeforeClosingBraceInInitializerListOption != null) {
			this.insert_space_before_closing_brace_in_initializer_list = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingBraceInInitializerListOption);
		}
		final String insertSpaceBeforeClosingBracketInArrayReferenceOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACKET);
		if (insertSpaceBeforeClosingBracketInArrayReferenceOption != null) {
			this.insert_space_before_closing_bracket = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingBracketInArrayReferenceOption);
		}
		final String insertSpaceBeforeClosingParenInCastOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST);
		if (insertSpaceBeforeClosingParenInCastOption != null) {
			this.insert_space_before_closing_paren_in_cast = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingParenInCastOption);
		}
		final String insertSpaceBeforeClosingParenInCatchOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CATCH);
		if (insertSpaceBeforeClosingParenInCatchOption != null) {
			this.insert_space_before_closing_paren_in_catch = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingParenInCatchOption);
		}
		final String insertSpaceBeforeClosingParenInForOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_FOR);
		if (insertSpaceBeforeClosingParenInForOption != null) {
			this.insert_space_before_closing_paren_in_for = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingParenInForOption);
		}
		final String insertSpaceBeforeClosingParenInIfOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_IF);
		if (insertSpaceBeforeClosingParenInIfOption != null) {
			this.insert_space_before_closing_paren_in_if = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingParenInIfOption);
		}
		final String insertSpaceBeforeClosingParenInMethodDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_DECLARATION);
		if (insertSpaceBeforeClosingParenInMethodDeclarationOption != null) {
			this.insert_space_before_closing_paren_in_method_declaration = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingParenInMethodDeclarationOption);
		}
		final String insertSpaceBeforeClosingParenInExceptionSpecificationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_EXCEPTION_SPECIFICATION);
		if (insertSpaceBeforeClosingParenInExceptionSpecificationOption != null) {
			this.insert_space_before_closing_paren_in_exception_specification = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingParenInExceptionSpecificationOption);
		}
		final String insertSpaceBeforeClosingParenInMethodInvocationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION);
		if (insertSpaceBeforeClosingParenInMethodInvocationOption != null) {
			this.insert_space_before_closing_paren_in_method_invocation = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingParenInMethodInvocationOption);
		}
		final String insertSpaceBeforeClosingParenInParenthesizedExpressionOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHESIZED_EXPRESSION);
		if (insertSpaceBeforeClosingParenInParenthesizedExpressionOption != null) {
			this.insert_space_before_closing_paren_in_parenthesized_expression = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingParenInParenthesizedExpressionOption);
		}
		final String insertSpaceBeforeClosingParenInSwitchOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SWITCH);
		if (insertSpaceBeforeClosingParenInSwitchOption != null) {
			this.insert_space_before_closing_paren_in_switch = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingParenInSwitchOption);
		}
		final String insertSpaceBeforeClosingParenInWhileOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_WHILE);
		if (insertSpaceBeforeClosingParenInWhileOption != null) {
			this.insert_space_before_closing_paren_in_while = IDEPlugin.INSERT.equals(insertSpaceBeforeClosingParenInWhileOption);
		}
		final String insertSpaceBeforeColonInCaseOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CASE);
		final String insertSpaceBeforeColonInBaseClauseOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_BASE_CLAUSE);
		if (insertSpaceBeforeColonInBaseClauseOption != null) {
			this.insert_space_before_colon_in_base_clause = IDEPlugin.INSERT.equals(insertSpaceBeforeColonInBaseClauseOption);
		}
		if (insertSpaceBeforeColonInCaseOption != null) {
			this.insert_space_before_colon_in_case = IDEPlugin.INSERT.equals(insertSpaceBeforeColonInCaseOption);
		}
		final String insertSpaceBeforeColonInConditionalOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL);
		if (insertSpaceBeforeColonInConditionalOption != null) {
			this.insert_space_before_colon_in_conditional = IDEPlugin.INSERT.equals(insertSpaceBeforeColonInConditionalOption);
		}
		final String insertSpaceBeforeColonInDefaultOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT);
		if (insertSpaceBeforeColonInDefaultOption != null) {
			this.insert_space_before_colon_in_default = IDEPlugin.INSERT.equals(insertSpaceBeforeColonInDefaultOption);
		}
		final String insertSpaceBeforeColonInLabeledStatementOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT);
		if (insertSpaceBeforeColonInLabeledStatementOption != null) {
			this.insert_space_before_colon_in_labeled_statement = IDEPlugin.INSERT.equals(insertSpaceBeforeColonInLabeledStatementOption);
		}
		final String insertSpaceBeforeCommaInInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_INITIALIZER_LIST);
		if (insertSpaceBeforeCommaInInitializerListOption != null) {
			this.insert_space_before_comma_in_initializer_list = IDEPlugin.INSERT.equals(insertSpaceBeforeCommaInInitializerListOption);
		}
		final String insertSpaceBeforeCommaInEnumDeclarationsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ENUM_DECLARATIONS);
		if (insertSpaceBeforeCommaInEnumDeclarationsOption != null) {
			this.insert_space_before_comma_in_enum_declarations = IDEPlugin.INSERT.equals(insertSpaceBeforeCommaInEnumDeclarationsOption);
		}
		final String insertSpaceBeforeCommaInMethodInvocationArgumentsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_INVOCATION_ARGUMENTS);
		if (insertSpaceBeforeCommaInMethodInvocationArgumentsOption != null) {
			this.insert_space_before_comma_in_method_invocation_arguments = IDEPlugin.INSERT.equals(insertSpaceBeforeCommaInMethodInvocationArgumentsOption);
		}
		final String insertSpaceBeforeCommaInMethodDeclarationParametersOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_PARAMETERS);
		if (insertSpaceBeforeCommaInMethodDeclarationParametersOption != null) {
			this.insert_space_before_comma_in_method_declaration_parameters = IDEPlugin.INSERT.equals(insertSpaceBeforeCommaInMethodDeclarationParametersOption);
		}
		final String insertSpaceBeforeCommaInMethodDeclarationThrowsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_THROWS);
		if (insertSpaceBeforeCommaInMethodDeclarationThrowsOption != null) {
			this.insert_space_before_comma_in_method_declaration_throws = IDEPlugin.INSERT.equals(insertSpaceBeforeCommaInMethodDeclarationThrowsOption);
		}
		final String insertSpaceBeforeCommaInMultipleFieldDeclarationsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_DECLARATOR_LIST);
		if (insertSpaceBeforeCommaInMultipleFieldDeclarationsOption != null) {
			this.insert_space_before_comma_in_declarator_list = IDEPlugin.INSERT.equals(insertSpaceBeforeCommaInMultipleFieldDeclarationsOption);
		}
		final String insertSpaceBeforeCommaInMultipleLocalDeclarationsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPRESSION_LIST);
		if (insertSpaceBeforeCommaInMultipleLocalDeclarationsOption != null) {
			this.insert_space_before_comma_in_expression_list = IDEPlugin.INSERT.equals(insertSpaceBeforeCommaInMultipleLocalDeclarationsOption);
		}
		final String insertSpaceBeforeCommaInBaseTypesOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_BASE_TYPES);
		if (insertSpaceBeforeCommaInBaseTypesOption != null) {
			this.insert_space_before_comma_in_base_types = IDEPlugin.INSERT.equals(insertSpaceBeforeCommaInBaseTypesOption);
		}
		final String insertSpaceBeforeCommaInTemplateArgumentsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_TEMPLATE_ARGUMENTS);
		if (insertSpaceBeforeCommaInTemplateArgumentsOption != null) {
			this.insert_space_before_comma_in_template_arguments = IDEPlugin.INSERT.equals(insertSpaceBeforeCommaInTemplateArgumentsOption);
		}
		final String insertSpaceBeforeCommaInTemplateParametersOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_TEMPLATE_PARAMETERS);
		if (insertSpaceBeforeCommaInTemplateParametersOption != null) {
			this.insert_space_before_comma_in_template_parameters = IDEPlugin.INSERT.equals(insertSpaceBeforeCommaInTemplateParametersOption);
		}
		final String insertSpaceBeforeOpeningAngleBrackerInTemplateArgumentsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS);
		if (insertSpaceBeforeOpeningAngleBrackerInTemplateArgumentsOption != null) {
			this.insert_space_before_opening_angle_bracket_in_template_arguments = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningAngleBrackerInTemplateArgumentsOption);
		}
		final String insertSpaceBeforeOpeningAngleBrackerInTemplateParametersOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS);
		if (insertSpaceBeforeOpeningAngleBrackerInTemplateParametersOption != null) {
			this.insert_space_before_opening_angle_bracket_in_template_parameters = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningAngleBrackerInTemplateParametersOption);
		}
		final String insertSpaceBeforeOpeningBraceInInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_INITIALIZER_LIST);
		if (insertSpaceBeforeOpeningBraceInInitializerListOption != null) {
			this.insert_space_before_opening_brace_in_initializer_list = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningBraceInInitializerListOption);
		}
		final String insertSpaceBeforeOpeningBraceInBlockOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK);
		if (insertSpaceBeforeOpeningBraceInBlockOption != null) {
			this.insert_space_before_opening_brace_in_block = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningBraceInBlockOption);
		}
		final String insertSpaceBeforeOpeningBraceInMethodDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_METHOD_DECLARATION);
		if (insertSpaceBeforeOpeningBraceInMethodDeclarationOption != null) {
			this.insert_space_before_opening_brace_in_method_declaration = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningBraceInMethodDeclarationOption);
		}
		final String insertSpaceBeforeOpeningBraceInTypeDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_TYPE_DECLARATION);
		if (insertSpaceBeforeOpeningBraceInTypeDeclarationOption != null) {
			this.insert_space_before_opening_brace_in_type_declaration = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningBraceInTypeDeclarationOption);
		}
		final String insertSpaceBeforeOpeningBraceInNamespaceDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_NAMESPACE_DECLARATION);
		if (insertSpaceBeforeOpeningBraceInNamespaceDeclarationOption != null) {
			this.insert_space_before_opening_brace_in_namespace_declaration = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningBraceInNamespaceDeclarationOption);
		}
		final String insertSpaceBeforeOpeningBracketInArrayReferenceOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET);
		if (insertSpaceBeforeOpeningBracketInArrayReferenceOption != null) {
			this.insert_space_before_opening_bracket = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningBracketInArrayReferenceOption);
		}
		final String insertSpaceBeforeOpeningParenInCatchOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CATCH);
		if (insertSpaceBeforeOpeningParenInCatchOption != null) {
			this.insert_space_before_opening_paren_in_catch = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningParenInCatchOption);
		}
		final String insertSpaceBeforeOpeningParenInForOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_FOR);
		if (insertSpaceBeforeOpeningParenInForOption != null) {
			this.insert_space_before_opening_paren_in_for = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningParenInForOption);
		}
		final String insertSpaceBeforeOpeningParenInIfOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_IF);
		if (insertSpaceBeforeOpeningParenInIfOption != null) {
			this.insert_space_before_opening_paren_in_if = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningParenInIfOption);
		}
		final String insertSpaceBeforeOpeningParenInMethodInvocationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_INVOCATION);
		if (insertSpaceBeforeOpeningParenInMethodInvocationOption != null) {
			this.insert_space_before_opening_paren_in_method_invocation = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningParenInMethodInvocationOption);
		}
		final String insertSpaceBeforeOpeningParenInMethodDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_DECLARATION);
		if (insertSpaceBeforeOpeningParenInMethodDeclarationOption != null) {
			this.insert_space_before_opening_paren_in_method_declaration = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningParenInMethodDeclarationOption);
		}
		final String insertSpaceBeforeOpeningParenInExceptionSpecificationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_EXCEPTION_SPECIFICATION);
		if (insertSpaceBeforeOpeningParenInExceptionSpecificationOption != null) {
			this.insert_space_before_opening_paren_in_exception_specification = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningParenInExceptionSpecificationOption);
		}
		final String insertSpaceBeforeOpeningParenInSwitchOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SWITCH);
		if (insertSpaceBeforeOpeningParenInSwitchOption != null) {
			this.insert_space_before_opening_paren_in_switch = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningParenInSwitchOption);
		}
		final String insertSpaceBeforeOpeningBraceInSwitchOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_SWITCH);
		if (insertSpaceBeforeOpeningBraceInSwitchOption != null) {
			this.insert_space_before_opening_brace_in_switch = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningBraceInSwitchOption);
		}
		final String insertSpaceBeforeOpeningParenInParenthesizedExpressionOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION);
		if (insertSpaceBeforeOpeningParenInParenthesizedExpressionOption != null) {
			this.insert_space_before_opening_paren_in_parenthesized_expression = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningParenInParenthesizedExpressionOption);
		}
		final String insertSpaceBeforeOpeningParenInWhileOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_WHILE);
		if (insertSpaceBeforeOpeningParenInWhileOption != null) {
			this.insert_space_before_opening_paren_in_while = IDEPlugin.INSERT.equals(insertSpaceBeforeOpeningParenInWhileOption);
		}
		final String insertSpaceBeforePostfixOperatorOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR);
		if (insertSpaceBeforePostfixOperatorOption != null) {
			this.insert_space_before_postfix_operator = IDEPlugin.INSERT.equals(insertSpaceBeforePostfixOperatorOption);
		}
		final String insertSpaceBeforePrefixOperatorOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PREFIX_OPERATOR);
		if (insertSpaceBeforePrefixOperatorOption != null) {
			this.insert_space_before_prefix_operator = IDEPlugin.INSERT.equals(insertSpaceBeforePrefixOperatorOption);
		}
		final String insertSpaceBeforeQuestionInConditionalOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL);
		if (insertSpaceBeforeQuestionInConditionalOption != null) {
			this.insert_space_before_question_in_conditional = IDEPlugin.INSERT.equals(insertSpaceBeforeQuestionInConditionalOption);
		}
		final String insertSpaceBeforeSemicolonOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON);
		if (insertSpaceBeforeSemicolonOption != null) {
			this.insert_space_before_semicolon = IDEPlugin.INSERT.equals(insertSpaceBeforeSemicolonOption);
		}
		final String insertSpaceBeforeSemicolonInForOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON_IN_FOR);
		if (insertSpaceBeforeSemicolonInForOption != null) {
			this.insert_space_before_semicolon_in_for = IDEPlugin.INSERT.equals(insertSpaceBeforeSemicolonInForOption);
		}
		final String insertSpaceBeforeUnaryOperatorOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_UNARY_OPERATOR);
		if (insertSpaceBeforeUnaryOperatorOption != null) {
			this.insert_space_before_unary_operator = IDEPlugin.INSERT.equals(insertSpaceBeforeUnaryOperatorOption);
		}
		final String insertSpaceBetweenEmptyBracesInInitializerListOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACES_IN_INITIALIZER_LIST);
		if (insertSpaceBetweenEmptyBracesInInitializerListOption != null) {
			this.insert_space_between_empty_braces_in_initializer_list = IDEPlugin.INSERT.equals(insertSpaceBetweenEmptyBracesInInitializerListOption);
		}
		final String insertSpaceBetweenEmptyBracketsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACKETS);
		if (insertSpaceBetweenEmptyBracketsOption != null) {
			this.insert_space_between_empty_brackets = IDEPlugin.INSERT.equals(insertSpaceBetweenEmptyBracketsOption);
		}
		final String insertSpaceBetweenEmptyParensInMethodDeclarationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_DECLARATION);
		if (insertSpaceBetweenEmptyParensInMethodDeclarationOption != null) {
			this.insert_space_between_empty_parens_in_method_declaration = IDEPlugin.INSERT.equals(insertSpaceBetweenEmptyParensInMethodDeclarationOption);
		}
		final String insertSpaceBetweenEmptyParensInMethodInvocationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_INVOCATION);
		if (insertSpaceBetweenEmptyParensInMethodInvocationOption != null) {
			this.insert_space_between_empty_parens_in_method_invocation = IDEPlugin.INSERT.equals(insertSpaceBetweenEmptyParensInMethodInvocationOption);
		}
		final String insertSpaceBetweenEmptyParensInExceptionSpecificationOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_EXCEPTION_SPECIFICATION);
		if (insertSpaceBetweenEmptyParensInExceptionSpecificationOption != null) {
			this.insert_space_between_empty_parens_in_exception_specification = IDEPlugin.INSERT.equals(insertSpaceBetweenEmptyParensInExceptionSpecificationOption);
		}
		final String compactElseIfOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_COMPACT_ELSE_IF);
		if (compactElseIfOption != null) {
			this.compact_else_if = STPDefaultCodeFormatterConstants.TRUE.equals(compactElseIfOption);
		}
		final String keepGuardianClauseOnOneLineOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_KEEP_GUARDIAN_CLAUSE_ON_ONE_LINE);
		if (keepGuardianClauseOnOneLineOption != null) {
			this.keep_guardian_clause_on_one_line = STPDefaultCodeFormatterConstants.TRUE.equals(keepGuardianClauseOnOneLineOption);
		}
		final String keepElseStatementOnSameLineOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_KEEP_ELSE_STATEMENT_ON_SAME_LINE);
		if (keepElseStatementOnSameLineOption != null) {
			this.keep_else_statement_on_same_line = STPDefaultCodeFormatterConstants.TRUE.equals(keepElseStatementOnSameLineOption);
		}
		final String keepEmptyInitializerListOnOneLineOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_KEEP_EMPTY_INITIALIZER_LIST_ON_ONE_LINE);
		if (keepEmptyInitializerListOnOneLineOption != null) {
			this.keep_empty_initializer_list_on_one_line = STPDefaultCodeFormatterConstants.TRUE.equals(keepEmptyInitializerListOnOneLineOption);
		}
		final String keepSimpleIfOnOneLineOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_KEEP_SIMPLE_IF_ON_ONE_LINE);
		if (keepSimpleIfOnOneLineOption != null) {
			this.keep_simple_if_on_one_line = STPDefaultCodeFormatterConstants.TRUE.equals(keepSimpleIfOnOneLineOption);
		}
		final String keepThenStatementOnSameLineOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_KEEP_THEN_STATEMENT_ON_SAME_LINE);
		if (keepThenStatementOnSameLineOption != null) {
			this.keep_then_statement_on_same_line = STPDefaultCodeFormatterConstants.TRUE.equals(keepThenStatementOnSameLineOption);
		}
		final String numberOfEmptyLinesToPreserveOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE);
		if (numberOfEmptyLinesToPreserveOption != null) {
			try {
				this.number_of_empty_lines_to_preserve = Integer.parseInt(numberOfEmptyLinesToPreserveOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.number_of_empty_lines_to_preserve = 0;
			}
		}
		final String joinWrappedLinesOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_JOIN_WRAPPED_LINES);
		if (joinWrappedLinesOption != null) {
			this.join_wrapped_lines = STPDefaultCodeFormatterConstants.TRUE.equals(joinWrappedLinesOption);
		}
		final String putEmptyStatementOnNewLineOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE);
		if (putEmptyStatementOnNewLineOption != null) {
			this.put_empty_statement_on_new_line = STPDefaultCodeFormatterConstants.TRUE.equals(putEmptyStatementOnNewLineOption);
		}
		final String tabSizeOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
		if (tabSizeOption != null) {
			try {
				this.tab_size = Integer.parseInt(tabSizeOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.tab_size = 4;
			}
		}
		final String useTabsOnlyForLeadingIndentationsOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_USE_TABS_ONLY_FOR_LEADING_INDENTATIONS);
		if (useTabsOnlyForLeadingIndentationsOption != null) {
			this.use_tabs_only_for_leading_indentations = STPDefaultCodeFormatterConstants.TRUE.equals(useTabsOnlyForLeadingIndentationsOption);
		}
		final String pageWidthOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT);
		if (pageWidthOption != null) {
			try {
				this.page_width = Integer.parseInt(pageWidthOption);
			} catch (NumberFormatException|ClassCastException e) {
				this.page_width = 80;
			}
		}
		final String useTabOption = settings.get(STPDefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		if (useTabOption != null) {
			if (IDEPlugin.TAB.equals(useTabOption)) {
				this.tab_char = TAB;
			} else if (IDEPlugin.SPACE.equals(useTabOption)) {
				this.tab_char = SPACE;
			} else {
				this.tab_char = MIXED;
			}
		}
	}

	private void setDefaultSettings() {
		this.alignment_for_arguments_in_method_invocation = STPAlignment.M_COMPACT_SPLIT;
		this.alignment_for_assignment = STPAlignment.M_COMPACT_SPLIT;
		this.alignment_for_base_clause_in_type_declaration = STPAlignment.M_NEXT_PER_LINE_SPLIT;
		this.alignment_for_binary_expression = STPAlignment.M_COMPACT_SPLIT;
		this.alignment_for_compact_if = STPAlignment.M_COMPACT_SPLIT;
		this.alignment_for_conditional_expression = STPAlignment.M_COMPACT_FIRST_BREAK_SPLIT | STPAlignment.M_INDENT_ON_COLUMN;
		this.alignment_for_conditional_expression_chain = STPAlignment.M_COMPACT_SPLIT | STPAlignment.M_INDENT_ON_COLUMN;
		this.alignment_for_declarator_list = STPAlignment.M_COMPACT_SPLIT;
		this.alignment_for_enumerator_list = STPAlignment.M_ONE_PER_LINE_SPLIT;
		this.alignment_for_expressions_in_initializer_list = STPAlignment.M_COMPACT_SPLIT;
		this.alignment_for_member_access = STPAlignment.M_NO_ALIGNMENT;
		this.alignment_for_overloaded_left_shift_chain = STPAlignment.M_COMPACT_SPLIT;
		this.alignment_for_parameters_in_method_declaration = STPAlignment.M_COMPACT_SPLIT;
		this.alignment_for_throws_clause_in_method_declaration = STPAlignment.M_COMPACT_SPLIT;
		this.brace_position_for_block = STPDefaultCodeFormatterConstants.END_OF_LINE;
		this.brace_position_for_block_in_case = STPDefaultCodeFormatterConstants.END_OF_LINE;
		this.brace_position_for_initializer_list = STPDefaultCodeFormatterConstants.END_OF_LINE;
		this.brace_position_for_method_declaration = STPDefaultCodeFormatterConstants.END_OF_LINE;
		this.brace_position_for_namespace_declaration = STPDefaultCodeFormatterConstants.END_OF_LINE;
		this.brace_position_for_switch = STPDefaultCodeFormatterConstants.END_OF_LINE;
		this.brace_position_for_type_declaration = STPDefaultCodeFormatterConstants.END_OF_LINE;
		this.comment_min_distance_between_code_and_line_comment = 1;
		this.comment_preserve_white_space_between_code_and_line_comment = true;
		this.never_indent_line_comments_on_first_column = true;
		this.continuation_indentation = 2;
		this.continuation_indentation_for_initializer_list = 2;
		this.indent_statements_compare_to_block = true;
		this.indent_statements_compare_to_body = true;
		this.indent_body_declarations_compare_to_namespace_header = false;
		this.indent_body_declarations_compare_to_access_specifier = true;
		this.indent_breaks_compare_to_cases = true;
		this.indent_empty_lines = false;
		this.indent_switchstatements_compare_to_cases = true;
		this.indent_switchstatements_compare_to_switch = false;
		this.indentation_size = 4;
		this.insert_new_line_after_opening_brace_in_initializer_list = false;
		this.insert_new_line_at_end_of_file_if_missing = false;
		this.insert_new_line_before_catch_in_try_statement = false;
		this.insert_new_line_before_closing_brace_in_initializer_list = false;
		this.insert_new_line_before_else_in_if_statement = false;
		this.insert_new_line_before_while_in_do_statement = false;
		this.insert_new_line_before_identifier_in_function_declaration = false;
		this.insert_new_line_in_empty_block = true;
		this.insert_space_after_assignment_operator = true;
		this.insert_space_after_binary_operator = true;
		this.insert_space_after_closing_angle_bracket_in_template_arguments = true;
		this.insert_space_after_closing_angle_bracket_in_template_parameters = true;
		this.insert_space_after_closing_paren_in_cast = true;
		this.insert_space_after_closing_brace_in_block = true;
		this.insert_space_after_colon_in_base_clause = true;
		this.insert_space_after_colon_in_case = true;
		this.insert_space_after_colon_in_conditional = true;
		this.insert_space_after_colon_in_labeled_statement = true;
		this.insert_space_after_comma_in_initializer_list = true;
		this.insert_space_after_comma_in_enum_declarations = true;
		this.insert_space_after_comma_in_method_invocation_arguments = true;
		this.insert_space_after_comma_in_method_declaration_parameters = true;
		this.insert_space_after_comma_in_method_declaration_throws = true;
		this.insert_space_after_comma_in_declarator_list = true;
		this.insert_space_after_comma_in_expression_list = true;
		this.insert_space_after_comma_in_base_types = true;
		this.insert_space_after_comma_in_template_arguments = true;
		this.insert_space_after_comma_in_template_parameters = true;
		this.insert_space_after_opening_angle_bracket_in_template_arguments = false;
		this.insert_space_after_opening_angle_bracket_in_template_parameters = false;
		this.insert_space_after_opening_bracket = false;
		this.insert_space_after_opening_brace_in_initializer_list = true;
		this.insert_space_after_opening_paren_in_cast = false;
		this.insert_space_after_opening_paren_in_catch = false;
		this.insert_space_after_opening_paren_in_for = false;
		this.insert_space_after_opening_paren_in_if = false;
		this.insert_space_after_opening_paren_in_method_declaration = false;
		this.insert_space_after_opening_paren_in_method_invocation = false;
		this.insert_space_after_opening_paren_in_parenthesized_expression = false;
		this.insert_space_after_opening_paren_in_switch = false;
		this.insert_space_after_opening_paren_in_while = false;
		this.insert_space_after_postfix_operator = false;
		this.insert_space_after_prefix_operator = false;
		this.insert_space_after_question_in_conditional = true;
		this.insert_space_after_semicolon_in_for = true;
		this.insert_space_after_unary_operator = false;
		this.insert_space_before_assignment_operator = true;
		this.insert_space_before_binary_operator = true;
		this.insert_space_before_closing_angle_bracket_in_template_arguments = false;
		this.insert_space_before_closing_angle_bracket_in_template_parameters = false;
		this.insert_space_before_closing_brace_in_initializer_list = true;
		this.insert_space_before_closing_bracket = false;
		this.insert_space_before_closing_paren_in_cast = false;
		this.insert_space_before_closing_paren_in_catch = false;
		this.insert_space_before_closing_paren_in_for = false;
		this.insert_space_before_closing_paren_in_if = false;
		this.insert_space_before_closing_paren_in_method_declaration = false;
		this.insert_space_before_closing_paren_in_method_invocation = false;
		this.insert_space_before_closing_paren_in_parenthesized_expression = false;
		this.insert_space_before_closing_paren_in_switch = false;
		this.insert_space_before_closing_paren_in_while = false;
		this.insert_space_before_colon_in_base_clause = false;
		this.insert_space_before_colon_in_case = false;
		this.insert_space_before_colon_in_conditional = true;
		this.insert_space_before_colon_in_default = false;
		this.insert_space_before_colon_in_labeled_statement = false;
		this.insert_space_before_comma_in_initializer_list = false;
		this.insert_space_before_comma_in_enum_declarations = false;
		this.insert_space_before_comma_in_method_invocation_arguments = false;
		this.insert_space_before_comma_in_method_declaration_parameters = false;
		this.insert_space_before_comma_in_method_declaration_throws = false;
		this.insert_space_before_comma_in_declarator_list = false;
		this.insert_space_before_comma_in_expression_list = false;
		this.insert_space_before_comma_in_base_types = false;
		this.insert_space_before_comma_in_template_arguments = false;
		this.insert_space_before_comma_in_template_parameters = false;
		this.insert_space_before_opening_angle_bracket_in_template_arguments = false;
		this.insert_space_before_opening_angle_bracket_in_template_parameters = false;
		this.insert_space_before_opening_brace_in_initializer_list = true;
		this.insert_space_before_opening_brace_in_block = true;
		this.insert_space_before_opening_brace_in_method_declaration = true;
		this.insert_space_before_opening_brace_in_switch = true;
		this.insert_space_before_opening_brace_in_type_declaration = true;
		this.insert_space_before_opening_brace_in_namespace_declaration = true;
		this.insert_space_before_opening_bracket = false;
		this.insert_space_before_opening_paren_in_catch = true;
		this.insert_space_before_opening_paren_in_exception_specification = true;
		this.insert_space_before_opening_paren_in_for = true;
		this.insert_space_before_opening_paren_in_if = true;
		this.insert_space_before_opening_paren_in_method_invocation = false;
		this.insert_space_before_opening_paren_in_method_declaration = false;
		this.insert_space_before_opening_paren_in_switch = true;
		this.insert_space_before_opening_paren_in_parenthesized_expression = false;
		this.insert_space_before_opening_paren_in_while = true;
		this.insert_space_before_postfix_operator = false;
		this.insert_space_before_prefix_operator = false;
		this.insert_space_before_question_in_conditional = true;
		this.insert_space_before_semicolon = false;
		this.insert_space_before_semicolon_in_for = false;
		this.insert_space_before_unary_operator = false;
		this.insert_space_between_empty_braces_in_initializer_list = false;
		this.insert_space_between_empty_brackets = false;
		this.insert_space_between_empty_parens_in_method_declaration = false;
		this.insert_space_between_empty_parens_in_method_invocation = false;
		this.insert_space_between_empty_parens_in_exception_specification = false;
		this.compact_else_if = true;
		this.keep_guardian_clause_on_one_line = false;
		this.keep_else_statement_on_same_line = false;
		this.keep_empty_initializer_list_on_one_line = false;
		this.keep_simple_if_on_one_line = false;
		this.keep_then_statement_on_same_line = false;
		this.number_of_empty_lines_to_preserve = 1;
		this.join_wrapped_lines = true;
		this.put_empty_statement_on_new_line = true;
		this.tab_size = 4;
		this.page_width = 80;
		this.tab_char = TAB;
		this.use_tabs_only_for_leading_indentations = false;
	}
}
