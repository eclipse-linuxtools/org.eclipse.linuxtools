/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover.texinfoparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TexinfoUtils {

    static final Pattern ParmBracketPattern = Pattern.compile("\\((.*?)\\)");
    static final Pattern IndexPattern        = Pattern.compile("@\\w*index\\s+[a-zA-Z0-9_@\\{\\}]*");
    static final Pattern IndexPattern2        = Pattern.compile("@\\w*index\\{[a-zA-Z0-9_@\\{\\}]*\\}");
    static final Pattern ExampleItem        = Pattern.compile("(@example)((.*?<br><eol>)*+.*?)(<eol>.*@end\\s+example)");
    static final Pattern ExamplePattern        = Pattern.compile("@example");
    static final Pattern EndExamplePattern  = Pattern.compile("@end\\s+example");
    static final Pattern SmallExampleItem    = Pattern.compile("(@smallexample)((.*?<br><eol>)*+.*?)(<eol>.*@end\\s+smallexample)");
    static final Pattern SmallExamplePattern     = Pattern.compile("@smallexample");
    static final Pattern StrongPattern        = Pattern.compile("@strong\\{(\\w*)\\}(@\\*)?");
    static final Pattern EndSmallExamplePattern  = Pattern.compile("@end\\s+smallexample");
    static final Pattern EnumeratePattern    = Pattern.compile("@enumerate");
    static final Pattern EndEnumeratePattern  = Pattern.compile("@end\\s+enumerate");
    static final Pattern VerbatimPattern    = Pattern.compile("@verbatim");
    static final Pattern ItemPattern        = Pattern.compile("@item");
    static final Pattern RefillPattern        = Pattern.compile("@refill");
    static final Pattern NoIndentPattern    = Pattern.compile("@noindent");
    static final Pattern QuotationPattern    = Pattern.compile("@quotation");
    static final Pattern EndQuotation        = Pattern.compile("@end\\s+quotation");
    static final Pattern GroupPattern        = Pattern.compile("@group");
    static final Pattern TabPattern            = Pattern.compile("@tab");
    static final Pattern DeftpPattern        = Pattern.compile("@deftp\\s+\\{([^\\}]*?)\\}\\s*\\{([^\\}]*?)\\}");
    static final Pattern EndDeftpPattern    = Pattern.compile("@end\\s+deftp");
    static final Pattern CommentPattern        = Pattern.compile("@c<eol>|@c\\s+.*?<eol>");
    static final Pattern EndGroupPattern    = Pattern.compile("@end\\s+group");
    static final Pattern BracketRefPattern     = Pattern.compile("\\(@.?.?ref\\{[^\\}]*\\}\\)");
    static final Pattern BRPattern            = Pattern.compile("&lt;br&gt;");
    static final Pattern EOLPattern            = Pattern.compile("&lt;eol&gt;");
    static final Pattern EndVerbatimPattern = Pattern.compile("@end\\s+verbatim");
    static final Pattern TableSampItemPattern = Pattern.compile("(@table\\s*@samp.*?)@item\\s+(.*?)(<eol>.*@end\\s+table)", Pattern.DOTALL);
    static final Pattern TableAsisItemPattern = Pattern.compile("(@table\\s*@asis.*?)@item\\s+(.*?)(<eol>.*@end\\+table)", Pattern.DOTALL);
    static final Pattern TableCodeItemPattern = Pattern.compile("(@table\\s*@code.*?)@item\\s+(.*?)(<eol>.*?)(@end\\s+table)", Pattern.DOTALL);
    static final Pattern TableVarItemPattern  = Pattern.compile("(@table\\s*@var.*?)@item\\s+(.*?)(<eol>.*@end\\s+table)", Pattern.DOTALL);
    static final Pattern VtableCodeItemPattern = Pattern.compile("(@vtable\\s*@code.*?)@item\\s+(.*?)(<eol>.*@end\\s+vtable)", Pattern.DOTALL);
    static final Pattern MultitableItemPattern = Pattern.compile("(@multitable.*)@item\\s+(.*?)<eol>(.*?@end\\s+multitable)" +
                                                                 "|(@multitable.*)@item\\s*<eol>(.*?)<eol>(.*?@end\\s+multitable)", Pattern.DOTALL);
    static final Pattern TableCodeMatchPattern = Pattern.compile("(@table\\s+@code)(.*?)(@end\\s+table)", Pattern.DOTALL);
    static final Pattern TableSampMatchPattern = Pattern.compile("(@table\\s+@samp)(.*?)(@end\\s+table)", Pattern.DOTALL);
    static final Pattern TableAsisMatchPattern = Pattern.compile("(@table\\s+@asis)(.*?)(@end\\s+table)", Pattern.DOTALL);
    static final Pattern TableVarMatchPattern  = Pattern.compile("(@table\\s+@var)(.*?)(@end\\s+table)", Pattern.DOTALL);
    static final Pattern TableSampPattern     = Pattern.compile("@table\\s*@samp", Pattern.MULTILINE);
    static final Pattern TableAsisPattern    = Pattern.compile("@table\\s*@asis", Pattern.MULTILINE);
    static final Pattern TableCodePattern    = Pattern.compile("@table\\s*@code", Pattern.MULTILINE);
    static final Pattern TableVarPattern    = Pattern.compile("@table\\s*@var", Pattern.MULTILINE);
    static final Pattern VtableCodePattern    = Pattern.compile("@vtable\\s*@code", Pattern.MULTILINE);
    static final Pattern MultitablePattern    = Pattern.compile("@multitable.*?<DT>", Pattern.DOTALL);
    static final Pattern EndTablePattern     = Pattern.compile("@end\\s+table");
    static final Pattern EndVtablePattern     = Pattern.compile("@end\\s+vtable");
    static final Pattern EndMultitablePattern     = Pattern.compile("@end\\s+multitable");
    static final Pattern DotsPattern        = Pattern.compile("@dots\\{\\}");
    static final Pattern ItemizeMinusPattern= Pattern.compile("@itemize\\s+@minus" + "(.*?)" + "@end\\s+itemize", Pattern.DOTALL);
    static final Pattern ItemizeBulletPattern= Pattern.compile("@itemize\\s+@bullet" + "(.*?)" + "@end\\s+itemize", Pattern.DOTALL);
    static final Pattern InfoOnlyPattern    = Pattern.compile("@ifinfo.*?@end\\s+ifinfo", Pattern.DOTALL);
    static final Pattern TexOnlyPattern        = Pattern.compile("@iftex.*?@end\\s+iftex", Pattern.DOTALL);
    static final Pattern TexPattern            = Pattern.compile("@tex.*?@end\\s+tex", Pattern.DOTALL);
    static final Pattern IgnorePattern        = Pattern.compile("@ignore.*?@end\\s+ignore", Pattern.DOTALL);
    static final Pattern IfInfoPattern        = Pattern.compile("@ifinfo");
    static final Pattern InfinityPattern    = Pattern.compile("@infinity");
    static final Pattern EndIfInfoPattern    = Pattern.compile("@end\\s+ifinfo");
    static final Pattern NotTexPattern        = Pattern.compile("@ifnottex");
    static final Pattern EndNotTexPattern    = Pattern.compile("@end\\s+ifnottex");
    static final Pattern DeftypevrPattern    = Pattern.compile("@deftypevr\\s+");
    static final Pattern EndDeftypevr        = Pattern.compile("@end\\s+deftypevr");
    static final Pattern XrefPattern        = Pattern.compile("@xref\\{[^\\}]*\\}", Pattern.MULTILINE);
    static final Pattern PxrefPattern        = Pattern.compile("@pxref\\{[^\\}]*\\}", Pattern.MULTILINE);
    static final Pattern AtTPattern            = Pattern.compile("@t\\{([^\\}]*)\\}");
    static final Pattern CommandPattern        = Pattern.compile("@command\\{([^\\}]*)\\}");
    static final Pattern KbdPattern            = Pattern.compile("@kbd\\{([^\\}]*)\\}");
    static final Pattern RPattern            = Pattern.compile("@r\\{([^\\}]*)\\}");
    static final Pattern FilePattern        = Pattern.compile("@file\\{([^\\}]*)\\}");
    static final Pattern VarPattern            = Pattern.compile("@var\\{([^\\}]*)\\}");
    static final Pattern OVarPattern        = Pattern.compile("@ovar\\{([^\\}]*)\\}");
    static final Pattern DVarPattern        = Pattern.compile("@dvar\\{([^\\},\\,]*),([^\\}]*)\\}");
    static final Pattern AnyVarPattern        = Pattern.compile("@[a-z]*var\\{([^\\}]*)\\}");
    static final Pattern CodePattern        = Pattern.compile("@code\\{([^\\}]*)\\}");
    static final Pattern EmphPattern        = Pattern.compile("@emph\\{([^\\}]*)\\}");
    static final Pattern SampPattern        = Pattern.compile("@samp\\{([^\\}]*)\\}");
    static final Pattern OptionPattern        = Pattern.compile("@option\\{([^\\}]*)\\}");
    static final Pattern TagPattern            = Pattern.compile("@\\w+\\{([^\\}]*)\\}");
    static final Pattern TagCharPattern        = Pattern.compile("@([\\{\\}\\:\\*\"])");
    static final Pattern StrandedPeriod        = Pattern.compile("(\\w)\\s+\\.\\s+");
    static final Pattern LeftOverPeriod        = Pattern.compile("^\\.\\s+<eol>");
    static final Pattern AmpersandPattern    = Pattern.compile("&");
    static final Pattern LeftAnglePattern    = Pattern.compile("<");
    static final Pattern RightAnglePattern    = Pattern.compile(">");

    public static String stripProtoTags(String tt) {
        Matcher mm;
        String ss = "";

        ss = "";
        while (ss != tt) {
            mm = AnyVarPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("$1");
        }

        ss = "";
        while (ss != tt) {
            mm = DotsPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("...");
        }

        ss = "";
        while (ss != tt) {
            mm = CodePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("$1");
        }

        ss = "";
        while (ss != tt) {
            mm = AtTPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("$1");
        }

        ss = "";
        while (ss != tt) {
            mm = TagPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = TagCharPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

    return tt;
    }

    public static String transformTags(String tt) {
        Matcher mm;
        String ss = "";
        int endtableIndex = Integer.MAX_VALUE;

        ss = "";
        while (ss != tt) {
            mm = BracketRefPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = XrefPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = CommentPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<eol>");
        }

        ss = "";
        while (ss != tt) {
            mm = PxrefPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = DeftypevrPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = StrongPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<h3>$1</h3>");
        }

        ss = "";
        while (ss != tt) {
            mm = EndDeftypevr.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = IndexPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = IndexPattern2.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = NoIndentPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = GroupPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = EndGroupPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = QuotationPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = EndQuotation.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = InfinityPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("infinity");
        }

        ss = "";
        while (ss != tt) {
            mm = DeftpPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<h4>$1 - $2</h4>");
        }

        ss = "";
        while (ss != tt) {
            mm = EndDeftpPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = RefillPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = VarPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<VAR>$1</VAR>");
        }

        ss = "";
        while (ss != tt) {
            mm = DotsPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<small>...</small>");
        }

        ss = "";
        while (ss != tt) {
            mm = CommandPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<CODE>$1</CODE>");
        }

        ss = "";
        while (ss != tt) {
            mm = CodePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<CODE>$1</CODE>");
        }

        ss = "";
        while (ss != tt) {
            mm = KbdPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<KBD>$1</KBD>");
        }

        ss = "";
        while (ss != tt) {
            mm = EmphPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<EM>$1</EM>");
        }

        ss = "";
        while (ss != tt) {
            mm = FilePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<TT>$1</TT>");
        }


        ss = "";
        while (ss != tt) {
            mm = VerbatimPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<CODE>");
        }

        ss = "";
        while (ss != tt) {
            mm = EndVerbatimPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("</CODE>");
        }

        ss = "";
        while (ss != tt) {
            mm = SampPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<samp>$1</samp>");
        }

        ss = "";
        while (ss != tt) {
            mm = OptionPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<samp>$1</samp>");
        }

        ss = "";
        while (ss != tt) {
            mm = ExampleItem.matcher(tt);
            ss = tt;
            // We want to add a break at the end of each example line to preserve
            // formatting when Eclipse processes the xml (e.g. C code lines).
            tt = mm.replaceAll("$1$2<br>$4");
        }

        ss = "";
        while (ss != tt) {
            mm = ExamplePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<TABLE><tr><td>&nbsp;</td><td class=example><pre>");
        }

        ss = "";
        while (ss != tt) {
            mm = EndExamplePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("</pre></td></tr></table>");
        }

        ss = "";
        while (ss != tt) {
            mm = SmallExampleItem.matcher(tt);
            ss = tt;
            // We want to add a break at the end of each example line to preserve
            // formatting when Eclipse processes the xml (e.g. C code lines).
            tt = mm.replaceAll("$1$2<br>$4");
        }

        ss = "";
        while (ss != tt) {
            mm = SmallExamplePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<pre>");
        }

        ss = "";
        while (ss != tt) {
            mm = EndSmallExamplePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("</pre>");
        }

        ss = "";
        while (ss != tt) {
            mm = EnumeratePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<OL>");
        }

        ss = "";
        while (ss != tt) {
            mm = EndEnumeratePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("</OL>");
        }

//        ss = "";
//        while (ss != tt) {
//            mm = InfoOnlyPattern.matcher(tt);
//            ss = tt;
//            tt = mm.replaceAll("");
//        }

        ss = "";
        while (ss != tt) {
            mm = TexOnlyPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = TexPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = IgnorePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = NotTexPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = EndNotTexPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        // We will treat ifinfo the same as ifnottex which we pass through.
        ss = "";
        while (ss != tt) {
            mm = IfInfoPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = EndIfInfoPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        endtableIndex = Integer.MAX_VALUE;
        while (ss != tt) {
            mm = TableSampItemPattern.matcher(tt);
            ss = tt;
            if (mm.find()) {
                if (mm.start(4) > endtableIndex) {
                    // We try and find the last @[table samp] marker we can find with an @item before @[end table].
                    // The end marker has moved forward to make the match which means we have exhausted all @item markers
                    // from the last @[table samp] and @[end table] we were using prior to this iteration of the loop.
                    // If we change the table markers now, we avoid the prior table from
                    // matching any @item marker that sits between tables.
                    Matcher mm2 = TableSampMatchPattern.matcher(tt);
                    tt = mm2.replaceFirst("<DL>\n$2\n</DL>");
                    mm2 = TableSampMatchPattern.matcher(tt);
                    if (mm2.find())
                        endtableIndex = mm2.start(3);
                }
                else {
                    tt = mm.replaceFirst("$1<DT><SAMP>$2</SAMP>\n<DD>$3$4");
                    endtableIndex = mm.end(1) + 10 + mm.group(2).length() + 12 + mm.group(3).length();
                }
            }
        }

        //FIXME: This parser assumes that a table does not have an @itemize element
        //       inside it.  It allows the opposite to be true (i.e. a table inside
        //       an @itemize element.

        ss = "";
        endtableIndex = Integer.MAX_VALUE;
        while (ss != tt) {
            mm = TableAsisItemPattern.matcher(tt);
            ss = tt;
            if (mm.find()) {
                if (mm.start(4) > endtableIndex) {
                    // We try and find the last @[table asis] marker we can find with an @item before @[end table].
                    // The end marker has moved forward to make the match which means we have exhausted all @item markers
                    // from the last @[table asis] and @[end table] we were using prior to this iteration of the loop.
                    // If we change the table markers now, we avoid the prior table from
                    // matching any @item marker that sits between tables.
                    Matcher mm2 = TableAsisMatchPattern.matcher(tt);
                    tt = mm2.replaceFirst("<DL>\n$2\n</DL>");
                    mm2 = TableAsisMatchPattern.matcher(tt);
                    if (mm2.find())
                        endtableIndex = mm2.start(3);
                }
                else {
                    tt = mm.replaceFirst("$1<DT>$2\n<DD>$3$4");
                    endtableIndex = mm.end(1) + 10 + mm.group(2).length() + 12 + mm.group(3).length();
                }
            }
        }

        ss = "";
        endtableIndex = Integer.MAX_VALUE;
        while (ss != tt) {
            mm = TableCodeItemPattern.matcher(tt);
            ss = tt;
            if (mm.find()) {
                if (mm.start(4) > endtableIndex) {
                    // We try and find the last @[table code] marker we can find with an @item before @[end table].
                    // The end marker has moved forward to make the match which means we have exhausted all @item markers
                    // from the last @[table code] and @[end table] we were using prior to this iteration of the loop.
                    // If we change the table markers now, we avoid the prior table from
                    // matching any @item marker that sits between tables.
                    Matcher mm2 = TableCodeMatchPattern.matcher(tt);
                    tt = mm2.replaceFirst("<DL>\n$2\n</DL>");
                    mm2 = TableCodeMatchPattern.matcher(tt);
                    if (mm2.find())
                        endtableIndex = mm2.start(3);
                }
                else {
                    tt = mm.replaceFirst("$1<DT><CODE>$2</CODE>\n<DD>$3$4");
                    endtableIndex = mm.end(1) + 10 + mm.group(2).length() + 12 + mm.group(3).length();
                }
            }
        }

        ss = "";
        endtableIndex = Integer.MAX_VALUE;
        while (ss != tt) {
            mm = TableVarItemPattern.matcher(tt);
            ss = tt;
            if (mm.find()) {
                if (mm.start(4) > endtableIndex) {
                    // We try and find the last @[table var] marker we can find with an @item before @[end table].
                    // The end marker has moved forward to make the match which means we have exhausted all @item markers
                    // from the last @[table var] and @[end table] we were using prior to this iteration of the loop.
                    // If we change the table markers now, we avoid the prior table from
                    // matching any @item marker that sits between tables.
                    Matcher mm2 = TableVarMatchPattern.matcher(tt);
                    tt = mm2.replaceFirst("<DL>\n$2\n</DL>");
                    mm2 = TableVarMatchPattern.matcher(tt);
                    if (mm2.find())
                        endtableIndex = mm2.start(3);
                }
                else {
                    tt = mm.replaceFirst("$1<DT><VAR>$2</VAR>\n<DD>$3$4");
                    endtableIndex = mm.end(1) + 10 + mm.group(2).length() + 12 + mm.group(3).length();
                }
            }
        }

        ss = "";
        while (ss != tt) {
            mm = VtableCodeItemPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("$1<DT><CODE>$2</CODE>\n<DD>$3");
        }

        ss = "";
        while (ss != tt) {
            mm = MultitableItemPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("$1<DT><pre>$2</pre>\n<DD><br>$3");
        }

        ss = "";
        while (ss != tt) {
            mm = TableSampPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<DL>\n");
        }

        ss = "";
        while (ss != tt) {
            mm = TableAsisPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<DL>\n");
        }

        ss = "";
        while (ss != tt) {
            mm = TableVarPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<DL>\n");
        }

        ss = "";
        while (ss != tt) {
            mm = TableCodePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<DL>\n");
        }

        ss = "";
        while (ss != tt) {
            mm = VtableCodePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<DL>\n");
        }

        ss = "";
        while (ss != tt) {
            mm = MultitablePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<DL>\n<DT>");
        }

        ss = "";
        while (ss != tt) {
            mm = EndTablePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("</DL>");
        }

        ss = "";
        while (ss != tt) {
            mm = EndVtablePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("</DL>");
        }

        ss = "";
        while (ss != tt) {
            mm = EndMultitablePattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("</DL>");
        }

        //FIXME: if there ever is a @itemize @bullet within a
        //       @itemize @minus or vice-versa, the following
        //       logic will get it wrong.
        ss = "";
        while (ss != tt) {
            mm = ItemizeMinusPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<UL>$1</UL>");
        }

        ss = "";
        while (ss != tt) {
            mm = ItemizeBulletPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<OL>$1</OL>");
        }

        ss = "";
        while (ss != tt) {
            mm = ItemPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<LI>");
        }

        ss = "";
        while (ss != tt) {
            mm = TagPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("$1");
        }

        ss = "";
        while (ss != tt) {
            mm = TagCharPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("$1");
        }

        ss = "";
        while (ss != tt) {
            mm = StrandedPeriod.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("$1. ");
        }

        ss = "";
        while (ss != tt) {
            mm = LeftOverPeriod.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        ss = "";
        while (ss != tt) {
            mm = TabPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("");
        }

        mm = AmpersandPattern.matcher(tt);
        tt = mm.replaceAll("&amp;");

        mm = LeftAnglePattern.matcher(tt);
        tt = mm.replaceAll("&lt;");

        mm = RightAnglePattern.matcher(tt);
        tt = mm.replaceAll("&gt;");

        // Put back all the eol markers
        mm = EOLPattern.matcher(tt);
        tt = mm.replaceAll("<eol>");

        return tt;
    }
}
