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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    static List<String> IncludeList = new ArrayList<>();


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
        List<TPElement> ElementList = new ArrayList<>();

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
                    if (null != td.TPSynopsis) {
                        WriteSynopsis(os, td.TPSynopsis, false);
                    }
                    if (ElementList.size() > 0) {
                        WriteString(os, "      <elements>");
                        for (int ee = 0; ee < ElementList.size(); ee++) {
                            TPElement ttt = ElementList.get(ee);
                            WriteString(os, "        <element content=\"" + ttt.Content + "\">");
                            if (null != ttt.Synopsis) {
                                WriteSynopsis(os, ttt.Synopsis, true);
                            }
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
                    if (!il.startsWith("@table")) {
                        Synopsis = ((Synopsis == null) ? "" : Synopsis + " " ) + ((il.length() == 0) ? "<br><br>" :
                            il + "<eol>");
                    }
                }
            }
        }
    }

    private static FunctionDef HandleFunctionDef(BufferedReader is, String s) throws IOException {
        FunctionDef fd;

        Matcher m = DeftypefunPattern.matcher(s);

        if (m.matches()) {
            fd = BuildFunctionDef(m);
        } else {                        // assume the line got split and retry
            String il = is.readLine();
            m = DeftypefunPattern.matcher(s + il);
            if (m.matches()) {
                fd = BuildFunctionDef(m);
            } else {
                fd = null;
            }
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
            }
        }
        if (aa.length() > 0) {
            WriteString(os, "        " + aa);
        }
        WriteString(os, spaces + "</synopsis>");
    }

    private static String HandleInclude (String srcdir, String line, String Synopsis) {
        Pattern p = Pattern.compile("@include\\s+(.*?)\\.texi");
        Matcher mm = p.matcher(line);
        if (mm.find()) {
            String il;
            String fileName = (srcdir.endsWith("/") ? srcdir : srcdir + "/") + mm.replaceAll("examples/$1");
            try (BufferedReader is = new BufferedReader(new FileReader(fileName))) {
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
            }
        }
        return Synopsis;
    }
    private static void HandleDeftypefun(BufferedWriter os, BufferedReader is, String s, String srcdir) throws IOException {
        String il;
        FunctionDef fd;
        List<FunctionDef> FDefs = new ArrayList<>();
        String Synopsis = null;

        if (null != (fd = HandleFunctionDef(is, s))) {
            FDefs.add(fd);
        }

        while (null != (il = is.readLine())) {
            Matcher mm = IncludePattern.matcher(il);
            if (il.startsWith("@deftypefunx")) {
                if (null != (fd = HandleFunctionDef(is, il))) {
                    FDefs.add(fd);
                }
            }
            else if (mm.matches()) {
                if (!IncludeList.contains(mm.group(1))) {
                    IncludeList.add(mm.group(1));
                }
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
                    fd = FDefs.get(kk);

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

                    if (null != Synopsis) {
                        WriteSynopsis(os, Synopsis, false);
                    }

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
        @Override
        public boolean accept(File dir, String s) {
            return (s.endsWith(".texi")) ? true : false;
        }
    }

    public static void BuildXMLFromTexinfo(String srcdir, String dstdir) {
        try {
            BufferedWriter os = new BufferedWriter(new FileWriter(dstdir));

            CreateHeader(os);

            WriteString(os, "<descriptions>");

            try {
                String[] dir = new java.io.File(srcdir).list(new OnlyTexi());
                for (int i = 0; i < dir.length; i++) {
                    String qFile = srcdir.endsWith("/")
                            ? srcdir + dir[i] : srcdir + "/" + dir[i];

                    try (BufferedReader is = new BufferedReader(new FileReader(qFile))) {
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
                                if (!IncludeList.contains(mm.group(1))) {
                                    IncludeList.add(mm.group(1));
                                }
                            }
                            else if (il.startsWith("@end deftypefn")) {
                                // Handle accumulated header file comments that are in
                                // constructs we aren't parsing.
                                IncludeList.clear();
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
        BuildXMLFromTexinfo(args[0], args[1]);
    }

}
