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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//This file contains a texinfo parser that can be
//run to create the Autotools glibc.xml file.
//Usage is as follows:
//1. compile this file using javac
//2. run this file using java, passing the
//arguments: dir_to_automake_texi_files output_xml_file_name

public class ParseAutomakeTexinfo {

    static final boolean DEBUG = false;

    static final String ATcmd = "(@\\w*)";

    // Currently in automake docs, the macro section starts with
    // a subsection as below and a table which contains macros which
    // are item and itemx entries.
    static final String MacrosStart = "@subsection\\sPublic\\smacros";
    static final String OldMacrosStart = "@section\\sAutoconf\\smacros.*";
    static final Pattern MacroSection1 = Pattern.compile(MacrosStart);
    static final Pattern MacroSection2 = Pattern.compile(OldMacrosStart);
    //                           0
    static final String Defmac = "@item";
    static final String Defmacx = "@itemx";

    //                           1
    static final String MacroName = "(\\w*)";
    static final int    MacroNameIndex = 1;

    //                          2    3
    static final String Parms = "(\\((.*)\\))";
    static final int    ParmsIndex = 2;

    static final String rest = ".*";

    static final String WhiteSpace = "\\s*";

    static final Pattern MacroPattern
    = Pattern.compile("^" + Defmac + WhiteSpace +
            MacroName + WhiteSpace +
            Parms +
            rest, Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

    static final Pattern MacroPattern2
    = Pattern.compile("^" + Defmac + WhiteSpace + MacroName + rest,
            Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

    static final Pattern MacroPatternx
    = Pattern.compile("^" + Defmacx + WhiteSpace +
            MacroName + WhiteSpace +
            Parms +
            rest, Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

    static final Pattern MacroPatternx2
    = Pattern.compile("^" + Defmacx + WhiteSpace + MacroName + rest,
            Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

    static final Pattern ParmBracketPattern = Pattern.compile("\\((.*)\\)");
    static final Pattern IndexPattern        = Pattern.compile("@\\w*index.*");
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
    static final Pattern UrefPattern        = Pattern.compile("@uref\\{([^,]*),\\s+([^\\}]*)\\}");
    static final Pattern TagPattern        = Pattern.compile("@\\w*\\{([^\\}]*)\\}");
    static final Pattern AmpersandPattern    = Pattern.compile("&");
    static final Pattern LeftAnglePattern    = Pattern.compile("<");
    static final Pattern RightAnglePattern    = Pattern.compile(">");


    private static Map<String, MacroDef> macroMap;

    static class MacroParms {
        String[] parms;
        MacroParms nextParms = null;

        public MacroParms(String[] parms) {
            this.parms = parms;
        }
    }

    static class MacroDef {
        String MacroName;
        MacroParms Parameters;
        String Synopsis;
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

    private static String killTagsParms(String tt) {
        Matcher mm;

        mm = ParmBracketPattern.matcher(tt);
        tt= mm.replaceAll("$1");

        mm = OVarPattern.matcher(tt);
        tt = mm.replaceAll("[$1]");

        mm = DVarPattern.matcher(tt);
        tt = mm.replaceAll("[$1=$2]");

        mm = VarPattern.matcher(tt);
        tt = mm.replaceAll("$1");

        mm = RPattern.matcher(tt);
        tt = mm.replaceAll("$1");

        mm = DotsPattern.matcher(tt);
        tt = mm.replaceAll("...");

        return tt;
    }


    private static String killTags(String tt) {
        Matcher mm;
        String ss = "";

        while (ss != tt) {
            mm = XrefPattern.matcher(tt);
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
            mm = UrefPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("<A HREF=\"$1>$2</A>");
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

        ss = "";
        while (ss != tt) {
            mm = TableSampItemPattern.matcher(tt);
            ss = tt;
            if (mm.matches()) {
                System.out.println("group 1 is " + mm.group(1));
                System.out.println("group 2 is " + mm.group(2));
                System.out.println("group 3 is " + mm.group(3));
            }
            tt = mm.replaceAll("$1<DT>'<SAMP>$2</SAMP>'\n<DD>$3");
        }

        ss = "";
        while (ss != tt) {
            mm = TableAsisItemPattern.matcher(tt);
            ss = tt;
            tt = mm.replaceAll("$1<DT>$2\n<DD>$3");
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
            mm = EndTablePattern.matcher(tt);
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

        mm = AmpersandPattern.matcher(tt);
        tt = mm.replaceAll("&amp;");

        mm = LeftAnglePattern.matcher(tt);
        tt = mm.replaceAll("&lt;");

        mm = RightAnglePattern.matcher(tt);
        tt = mm.replaceAll("&gt;");

        // Clean up the eol markers we used to mark end of line for items
        mm = EOLPattern.matcher(tt);
        tt = mm.replaceAll("");

        return tt;
    }

    private static MacroDef BuildMacroDef(Matcher m) {
        MacroDef md = new MacroDef();

        md.MacroName = m.group(MacroNameIndex);

        if (null != m.group(ParmsIndex)) {
            String tt = killTagsParms(m.group(ParmsIndex));
            String[] parms = tt.split(",\\s");
            md.Parameters = new MacroParms(parms);
        }
        return md;
    }

    private static MacroParms AddMacroDefxParms(MacroParms mp, Matcher mx) {
        if (null != mx.group(ParmsIndex)) {
            String tt = killTagsParms(mx.group(ParmsIndex));
            String[] parms = tt.split(",\\s");
            MacroParms mpnew = new MacroParms(parms);
            mp.nextParms = mpnew;
            return mpnew;
        }
        return null;
    }

    private static MacroDef HandleMacroDef(BufferedReader is, String s) throws IOException {
        MacroDef fd = null;

        Matcher m = MacroPattern.matcher(s);

        if (m.matches()) {
            fd = BuildMacroDef(m);
        }
        else {                        // assume the line got split and retry
            is.mark(100);
            String il = is.readLine();
            m = MacroPattern.matcher(s + il);
            if (m.matches()) {
                fd = BuildMacroDef(m);
            } else {
                is.reset();
                m = MacroPattern2.matcher(s);
                if (m.matches()) {
                    fd = new MacroDef();
                    fd.MacroName = m.group(MacroNameIndex);
                    fd.Parameters = new MacroParms(new String[0]);
                }
            }
        }

        if (fd != null) {
            // Look for @defmacx which are alternate prototypes for the macro
            is.mark(100);
            String il = is.readLine();
            if (il != null) {
                Matcher mx = MacroPatternx.matcher(il);
                Matcher mx2 = MacroPatternx2.matcher(il);
                MacroParms mp = fd.Parameters;
                while (mx.matches() || mx2.matches()) {
                    if (mx.matches()) {
                        mp = AddMacroDefxParms(mp, mx);
                    } else {
                        MacroParms mpnew = new MacroParms(new String[0]);
                        mp.nextParms = mpnew;
                        mp = mpnew;
                    }
                    is.mark(100);
                    il = is.readLine();
                    if (il != null) {
                        mx = MacroPatternx.matcher(il);
                        mx2 = MacroPatternx2.matcher(il);
                    }
                }
                is.reset();
            }

            if (macroMap.get(fd.MacroName) != null) {
                return null;
            }
            macroMap.put(fd.MacroName, fd);
        }

        return fd;
    }

    private static void WriteString(BufferedWriter os, String s) throws IOException {
        //    System.out.println(s);
        os.write(s+"\n", 0, 1+s.length());
    }

    private static void CreateHeader(BufferedWriter os) throws IOException {
        WriteString(os, "<!-- This file automatically generated by ParseAutomakeTexinfo utility -->");
        WriteString(os, "<!-- cvs -d:pserver:anonymous@sources.redhat.com:/cvs/eclipse \\        -->");
        WriteString(os, "<!--   co autotools/ParseTexinfo                                       -->");
        WriteString(os, "<!DOCTYPE macros [");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT macros (macro)*>");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT macro (prototype*,synopsis)>");
        WriteString(os, "  <!ATTLIST macro");
        WriteString(os, "    id ID #REQUIRED");
        WriteString(os, "  >");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT synopsis     (#PCDATA)*>");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT prototype    (parameter+)?>");
        WriteString(os, "");
        WriteString(os, "  <!ELEMENT parameter (#PCDATA)*>");
        WriteString(os, "  <!ATTLIST parameter");
        WriteString(os, "    content CDATA #REQUIRED");
        WriteString(os, "  >");
        WriteString(os, "");
        WriteString(os, "]>");
        WriteString(os, "");
    }



    private static void CreateTrailer(BufferedWriter os) throws IOException {
        WriteString(os, "</macros>");
    }

    private static void WriteSynopsis(BufferedWriter os, String Synopsis, boolean indent)  throws IOException {
        String ss = Synopsis;
        String[] tt = ss.split("\\s");
        String aa = "";
        String spaces = indent ? "            " : "        ";
        WriteString(os, spaces + "<synopsis>");
        if (null != Synopsis) {
            for (int pp = 0; pp < tt.length; pp++) {
                if (tt[pp].equals("&lt;br&gt;")) {
                    WriteString(os, spaces + aa + "&lt;/P&gt;&lt;P&gt;\n");
                    aa = "";
                }
                else {
                    if ((aa.length() + tt[pp].length()) > 64) {
                        WriteString(os, spaces + aa);
                        aa = "";
                    }
                    aa = aa + " " + tt[pp];
                }
            }
        }
        if (aa.length() > 0) {
            WriteString(os, "        " + aa);
        }
        WriteString(os, spaces + "</synopsis>");
    }

    private static void HandleDefmacro(BufferedWriter os, BufferedReader is, String s) throws IOException {
        String il;
        MacroDef md = null;
        List<MacroDef> FDefs = new ArrayList<>();

        while (null != (il = is.readLine())) {
            if (il.startsWith(Defmac)) {
                if (null != (md = HandleMacroDef(is, il))) {
                    FDefs.add(md);
                }
            }
            else if (il.startsWith("@comment") ||
                    il.startsWith("@c ")) {    // comment -- ignore it
            }
            else if (il.startsWith("@subsection") ||
                    il.startsWith("@section")) {
                for (int kk = 0; kk < FDefs.size(); kk++) {
                    md = FDefs.get(kk);

                    WriteString(os, "  <macro id=\"" + md.MacroName + "\">");

                    MacroParms mp = md.Parameters;
                    do {
                        WriteString(os, "      <prototype>");
                        String[] parms = mp.parms;
                        for (int i = 0; i < parms.length; i++) {
                            WriteString(os, "        <parameter content=\"" + parms[i] + "\"/>");
                        }
                        WriteString(os, "      </prototype>");
                        mp = mp.nextParms;
                    } while (mp != null);

                    if (null != md.Synopsis) {
                        WriteSynopsis(os, md.Synopsis, false);
                    }

                    WriteString(os, "  </macro>");
                }
                return;
            }
            else {
                if (md != null) {
                    md.Synopsis = ((md.Synopsis == null) ? "" : md.Synopsis + " " ) + ((il.length() == 0) ? "&lt;br&gt;&lt;br&gt;" :
                        il.startsWith("@item") ? killTags(il) + "<eol>" : killTags(il));
                }
            }
        }
        FDefs.clear();

    }

    private static class OnlyTexi implements FilenameFilter {
        @Override
        public boolean accept(File dir, String s) {
            return (s.endsWith(".texi")) ? true : false;
        }
    }

    public static void BuildXMLFromTexinfo(String srcdir, String dstdir) {
        try {
            macroMap = new HashMap<>();
            BufferedWriter os = new BufferedWriter(new FileWriter(dstdir));

            CreateHeader(os);
            WriteString(os, "<macros>");

            try {
                String[] dir = new java.io.File(srcdir).list(new OnlyTexi());
                for (int i = 0; i < dir.length; i++) {
                    String qFile = srcdir.endsWith("/")
                            ? srcdir + dir[i]
                                    : srcdir + "/" + dir[i];

                            try (BufferedReader is = new BufferedReader(new FileReader(qFile))) {
                                String il;

                                while (null != (il = is.readLine())) {
                                    Matcher mm1 = MacroSection1.matcher(il);
                                    Matcher mm2 = MacroSection2.matcher(il);
                                    if (mm1.matches() || mm2.matches()) {
                                        HandleDefmacro(os, is, il);
                                    }
                                }
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
        // arg[0] is input directory containing .texi documents to read
        // arg[1] is output xml file to create
        BuildXMLFromTexinfo(args[0], args[1]);
    }

}
