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
import java.io.*;
import java.util.regex.*;
import java.util.*;

//This file contains a texinfo parser that can be
//run to create the Autotools glibc.xml file.
//Usage is as follows:
//1. compile this file using javac
//2. run this file using java, passing the
//arguments: ${glibc_source_path}/manual glibc.xml

public class ParseGlibcTexinfo {
    static final boolean DEBUG = false;

    //                           1
    static final String ATcmd = "(@\\w*)";


    //                              3   4
    static final String RtnTypeM = "(\\{([^\\}]*)\\})";
    static final int    RtnTypeMIndex = 4;

    //                              5
    static final String RtnTypeS = "(\\w*)";
    static final int    RtnTypeSIndex = 5;

    //                             2
    static final String RtnType = "(" + RtnTypeM + "|" + RtnTypeS + ")";
    //                                  6
    static final String FunctionName = "(\\w*)";
    static final int    FunctionNameIndex = 6;

    //                          7    8
    static final String Parms = "(\\((.*)\\))";
    static final int    ParmsIndex = 8;

    static final String rest = ".*";

    static final String WhiteSpace = "\\s*";

    static final Pattern DeftypefunPattern
    = Pattern.compile("^" + ATcmd + WhiteSpace +
            RtnType + WhiteSpace +
            FunctionName + WhiteSpace +
            Parms +
            rest, Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

    static final String TPDataType = "\\{[^\\}]*\\}";


    //                             3   4
    static final String TPTypeM = "(\\{([^\\}]*)\\})";
    static final int    TPTypeMIndex = 4;

    //                             5
    static final String TPTypeS = "(\\w*)";
    static final int    TPTypeSIndex = 5;

    //                            2
    static final String TPType = "(" + TPTypeM + "|" + TPTypeS + ")";

    static final Pattern DeftpPattern
    = Pattern.compile("^" + ATcmd + WhiteSpace + TPDataType + WhiteSpace + TPType + rest,
            Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

    static final Pattern IncludePattern        = Pattern.compile("^@comment ((\\w*/)*\\w*\\.h\\s*)*\\s*$");
    static final Pattern PindexPattern        = Pattern.compile("^@pindex\\s*\\w*\\s*$");
    static final Pattern ParmBracketPattern = Pattern.compile("\\((.*)\\)");
    static final Pattern IndexPattern        = Pattern.compile("@\\w*index\\s+[a-zA-Z0-9_@\\{\\}]*");
    static final Pattern IndexPattern2        = Pattern.compile("@\\w*index\\{[a-zA-Z0-9_@\\{\\}]*\\}");
    static final Pattern ExamplePattern        = Pattern.compile("@example");
    static final Pattern EndExamplePattern  = Pattern.compile("@end\\s+example");
    static final Pattern EnumeratePattern    = Pattern.compile("@enumerate");
    static final Pattern EndEnumeratePattern  = Pattern.compile("@end\\s+enumerate");
    static final Pattern VerbatimPattern    = Pattern.compile("@verbatim");
    static final Pattern ItemPattern        = Pattern.compile("@item");
    static final Pattern NoIndentPattern    = Pattern.compile("@noindent");
    static final Pattern BRPattern            = Pattern.compile("&lt;br&gt;");
    static final Pattern EOLPattern            = Pattern.compile("&lt;eol&gt;");
    static final Pattern EndVerbatimPattern = Pattern.compile("@end\\s+verbatim");
    static final Pattern TableSampItemPattern = Pattern.compile("(@table\\s*@samp.*)@item\\s*([a-zA-Z_0-9+\\-<>/ ]*)<eol>(.*@end\\s*table)", Pattern.DOTALL);
    static final Pattern TableAsisItemPattern = Pattern.compile("(@table\\s*@asis.*)@item\\s*([a-zA-Z_0-9+\\-,<>/ ]*)<eol>(.*@end\\s*table)", Pattern.DOTALL);
    static final Pattern TableSampPattern     = Pattern.compile("@table\\s*@samp", Pattern.MULTILINE);
    static final Pattern TableAsisPattern    = Pattern.compile("@table\\s*@asis", Pattern.MULTILINE);
    static final Pattern EndTablePattern     = Pattern.compile("@end\\s+table");
    static final Pattern DotsPattern        = Pattern.compile("@dots\\{\\}");
    static final Pattern ItemizeMinusPattern= Pattern.compile("@itemize\\s+@minus" + "(.*)" + "@end\\s+itemize", Pattern.MULTILINE);
    static final Pattern ItemizeBulletPattern= Pattern.compile("@itemize\\s+@bullet" + "(.*)" + "@end\\s+itemize", Pattern.MULTILINE);
    static final Pattern XrefPattern        = Pattern.compile("@xref\\{[^\\}]*\\}", Pattern.MULTILINE);
    static final Pattern CommandPattern        = Pattern.compile("@command\\{([^\\}]*)\\}");
    static final Pattern KbdPattern            = Pattern.compile("@kbd\\{([^\\}]*)\\}");
    static final Pattern RPattern            = Pattern.compile("@r\\{([^\\}]*)\\}");
    static final Pattern FilePattern        = Pattern.compile("@file\\{([^\\}]*)\\}");
    static final Pattern VarPattern            = Pattern.compile("@var\\{([^\\}]*)\\}");
    static final Pattern OVarPattern        = Pattern.compile("@ovar\\{([^\\}]*)\\}");
    static final Pattern DVarPattern        = Pattern.compile("@dvar\\{([^\\},\\,]*),([^\\}]*)\\}");
    static final Pattern CodePattern        = Pattern.compile("@code\\{([^\\}]*)\\}");
    static final Pattern EmphPattern        = Pattern.compile("@emph\\{([^\\}]*)\\}");
    static final Pattern SampPattern        = Pattern.compile("@samp\\{([^\\}]*)\\}");
    static final Pattern OptionPattern        = Pattern.compile("@option\\{([^\\}]*)\\}");
    static final Pattern TagPattern        = Pattern.compile("@\\w*\\{([^\\}]*)\\}");
    static final Pattern AmpersandPattern    = Pattern.compile("&");
    static final Pattern LeftAnglePattern    = Pattern.compile("<");
    static final Pattern RightAnglePattern    = Pattern.compile(">");

    static List IncludeList = new ArrayList();


    static class FunctionDef {
        String ReturnType;
        String FunctionName;
        String[] Parameters;
        Object[] IncludeList;
    }

    static class TPElement {
        String Content;
        String Synopsis;
    }

    static class TPDef {
        String TPType;
        String TPName;
        String TPSynopsis;
        TPElement[] TPElements;
        Object[] IncludeList;
    }

    private static FunctionDef BuildFunctionDef(Matcher m) {
        FunctionDef fd = new FunctionDef();

        fd.ReturnType = ((null != m.group(RtnTypeSIndex))
                ? m.group(RtnTypeSIndex)
                        : m.group(RtnTypeMIndex));
        fd.FunctionName = m.group(FunctionNameIndex);

        if (null != m.group(ParmsIndex)) {
            String tt = TexinfoUtils.stripProtoTags(m.group(ParmsIndex));
            String[] parms = tt.split(",\\s");
            fd.Parameters = parms;
        }

        if (IncludeList.size() > 0) {
            fd.IncludeList = IncludeList.toArray();
            IncludeList.clear();
        }
        return fd;
    }

    private static void HandleDeftp(BufferedWriter os, BufferedReader is, String s) throws IOException {
        TPDef td = new TPDef();
        String il;
        String Synopsis = null;
        boolean ItemsAccumulating = false;
        TPElement tpe = new TPElement();
        List ElementList = new ArrayList();

        Matcher m = DeftpPattern.matcher(s);
        if (m.matches()) {
            if (null != m.group(TPTypeMIndex)) {
                String[] ss = m.group(TPTypeMIndex).split("\\s");
                switch(ss.length) {
                case 0:
                    td.TPType = "";
                    td.TPName = "type";
                    break;
                case 1:
                    td.TPType = "type";
                    td.TPName = ss[0];
                    break;
                case 2:
                    td.TPType = ss[0];
                    td.TPName = ss[1];
                    break;
                default:
                    td.TPType = "type";
                td.TPName = ss[ss.length - 1];
                break;
                }
            }
            else {
                td.TPType = "dtype";
                td.TPName = m.group(TPTypeSIndex);
            }

            while (null != (il = is.readLine())) {
                if (il.startsWith("@end deftp")) {
                    WriteString(os, "  <construct id=\"" + td.TPType + "-" + td.TPName
                            + "\" type=\"" + td.TPType + "\">");
                    WriteString(os, "    <structure>");
                    if (null != td.TPSynopsis) WriteSynopsis(os, td.TPSynopsis, false);
                    if (ElementList.size() > 0) {
                        WriteString(os, "      <elements>");
                        for (int ee = 0; ee < ElementList.size(); ee++) {
                            TPElement ttt = (TPElement)ElementList.get(ee);
                            WriteString(os, "        <element content=\"" + ttt.Content + "\">");
                            if (null != ttt.Synopsis) WriteSynopsis(os, ttt.Synopsis, true);
                            WriteString(os, "        </element>");
                        }
                        WriteString(os, "      </elements>");
                    }
                    WriteString(os, "    </structure>");
                    WriteString(os, "  </construct>");
                    return;
                }
                else if (il.startsWith("@item")) {
                    if (ItemsAccumulating) {
                        tpe.Synopsis = Synopsis;
                        ElementList.add(tpe);
                    }
                    else {
                        td.TPSynopsis  = Synopsis;
                        ItemsAccumulating = true;
                    }
                    Synopsis = null;
                    tpe = new TPElement();
                    tpe.Content = TexinfoUtils.transformTags(il.replaceFirst("@item ", ""));
                }
                else {
                    if (!il.startsWith("@table"))
                        Synopsis = ((Synopsis == null) ? "" : Synopsis + " " ) + ((il.length() == 0) ? "<br><br>" :
                            il + "<eol>");
                }
            }
        }
    }

    private static FunctionDef HandleFunctionDef(BufferedReader is, String s) throws IOException {
        FunctionDef fd;

        Matcher m = DeftypefunPattern.matcher(s);

        if (m.matches()) fd = BuildFunctionDef(m);
        else {                        // assume the line got split and retry
            String il = is.readLine();
            m = DeftypefunPattern.matcher(s + il);
            if (m.matches()) fd = BuildFunctionDef(m);
            else fd = null;
        }

        return fd;
    }

    private static void WriteString(BufferedWriter os, String s) throws IOException {
        //    System.out.println(s);
        os.write(s+"\n", 0, 1+s.length());
    }

    private static void CreateHeader(BufferedWriter os) throws IOException {
        WriteString(os, "<!-- This file automatically generated by an Eclipse utility -->");
        WriteString(os, "<!DOCTYPE descriptions [");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT descriptions (construct)*>");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT construct (structure|function)*>");
        WriteString(os, "  <!ATTLIST construct");
        WriteString(os, "    id ID #REQUIRED");
        WriteString(os, "    type CDATA #REQUIRED");
        WriteString(os, "  >");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT structure       (synopsis?, elements?)?>");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT elements     (element*)>");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT element (synopsis*)>");
        WriteString(os, "  <!ATTLIST element");
        WriteString(os, "    content CDATA #REQUIRED");
        WriteString(os, "  >");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT synopsis     (#PCDATA)*>");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT function     (prototype,headers?,synopsis)>");
        WriteString(os, "  <!ATTLIST function");
        WriteString(os, "    returntype CDATA #REQUIRED");
        WriteString(os, "  >");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT prototype    (parameter+)?>");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT parameter (#PCDATA)*>");
        WriteString(os, "  <!ATTLIST parameter");
        WriteString(os, "    content CDATA #REQUIRED");
        WriteString(os, "  >");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT headers      (header+)?>");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT header (#PCDATA)*>");
        WriteString(os, "  <!ATTLIST header");
        WriteString(os, "    filename CDATA #REQUIRED");
        WriteString(os, "  >");
        WriteString(os, "");
        WriteString(os, "]>");
        WriteString(os, "");
    }

//  // For now we will use an aggregate document license instead of one per
//  // document.
//    private static void CreateLicense(BufferedWriter os) throws IOException {
//        WriteString(os, "<!--");
//        WriteString(os, "A.1 GNU Free Documentation License");
//        WriteString(os,"Version 1.2, November 2002");
//        WriteString(os, "");
//        WriteString(os, "Copyright &copy; 2000,2001,2002 Free Software Foundation, Inc.");
//        WriteString(os, "59 Temple Place, Suite 330, Boston, MA  02111-1307, USA");
//        WriteString(os, "");
//        WriteString(os, "Everyone is permitted to copy and distribute verbatim copies");
//        WriteString(os, "of this license document, but changing it is not allowed.");
//        WriteString(os, "");
//        WriteString(os, "1. PREAMBLE");
//        WriteString(os, "");
//        WriteString(os, "The purpose of this License is to make a manual, textbook, or other");
//        WriteString(os, "functional and useful document free in the sense of freedom: to");
//        WriteString(os, "assure everyone the effective freedom to copy and redistribute it,");
//        WriteString(os, "with or without modifying it, either commercially or noncommercially.");
//        WriteString(os, "Secondarily, this License preserves for the author and publisher a way");
//        WriteString(os, "to get credit for their work, while not being considered responsible");
//        WriteString(os, "for modifications made by others.");
//        WriteString(os, "");
//        WriteString(os, "This License is a kind of \"copyleft\", which means that derivative");
//        WriteString(os, "works of the document must themselves be free in the same sense.  It");
//        WriteString(os, "complements the GNU General Public License, which is a copyleft");
//        WriteString(os, "license designed for free software.");
//        WriteString(os, "");
//        WriteString(os, "We have designed this License in order to use it for manuals for free");
//        WriteString(os, "software, because free software needs free documentation: a free");
//        WriteString(os, "program should come with manuals providing the same freedoms that the");
//        WriteString(os, "software does.  But this License is not limited to software manuals;");
//        WriteString(os, "it can be used for any textual work, regardless of subject matter or");
//        WriteString(os, "whether it is published as a printed book.  We recommend this License");
//        WriteString(os, "principally for works whose purpose is instruction or reference.");
//        WriteString(os, "");
//        WriteString(os, "2. APPLICABILITY AND DEFINITIONS");
//        WriteString(os, "");
//        WriteString(os, "This License applies to any manual or other work, in any medium, that");
//        WriteString(os, "contains a notice placed by the copyright holder saying it can be");
//        WriteString(os, "distributed under the terms of this License.  Such a notice grants a");
//        WriteString(os, "world-wide, royalty-free license, unlimited in duration, to use that");
//        WriteString(os, "work under the conditions stated herein.  The \"Document\", below,");
//        WriteString(os, "refers to any such manual or work.  Any member of the public is a");
//        WriteString(os, "licensee, and is addressed as \"you\".  You accept the license if you");
//        WriteString(os, "copy, modify or distribute the work in a way requiring permission");
//        WriteString(os, "under copyright law.");
//        WriteString(os, "");
//        WriteString(os, "A \"Modified Version\" of the Document means any work containing the");
//        WriteString(os, "Document or a portion of it, either copied verbatim, or with");
//        WriteString(os, "modifications and/or translated into another language.");
//        WriteString(os, "");
//        WriteString(os, "A \"Secondary Section\" is a named appendix or a front-matter section");
//        WriteString(os, "of the Document that deals exclusively with the relationship of the");
//        WriteString(os, "publishers or authors of the Document to the Document's overall");
//        WriteString(os, "subject (or to related matters) and contains nothing that could fall");
//        WriteString(os, "directly within that overall subject.  (Thus, if the Document is in");
//        WriteString(os, "part a textbook of mathematics, a Secondary Section may not explain");
//        WriteString(os, "any mathematics.)  The relationship could be a matter of historical");
//        WriteString(os, "connection with the subject or with related matters, or of legal,");
//        WriteString(os, "commercial, philosophical, ethical or political position regarding");
//        WriteString(os, "them.");
//        WriteString(os, "");
//        WriteString(os, "The \"Invariant Sections\" are certain Secondary Sections whose titles");
//        WriteString(os, "are designated, as being those of Invariant Sections, in the notice");
//        WriteString(os, "that says that the Document is released under this License.  If a");
//        WriteString(os, "section does not fit the above definition of Secondary then it is not");
//        WriteString(os, "allowed to be designated as Invariant.  The Document may contain zero");
//        WriteString(os, "Invariant Sections.  If the Document does not identify any Invariant");
//        WriteString(os, "Sections then there are none.");
//        WriteString(os, "");
//        WriteString(os, "The \"Cover Texts\" are certain short passages of text that are listed,");
//        WriteString(os, "as Front-Cover Texts or Back-Cover Texts, in the notice that says that");
//        WriteString(os, "the Document is released under this License.  A Front-Cover Text may");
//        WriteString(os, "be at most 5 words, and a Back-Cover Text may be at most 25 words.");
//        WriteString(os, "");
//        WriteString(os, "A \"Transparent\" copy of the Document means a machine-readable copy,");
//        WriteString(os, "represented in a format whose specification is available to the");
//        WriteString(os, "general public, that is suitable for revising the document");
//        WriteString(os, "straightforwardly with generic text editors or (for images composed of");
//        WriteString(os, "pixels) generic paint programs or (for drawings) some widely available");
//        WriteString(os, "drawing editor, and that is suitable for input to text formatters or");
//        WriteString(os, "for automatic translation to a variety of formats suitable for input");
//        WriteString(os, "to text formatters.  A copy made in an otherwise Transparent file");
//        WriteString(os, "format whose markup, or absence of markup, has been arranged to thwart");
//        WriteString(os, "or discourage subsequent modification by readers is not Transparent.");
//        WriteString(os, "An image format is not Transparent if used for any substantial amount");
//        WriteString(os, "of text.  A copy that is not \"Transparent\" is called \"Opaque\".");
//        WriteString(os, "");
//        WriteString(os, "Examples of suitable formats for Transparent copies include plain");
//        WriteString(os, "ASCII without markup, Texinfo input format, LaTeX input");
//        WriteString(os, "format, SGML or XML using a publicly available");
//        WriteString(os, "DTD, and standard-conforming simple HTML,");
//        WriteString(os, "PostScript or PDF designed for human modification.  Examples");
//        WriteString(os, "of transparent image formats include PNG, XCF and");
//        WriteString(os, "JPG.  Opaque formats include proprietary formats that can be");
//        WriteString(os, "read and edited only by proprietary word processors, SGML or");
//        WriteString(os, "XML for which the DTD and/or processing tools are");
//        WriteString(os, "not generally available, and the machine-generated HTML,");
//        WriteString(os, "PostScript or PDF produced by some word processors for");
//        WriteString(os, "output purposes only.");
//        WriteString(os, "");
//        WriteString(os, "The \"Title Page\" means, for a printed book, the title page itself,");
//        WriteString(os, "plus such following pages as are needed to hold, legibly, the material");
//        WriteString(os, "this License requires to appear in the title page.  For works in");
//        WriteString(os, "formats which do not have any title page as such, \"Title Page\" means");
//        WriteString(os, "the text near the most prominent appearance of the work's title,");
//        WriteString(os, "preceding the beginning of the body of the text.");
//        WriteString(os, "");
//        WriteString(os, "A section \"Entitled XYZ\" means a named subunit of the Document whose");
//        WriteString(os, "title either is precisely XYZ or contains XYZ in parentheses following");
//        WriteString(os, "text that translates XYZ in another language.  (Here XYZ stands for a");
//        WriteString(os, "specific section name mentioned below, such as \"Acknowledgements\",");
//        WriteString(os, "\"Dedications\", \"Endorsements\", or \"History\".)  To \"Preserve the Title\"");
//        WriteString(os, "of such a section when you modify the Document means that it remains a");
//        WriteString(os, "section \"Entitled XYZ\" according to this definition.");
//        WriteString(os, "");
//        WriteString(os, "The Document may include Warranty Disclaimers next to the notice which");
//        WriteString(os, "states that this License applies to the Document.  These Warranty");
//        WriteString(os, "Disclaimers are considered to be included by reference in this");
//        WriteString(os, "License, but only as regards disclaiming warranties: any other");
//        WriteString(os, "implication that these Warranty Disclaimers may have is void and has");
//        WriteString(os, "no effect on the meaning of this License.");
//        WriteString(os, "");
//        WriteString(os, "3. VERBATIM COPYING");
//        WriteString(os, "");
//        WriteString(os, "You may copy and distribute the Document in any medium, either");
//        WriteString(os, "commercially or noncommercially, provided that this License, the");
//        WriteString(os, "copyright notices, and the license notice saying this License applies");
//        WriteString(os, "to the Document are reproduced in all copies, and that you add no other");
//        WriteString(os, "conditions whatsoever to those of this License.  You may not use");
//        WriteString(os, "technical measures to obstruct or control the reading or further");
//        WriteString(os, "copying of the copies you make or distribute.  However, you may accept");
//        WriteString(os, "compensation in exchange for copies.  If you distribute a large enough");
//        WriteString(os, "number of copies you must also follow the conditions in section 3.");
//        WriteString(os, "");
//        WriteString(os, "You may also lend copies, under the same conditions stated above, and");
//        WriteString(os, "you may publicly display copies.");
//        WriteString(os, "");
//        WriteString(os, "4. COPYING IN QUANTITY");
//        WriteString(os, "");
//        WriteString(os, "If you publish printed copies (or copies in media that commonly have");
//        WriteString(os, "printed covers) of the Document, numbering more than 100, and the");
//        WriteString(os, "Document's license notice requires Cover Texts, you must enclose the");
//        WriteString(os, "copies in covers that carry, clearly and legibly, all these Cover");
//        WriteString(os, "Texts: Front-Cover Texts on the front cover, and Back-Cover Texts on");
//        WriteString(os, "the back cover.  Both covers must also clearly and legibly identify");
//        WriteString(os, "you as the publisher of these copies.  The front cover must present");
//        WriteString(os, "the full title with all words of the title equally prominent and");
//        WriteString(os, "visible.  You may add other material on the covers in addition.");
//        WriteString(os, "Copying with changes limited to the covers, as long as they preserve");
//        WriteString(os, "the title of the Document and satisfy these conditions, can be treated");
//        WriteString(os, "as verbatim copying in other respects.");
//        WriteString(os, "");
//        WriteString(os, "If the required texts for either cover are too voluminous to fit");
//        WriteString(os, "legibly, you should put the first ones listed (as many as fit");
//        WriteString(os, "reasonably) on the actual cover, and continue the rest onto adjacent");
//        WriteString(os, "pages.");
//        WriteString(os, "");
//        WriteString(os, "If you publish or distribute Opaque copies of the Document numbering");
//        WriteString(os, "more than 100, you must either include a machine-readable Transparent");
//        WriteString(os, "copy along with each Opaque copy, or state in or with each Opaque copy");
//        WriteString(os, "a computer-network location from which the general network-using");
//        WriteString(os, "public has access to download using public-standard network protocols");
//        WriteString(os, "a complete Transparent copy of the Document, free of added material.");
//        WriteString(os, "If you use the latter option, you must take reasonably prudent steps,");
//        WriteString(os, "when you begin distribution of Opaque copies in quantity, to ensure");
//        WriteString(os, "that this Transparent copy will remain thus accessible at the stated");
//        WriteString(os, "location until at least one year after the last time you distribute an");
//        WriteString(os, "Opaque copy (directly or through your agents or retailers) of that");
//        WriteString(os, "edition to the public.");
//        WriteString(os, "");
//        WriteString(os, "It is requested, but not required, that you contact the authors of the");
//        WriteString(os, "Document well before redistributing any large number of copies, to give");
//        WriteString(os, "them a chance to provide you with an updated version of the Document.");
//        WriteString(os, "");
//        WriteString(os, "5. MODIFICATIONS");
//        WriteString(os, "");
//        WriteString(os, "You may copy and distribute a Modified Version of the Document under");
//        WriteString(os, "the conditions of sections 2 and 3 above, provided that you release");
//        WriteString(os, "the Modified Version under precisely this License, with the Modified");
//        WriteString(os, "Version filling the role of the Document, thus licensing distribution");
//        WriteString(os, "and modification of the Modified Version to whoever possesses a copy");
//        WriteString(os, "of it.  In addition, you must do these things in the Modified Version:");
//        WriteString(os, "");
//        WriteString(os, "   1. Use in the Title Page (and on the covers, if any) a title distinct");
//        WriteString(os, "      from that of the Document, and from those of previous versions");
//        WriteString(os, "      (which should, if there were any, be listed in the History section");
//        WriteString(os, "      of the Document).  You may use the same title as a previous version");
//        WriteString(os, "      if the original publisher of that version gives permission.");
//        WriteString(os, "");
//        WriteString(os, "   2. List on the Title Page, as authors, one or more persons or entities");
//        WriteString(os, "      responsible for authorship of the modifications in the Modified");
//        WriteString(os, "      Version, together with at least five of the principal authors of the");
//        WriteString(os, "      Document (all of its principal authors, if it has fewer than five),");
//        WriteString(os, "      unless they release you from this requirement.");
//        WriteString(os, "");
//        WriteString(os, "   3. State on the Title page the name of the publisher of the");
//        WriteString(os, "      Modified Version, as the publisher.");
//        WriteString(os, "");
//        WriteString(os, "   4. Preserve all the copyright notices of the Document.");
//        WriteString(os, "");
//        WriteString(os, "   5. Add an appropriate copyright notice for your modifications");
//        WriteString(os, "      adjacent to the other copyright notices.");
//        WriteString(os, "");
//        WriteString(os, "   6. Include, immediately after the copyright notices, a license notice");
//        WriteString(os, "      giving the public permission to use the Modified Version under the");
//        WriteString(os, "      terms of this License, in the form shown in the Addendum below.");
//        WriteString(os, "");
//        WriteString(os, "   7. Preserve in that license notice the full lists of Invariant Sections");
//        WriteString(os, "      and required Cover Texts given in the Document's license notice.");
//        WriteString(os, "");
//        WriteString(os, "   8. Include an unaltered copy of this License.");
//        WriteString(os, "");
//        WriteString(os, "   9. Preserve the section Entitled \"History\", Preserve its Title, and add");
//        WriteString(os, "      to it an item stating at least the title, year, new authors, and");
//        WriteString(os, "      publisher of the Modified Version as given on the Title Page.  If");
//        WriteString(os, "      there is no section Entitled \"History\" in the Document, create one");
//        WriteString(os, "      stating the title, year, authors, and publisher of the Document as");
//        WriteString(os, "      given on its Title Page, then add an item describing the Modified");
//        WriteString(os, "      Version as stated in the previous sentence.");
//        WriteString(os, "");
//        WriteString(os, "  10. Preserve the network location, if any, given in the Document for");
//        WriteString(os, "      public access to a Transparent copy of the Document, and likewise");
//        WriteString(os, "      the network locations given in the Document for previous versions");
//        WriteString(os, "      it was based on.  These may be placed in the \"History\" section.");
//        WriteString(os, "      You may omit a network location for a work that was published at");
//        WriteString(os, "      least four years before the Document itself, or if the original");
//        WriteString(os, "      publisher of the version it refers to gives permission.");
//        WriteString(os, "");
//        WriteString(os, "  11. For any section Entitled \"Acknowledgements\" or \"Dedications\", Preserve");
//        WriteString(os, "      the Title of the section, and preserve in the section all the");
//        WriteString(os, "      substance and tone of each of the contributor acknowledgements and/or");
//        WriteString(os, "      dedications given therein.");
//        WriteString(os, "");
//        WriteString(os, "  12. Preserve all the Invariant Sections of the Document,");
//        WriteString(os, "      unaltered in their text and in their titles.  Section numbers");
//        WriteString(os, "      or the equivalent are not considered part of the section titles.");
//        WriteString(os, "");
//        WriteString(os, "  13. Delete any section Entitled \"Endorsements\".  Such a section");
//        WriteString(os, "      may not be included in the Modified Version.");
//        WriteString(os, "");
//        WriteString(os, "  14. Do not retitle any existing section to be Entitled \"Endorsements\" or");
//        WriteString(os, "      to conflict in title with any Invariant Section.");
//        WriteString(os, "");
//        WriteString(os, "  15. Preserve any Warranty Disclaimers.");
//        WriteString(os, "");
//        WriteString(os, "");
//        WriteString(os, "If the Modified Version includes new front-matter sections or");
//        WriteString(os, "appendices that qualify as Secondary Sections and contain no material");
//        WriteString(os, "copied from the Document, you may at your option designate some or all");
//        WriteString(os, "of these sections as invariant.  To do this, add their titles to the");
//        WriteString(os, "list of Invariant Sections in the Modified Version's license notice.");
//        WriteString(os, "These titles must be distinct from any other section titles.");
//        WriteString(os, "");
//        WriteString(os, "You may add a section Entitled \"Endorsements\", provided it contains");
//        WriteString(os, "nothing but endorsements of your Modified Version by various");
//        WriteString(os, "parties - for example, statements of peer review or that the text has");
//        WriteString(os, "been approved by an organization as the authoritative definition of a");
//        WriteString(os, "standard.");
//        WriteString(os, "");
//        WriteString(os, "You may add a passage of up to five words as a Front-Cover Text, and a");
//        WriteString(os, "passage of up to 25 words as a Back-Cover Text, to the end of the list");
//        WriteString(os, "of Cover Texts in the Modified Version.  Only one passage of");
//        WriteString(os, "Front-Cover Text and one of Back-Cover Text may be added by (or");
//        WriteString(os, "through arrangements made by) any one entity.  If the Document already");
//        WriteString(os, "includes a cover text for the same cover, previously added by you or");
//        WriteString(os, "by arrangement made by the same entity you are acting on behalf of,");
//        WriteString(os, "you may not add another; but you may replace the old one, on explicit");
//        WriteString(os, "permission from the previous publisher that added the old one.");
//        WriteString(os, "");
//        WriteString(os, "The author(s) and publisher(s) of the Document do not by this License");
//        WriteString(os, "give permission to use their names for publicity for or to assert or");
//        WriteString(os, "imply endorsement of any Modified Version.");
//        WriteString(os, "");
//        WriteString(os, "6. COMBINING DOCUMENTS");
//        WriteString(os, "");
//        WriteString(os, "You may combine the Document with other documents released under this");
//        WriteString(os, "License, under the terms defined in section 4 above for modified");
//        WriteString(os, "versions, provided that you include in the combination all of the");
//        WriteString(os, "Invariant Sections of all of the original documents, unmodified, and");
//        WriteString(os, "list them all as Invariant Sections of your combined work in its");
//        WriteString(os, "license notice, and that you preserve all their Warranty Disclaimers.");
//        WriteString(os, "");
//        WriteString(os, "The combined work need only contain one copy of this License, and");
//        WriteString(os, "multiple identical Invariant Sections may be replaced with a single");
//        WriteString(os, "copy.  If there are multiple Invariant Sections with the same name but");
//        WriteString(os, "different contents, make the title of each such section unique by");
//        WriteString(os, "adding at the end of it, in parentheses, the name of the original");
//        WriteString(os, "author or publisher of that section if known, or else a unique number.");
//        WriteString(os, "Make the same adjustment to the section titles in the list of");
//        WriteString(os, "Invariant Sections in the license notice of the combined work.");
//        WriteString(os, "");
//        WriteString(os, "In the combination, you must combine any sections Entitled \"History\"");
//        WriteString(os, "in the various original documents, forming one section Entitled");
//        WriteString(os, "\"History\"; likewise combine any sections Entitled \"Acknowledgements\",");
//        WriteString(os, "and any sections Entitled \"Dedications\".  You must delete all");
//        WriteString(os, "sections Entitled \"Endorsements.\"");
//        WriteString(os, "");
//        WriteString(os, "7. COLLECTIONS OF DOCUMENTS");
//        WriteString(os, "");
//        WriteString(os, "You may make a collection consisting of the Document and other documents");
//        WriteString(os, "released under this License, and replace the individual copies of this");
//        WriteString(os, "License in the various documents with a single copy that is included in");
//        WriteString(os, "the collection, provided that you follow the rules of this License for");
//        WriteString(os, "verbatim copying of each of the documents in all other respects.");
//        WriteString(os, "");
//        WriteString(os, "You may extract a single document from such a collection, and distribute");
//        WriteString(os, "it individually under this License, provided you insert a copy of this");
//        WriteString(os, "License into the extracted document, and follow this License in all");
//        WriteString(os, "other respects regarding verbatim copying of that document.");
//        WriteString(os, "");
//        WriteString(os, "8. AGGREGATION WITH INDEPENDENT WORKS");
//        WriteString(os, "");
//        WriteString(os, "A compilation of the Document or its derivatives with other separate");
//        WriteString(os, "and independent documents or works, in or on a volume of a storage or");
//        WriteString(os, "distribution medium, is called an \"aggregate\" if the copyright");
//        WriteString(os, "resulting from the compilation is not used to limit the legal rights");
//        WriteString(os, "of the compilation's users beyond what the individual works permit.");
//        WriteString(os, "When the Document is included an aggregate, this License does not");
//        WriteString(os, "apply to the other works in the aggregate which are not themselves");
//        WriteString(os, "derivative works of the Document.");
//        WriteString(os, "");
//        WriteString(os, "If the Cover Text requirement of section 3 is applicable to these");
//        WriteString(os, "copies of the Document, then if the Document is less than one half of");
//        WriteString(os, "the entire aggregate, the Document's Cover Texts may be placed on");
//        WriteString(os, "covers that bracket the Document within the aggregate, or the");
//        WriteString(os, "electronic equivalent of covers if the Document is in electronic form.");
//        WriteString(os, "Otherwise they must appear on printed covers that bracket the whole");
//        WriteString(os, "aggregate.");
//        WriteString(os, "");
//        WriteString(os, "9. TRANSLATION");
//        WriteString(os, "");
//        WriteString(os, "Translation is considered a kind of modification, so you may");
//        WriteString(os, "distribute translations of the Document under the terms of section 4.");
//        WriteString(os, "Replacing Invariant Sections with translations requires special");
//        WriteString(os, "permission from their copyright holders, but you may include");
//        WriteString(os, "translations of some or all Invariant Sections in addition to the");
//        WriteString(os, "original versions of these Invariant Sections.  You may include a");
//        WriteString(os, "translation of this License, and all the license notices in the");
//        WriteString(os, "Document, and any Warrany Disclaimers, provided that you also include");
//        WriteString(os, "the original English version of this License and the original versions");
//        WriteString(os, "of those notices and disclaimers.  In case of a disagreement between");
//        WriteString(os, "the translation and the original version of this License or a notice");
//        WriteString(os, "or disclaimer, the original version will prevail.");
//        WriteString(os, "");
//        WriteString(os, "If a section in the Document is Entitled \"Acknowledgements\",");
//        WriteString(os, "\"Dedications\", or \"History\", the requirement (section 4) to Preserve");
//        WriteString(os, "its Title (section 1) will typically require changing the actual");
//        WriteString(os, "title.");
//        WriteString(os, "");
//        WriteString(os, "10. TERMINATION");
//        WriteString(os, "");
//        WriteString(os, "You may not copy, modify, sublicense, or distribute the Document except");
//        WriteString(os, "as expressly provided for under this License.  Any other attempt to");
//        WriteString(os, "copy, modify, sublicense or distribute the Document is void, and will");
//        WriteString(os, "automatically terminate your rights under this License.  However,");
//        WriteString(os, "parties who have received copies, or rights, from you under this");
//        WriteString(os, "License will not have their licenses terminated so long as such");
//        WriteString(os, "parties remain in full compliance.");
//        WriteString(os, "");
//        WriteString(os, "11. FUTURE REVISIONS OF THIS LICENSE");
//        WriteString(os, "");
//        WriteString(os, "The Free Software Foundation may publish new, revised versions");
//        WriteString(os, "of the GNU Free Documentation License from time to time.  Such new");
//        WriteString(os, "versions will be similar in spirit to the present version, but may");
//        WriteString(os, "differ in detail to address new problems or concerns.  See");
//        WriteString(os, "http://www.gnu.org/copyleft/.");
//        WriteString(os, "");
//        WriteString(os, "Each version of the License is given a distinguishing version number.");
//        WriteString(os, "If the Document specifies that a particular numbered version of this");
//        WriteString(os, "License \"or any later version\" applies to it, you have the option of");
//        WriteString(os, "following the terms and conditions either of that specified version or");
//        WriteString(os, "of any later version that has been published (not as a draft) by the");
//        WriteString(os, "Free Software Foundation.  If the Document does not specify a version");
//        WriteString(os, "number of this License, you may choose any version ever published (not");
//        WriteString(os, "as a draft) by the Free Software Foundation.");
//        WriteString(os, "-->");
//    }

    private static void CreateTrailer(BufferedWriter os) throws IOException {
        WriteString(os, "</descriptions>");
    }

    private static void WriteSynopsis(BufferedWriter os, String Synopsis, boolean indent)  throws IOException {
        String ss = TexinfoUtils.transformTags(Synopsis);
        String[] tt = ss.split("<eol>");
        String aa = "";
        String spaces = indent ? "            " : "        ";
        WriteString(os, spaces + "<synopsis>");
        if (null != Synopsis) {
            for (int pp = 0; pp < tt.length; pp++) {
                WriteString(os, spaces + tt[pp]);
//                if (tt[pp].equals("<br>")) {
//                    WriteString(os, spaces + aa + "\n");
//                    aa = "";
//                }
//                else {
//                    if ((aa.length() + tt[pp].length()) > 64) {
//                        WriteString(os, spaces + aa);
//                        aa = "";
//                    }
//                    aa = aa + " " + tt[pp];
//                }
            }
        }
        if (aa.length() > 0) WriteString(os, "        " + aa);
        WriteString(os, spaces + "</synopsis>");
    }

    private static String HandleInclude (String srcdir, String line, String Synopsis) {
        Pattern p = Pattern.compile("@include\\s+(.*?)\\.texi");
        Matcher mm = p.matcher(line);
        if (mm.find()) {
            String il;
            BufferedReader is = null;
            try {
                String fileName = (srcdir.endsWith("/") ? srcdir : srcdir + "/") + mm.replaceAll("examples/$1");
                is = new BufferedReader(new FileReader(fileName));
                while (null != (il = is.readLine())) {
                    // C Help does not ignore "<" or ">" inside a <pre> or <samp> tag
                    // so we have to prepare for two levels of indirection.  The
                    // first is for xml to interpret and the second is for the
                    // C Help processor to interpret.  So, we put &lt; and &gt; which
                    // will be transformed into &amp;lt; by the tag transformer.
                    Pattern p1 = Pattern.compile("<");
                    Pattern p2 = Pattern.compile(">");
                    Matcher mm1 = p1.matcher(il);
                    il = mm1.replaceAll("&lt;");
                    Matcher mm2 = p2.matcher(il);
                    il = mm2.replaceAll("&gt;");
                    Synopsis = ((Synopsis == null) ? "" : Synopsis + " " ) + ((il.length() == 0) ? "<br><br>" :
                        il + "<eol>");
                }
            } catch (IOException e) {
                System.out.println("IOException reading example file");
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return Synopsis;
    }
    private static void HandleDeftypefun(BufferedWriter os, BufferedReader is, String s, String srcdir) throws IOException {
        String il;
        FunctionDef fd;
        List FDefs = new ArrayList();
        String Synopsis = null;

        if (null != (fd = HandleFunctionDef(is, s))) FDefs.add(fd);

        while (null != (il = is.readLine())) {
            Matcher mm = IncludePattern.matcher(il);
            if (il.startsWith("@deftypefunx")) {
                if (null != (fd = HandleFunctionDef(is, il))) FDefs.add(fd);
            }
            else if (mm.matches()) {
                if (!IncludeList.contains(mm.group(1)))
                    IncludeList.add(mm.group(1));
            }
            else if (il.startsWith("@comment") ||
                    il.startsWith("@c ") ||
                    il.startsWith("@pindex")) {    // ignore
            }
            else if (il.startsWith("@include") && fd != null) {
                Synopsis = HandleInclude(srcdir, il, Synopsis);
            }
            else if (il.startsWith("@end deftypefun")) {
                for (int kk = 0; kk < FDefs.size(); kk++) {
                    fd = (FunctionDef)FDefs.get(kk);

                    WriteString(os, "  <construct id=\"function-" + fd.FunctionName + "\" type=\"function\">");

                    WriteString(os, "    <function returntype=\"" + fd.ReturnType + "\">");

                    WriteString(os, "      <prototype>");
                    String[] parms = fd.Parameters;
                    for (int i = 0; i < parms.length; i++) {
                        String parm = TexinfoUtils.stripProtoTags(parms[i]);
                        WriteString(os, "        <parameter content=\"" + parm + "\"/>");
                    }
                    WriteString(os, "      </prototype>");

                    if (fd.IncludeList != null) {
                        WriteString(os, "      <headers>");
                        Object[] incs = fd.IncludeList;
                        for (int j = 0; j < incs.length; j++) {
                            String inc = (String)incs[j];
                            WriteString(os, "        <header filename = \"" + inc + "\"/>");
                        }
                        WriteString(os, "      </headers>");
                    }

                    if (null != Synopsis) WriteSynopsis(os, Synopsis, false);

                    WriteString(os, "    </function>");
                    WriteString(os, "  </construct>");
                }
                return;
            }
            else {
                Synopsis = ((Synopsis == null) ? "" : Synopsis + " " ) + ((il.length() == 0) ? "<br><br>" :
                    il + "<eol>");
            }
        }
        FDefs.clear();

    }

    private static class OnlyTexi implements FilenameFilter {
        public boolean accept(File dir, String s) {
            return (s.endsWith(".texi")) ? true : false;
        }
    }

    public static void BuildXMLFromTexinfo(String srcdir, String dstdir) {
        try {
            BufferedWriter os = new BufferedWriter(new FileWriter(dstdir));

            CreateHeader(os);
//            CreateLicense(os);

            WriteString(os, "<descriptions>");

            try {
                String[] dir = new java.io.File(srcdir).list(new OnlyTexi());
                for (int i = 0; i < dir.length; i++) {
                    String qFile = srcdir.endsWith("/")
                    ? srcdir + dir[i]
                                   : srcdir + "/" + dir[i];

                    try {
                        BufferedReader is = new BufferedReader(new FileReader(qFile));
                        String il;

                        while (null != (il = is.readLine())) {
                            Matcher mm = IncludePattern.matcher(il);
                            if (il.startsWith("@deftypefun")) {    // handle @deftypefun[x]
                                HandleDeftypefun(os, is, il, srcdir);
                            }
                            else if (il.startsWith("@deftp")) {    // handle @deftp
                                HandleDeftp(os, is, il);
                            }
                            else if (mm.matches()) {        // handle @comment <include_file>
                                if (!IncludeList.contains(mm.group(1)))
                                    IncludeList.add(mm.group(1));
                            }
                            else if (il.startsWith("@end deftypefn"))
                                // Handle accumulated header file comments that are in
                                // constructs we aren't parsing.
                                IncludeList.clear();
                        }
                        is.close();
                    }
                    catch (IOException e) {
                        System.out.println("Input File IOException: " + e);
                        return;
                    }
                }
            }
            catch (NullPointerException e) {
                System.out.println("NullPointerException: " + e);
                return;
            }

            CreateTrailer(os);

            os.close();
        }
        catch (IOException e) {
            System.out.println("Output File IOException: " + e);
            return;
        }
    }

    public static void main(String[] args) {
        BuildXMLFromTexinfo(args[0], args[1]);
    }

}
