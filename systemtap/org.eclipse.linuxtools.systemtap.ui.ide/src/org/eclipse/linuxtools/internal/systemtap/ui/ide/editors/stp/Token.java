/*******************************************************************************
 * Copyright (c) 2004, 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Anton Leherbauer - adding tokens for preprocessing directives
 *     Markus Schorn - classification of preprocessing directives.
 *     Red Hat Inc. - used in SystemTap editor
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

public interface Token {
    // Token types
     int tCLASSNAME = 53;
     int t_and = 54;
     int t_and_eq = 55;
     int t_asm = 56;
     int t_auto = 57;
     int t_bitand = 58;
     int t_bitor = 59;
     int t_bool = 60;
     int t_break = 61;
     int t_case = 62;
     int t_catch = 63;
     int t_char = 64;
     int t_class = 65;
     int t_compl = 66;
     int t_const = 67;
     int t_const_cast = 69;
     int t_continue = 70;
     int t_default = 71;
     int t_delete = 72;
     int t_do = 73;
     int t_double = 74;
     int t_dynamic_cast = 75;
     int t_else = 76;
     int t_enum = 77;
     int t_explicit = 78;
     int t_export = 79;
     int t_extern = 80;
     int t_false = 81;
     int t_float = 82;
     int t_for = 83;
     int t_friend = 84;
     int t_goto = 85;
     int t_if = 86;
     int t_inline = 87;
     int t_int = 88;
     int t_long = 89;
     int t_mutable = 90;
     int t_namespace = 91;
     int t_new = 92;
     int t_not = 93;
     int t_not_eq = 94;
     int t_operator = 95;
     int t_or = 96;
     int t_or_eq = 97;
     int t_private = 98;
     int t_protected = 99;
     int t_public = 100;
     int t_register = 101;
     int t_reinterpret_cast = 102;
     int t_return = 103;
     int t_short = 104;
     int t_sizeof = 105;
     int t_static = 106;
     int t_static_cast = 107;
     int t_signed = 108;
     int t_struct = 109;
     int t_switch = 110;
     int t_template = 111;
     int t_this = 112;
     int t_throw = 113;
     int t_true = 114;
     int t_try = 115;
     int t_typedef = 116;
     int t_typeid = 117;
     int t_typename = 118;
     int t_union = 119;
     int t_unsigned = 120;
     int t_using = 121;
     int t_virtual = 122;
     int t_void = 123;
     int t_volatile = 124;
     int t_wchar_t = 125;
     int t_while = 126;
     int t_xor = 127;
     int t_xor_eq = 128;
     int tSTRING = 129;
     int tFLOATINGPT = 130;
     int tLSTRING = 131;
     int tCHAR = 132;
     int tRSTRING = 133;
     int t_restrict = 136;
     int t_interface = 200;
     int t_import = 201;
     int t_instanceof = 202;
     int t_extends = 203;
     int t_implements = 204;
     int t_final = 205;
     int t_super = 206;
     int t_package = 207;
     int t_boolean = 208;
     int t_abstract = 209;
     int t_finally = 210;
     int t_null = 211;
     int t_synchronized = 212;
     int t_throws = 213;
     int t_byte = 214;
     int t_transient = 215;
     int t_native = 216;
}
