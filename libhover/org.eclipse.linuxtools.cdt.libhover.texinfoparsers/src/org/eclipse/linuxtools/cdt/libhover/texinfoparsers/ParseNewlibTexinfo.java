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

public class ParseNewlibTexinfo {
    static final boolean DEBUG = false;

    //                              1, 2, 3
    static final String RtnType = "(\\w+\\s+(\\w+\\s+)?(\\w+\\s+)?\\**)";
    static final int    RtnTypeSIndex = 1;

    //                              4
    static final String FunctionName = "(\\w+)\\s*\\(";
    static final int    FunctionNameIndex = 4;

    //                              5 and 6
    static final String Parms = "((.*)\\))";
    static final int    ParmsIndex = 6;

    static final String rest = ".*";

    static final String WhiteSpace = "\\s*";

    static final Pattern DeftypefunPattern
    = Pattern.compile("^" + WhiteSpace +
            RtnType + WhiteSpace +
            FunctionName + WhiteSpace +
            Parms +
            rest, Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

    //                              1, 2, 3
    static final String RtnType2 = "(\\w*\\s*(\\w*\\*?\\s+)?(\\w*\\*?\\s+)?\\**\\s*)\\(\\*";
    static final int    RtnType2Index = 1;

    //                              4
    static final String FunctionName2 = "(\\w+)\\s*\\(";
    static final int    FunctionName2Index = 4;

    //                              5 and 6
    static final String Parms2 = "((.*)\\)\\)\\s*)";
    static final int    Parms2Index = 6;

    //                                7
    static final String RtnTypeParms = "(\\(.*\\))";
    static final int    RtnTypeParmsIndex = 7;

    static final Pattern DeftypefunPattern2
    = Pattern.compile("^" + WhiteSpace +
            RtnType2 + WhiteSpace +
            FunctionName2 + WhiteSpace +
            Parms2 + RtnTypeParms +
            rest, Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

    // For va_arg, the prototype is @var{type} so we create a third type of function definition
    // and we make it so the return type ends up taking as many groups as the normal RtnType so
    // the BuildFunctionDef routine can be used without modification.
    //                              1, 2, 3
    static final String RtnTypeVar = "@var\\{(\\w+)(\\})(\\s)";

    static final Pattern DeftypefunPattern3
    = Pattern.compile("^" + WhiteSpace +
            RtnTypeVar + WhiteSpace +
            FunctionName + WhiteSpace +
            Parms +
            rest, Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

    static final Pattern IncludePattern        = Pattern.compile("^#include\\s*<((\\w*/)*\\w*\\.h)>\\s*$");
    static final Pattern PindexPattern        = Pattern.compile("^@pindex\\s*\\w*\\s*$");
    static final Pattern FindexPattern        = Pattern.compile("^@findex\\s*(\\w*)\\s*$");
    static final Pattern SynopsisPattern    = Pattern.compile("^\\s*@strong\\{Synopsis\\}\\s*");
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
    static Stack readers = new Stack();

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

    private static FunctionDef FindFunctionDef(String name, List FDefs) {
        for (Iterator iterator = FDefs.iterator(); iterator.hasNext();) {
            FunctionDef k = (FunctionDef) iterator.next();
            if (k.FunctionName.equals(name))
                return k;
        }
        return null;
    }

    private static FunctionDef BuildFunctionDef(Matcher m, FunctionDef fd) {
        fd.ReturnType = m.group(RtnTypeSIndex);
        fd.FunctionName = m.group(FunctionNameIndex);

        if (null != m.group(ParmsIndex)) {
            String tt = TexinfoUtils.stripProtoTags(m.group(ParmsIndex));
            String[] parms = tt.split(",\\s");
            fd.Parameters = parms;
        }

        if (IncludeList.size() > 0) {
            fd.IncludeList = IncludeList.toArray();
        }
        return fd;
    }

    private static FunctionDef BuildFunctionDef2(Matcher m, FunctionDef fd) {
        fd.ReturnType = m.group(RtnType2Index) + "(*)" + m.group(RtnTypeParmsIndex);
        fd.FunctionName = m.group(FunctionName2Index);

        if (null != m.group(Parms2Index)) {
            String tt = TexinfoUtils.stripProtoTags(m.group(Parms2Index));
            String[] parms = tt.split(",\\s");
            fd.Parameters = parms;
        }

        if (IncludeList.size() > 0) {
            fd.IncludeList = IncludeList.toArray();
        }
        return fd;
    }

    private static void HandleFunctionDefs(BufferedReader is, List FDefs) throws IOException {
        FunctionDef fd;
        String il = null;
        boolean preRead = false;

        while (preRead || (il = is.readLine()) != null) {
            preRead = false;
            if (il.startsWith("@end example"))
                return;

            Matcher m = DeftypefunPattern.matcher(il);
            Matcher m2 = DeftypefunPattern2.matcher(il);
            Matcher m3 = DeftypefunPattern3.matcher(il);
            Matcher mm = IncludePattern.matcher(il);

            if (mm.matches()) {
                if (!IncludeList.contains(mm.group(1)))
                    IncludeList.add(mm.group(1));
            }
            else if (m.matches()) {
                fd = FindFunctionDef(m.group(FunctionNameIndex), FDefs);
                if (fd != null)
                    BuildFunctionDef(m, fd);
                else
                    System.out.println("Missing findex for " + m.group(FunctionNameIndex));
            }
            else if (m2.matches()) {
                fd = FindFunctionDef(m2.group(FunctionName2Index), FDefs);
                if (fd != null)
                    BuildFunctionDef2(m2, fd);
                else
                    System.out.println("Missing findex for " + m2.group(FunctionName2Index));
            }
            else if (m3.matches()) {
                fd = FindFunctionDef(m3.group(FunctionNameIndex), FDefs);
                if (fd != null)
                    BuildFunctionDef(m3, fd);
                else
                    System.out.println("Missing findex for " + m3.group(FunctionName2Index));
            }
            else if (il.trim().length() > 0) {
                il = il.trim();
                while (il.endsWith(",")) {  // assume prototype extends more than one line
                    preRead = true;
                    String il2 = is.readLine().trim();
                    if (il2 != null && il2.startsWith("@")) { // something wrong, just look at new line fetched
                        il = il2;
                        continue;
                    }
                    il = il + il2; // concatenate
                }
            }
        }
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
        WriteString(os, "  <!ELEMENT construct (structure|function|synopsis)*>");
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
        WriteString(os, "  <!ELEMENT function     (prototype,headers?,groupsynopsis?,synopsis)>");
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
        WriteString(os, "  <!ELEMENT groupsynopsis (#PCDATA)*>");
        WriteString(os, "  <!ATTLIST groupsynopsis");
        WriteString(os, "    id CDATA #REQUIRED");
        WriteString(os, "  >");
        WriteString(os, "");
        WriteString(os, "]>");
        WriteString(os, "");
    }

    private static void CreateTrailer(BufferedWriter os) throws IOException {
        WriteString(os, "</descriptions>");
    }

    private static void WriteDescription(BufferedWriter os, String Synopsis, boolean indent)  throws IOException {
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

    private static BufferedReader HandleInclude (BufferedReader is, String srcdir, String line) {
        Pattern p = Pattern.compile("@include\\s+(.*?)");
        Matcher mm = p.matcher(line.trim());
        BufferedReader is2 = null;
        if (mm.find()) {
            String fileName = (srcdir.endsWith("/") ? srcdir : srcdir + "/") + mm.replaceAll("$1");
            try {
                is2 = new BufferedReader(new FileReader(fileName));
                readers.push(is);
            } catch (FileNotFoundException e) {
                System.out.println("include " + fileName + " not found");
                // do nothing and return null
            }
        }
        return is2 == null ? is : is2;
    }

    private static String HandleDescription(BufferedReader is) throws IOException {
        String Description = null;
        String il;

        while (null != (il = is.readLine())) {
            if (il.startsWith("@page") ||
                    il.startsWith("@section") ||
                    il.startsWith("@node"))
                break;
            Description = ((Description == null) ? "" : Description + " " ) + ((il.length() == 0) ? "<br><br>" :
                il + "<eol>");
        }

        return Description;
    }

    private static void HandleFunction(BufferedWriter os, BufferedReader is, String s, String builddir) throws IOException {
        String il;
        FunctionDef fd;
        List FDefs = new ArrayList();
        String Description = null;
        boolean synopsisMarker = false;

        IncludeList.clear();
        Matcher mmf = FindexPattern.matcher(s);
        fd = new FunctionDef();
        if (mmf.matches())
            fd.FunctionName = mmf.group(1);
        else
            return;
        FDefs.add(fd);

        while (null != (il = is.readLine())) {
            Matcher syn = SynopsisPattern.matcher(il);
            if (il.startsWith("@findex")) {
                synopsisMarker = false;
                Matcher mm2 = FindexPattern.matcher(il);
                FunctionDef fd2 = new FunctionDef();
                if (mm2.matches()) {
                    fd2.FunctionName = mm2.group(1);
                    FDefs.add(fd2);
                }
            }
            else if (il.startsWith("@example") && synopsisMarker) {
                HandleFunctionDefs(is, FDefs);
                synopsisMarker = false;
            }
            else if (il.startsWith("@include") && fd != null) {
                is = HandleInclude(is, builddir, il);
            }
            else if (syn.matches()) {
                synopsisMarker = true;
            }
            else if (il.startsWith("@strong{Description}")) {
                synopsisMarker = false;
                Description = HandleDescription(is);
                break; // we are done after description has been fetched
            }
            // otherwise ignore line
        }

        String name = ((FunctionDef)FDefs.get(0)).FunctionName;

        if (FDefs.size() > 1) {
            for (int kk = 0; kk < FDefs.size(); kk++) {
                fd = (FunctionDef)FDefs.get(kk);

                WriteString(os, "  <construct id=\"function-" + fd.FunctionName + "\" type=\"function\">");

                WriteString(os, "    <function returntype=\"" + fd.ReturnType + "\">");

                WriteString(os, "      <prototype>");
                String[] parms = fd.Parameters;
                if (parms == null)
                    System.out.println("null parms for findex " + fd.FunctionName);
                else {
                    for (int i = 0; i < parms.length; i++) {
                        String parm = TexinfoUtils.stripProtoTags(parms[i]);
                        WriteString(os, "        <parameter content=\"" + parm + "\"/>");
                    }
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

                if (null != Description)
                    WriteString(os, "      <groupsynopsis id=\"group-" + name + "\"/>");


                WriteString(os, "    </function>");
                WriteString(os, "  </construct>");
            }

            if (null != Description) {
                WriteString(os, "  <construct id=\"group-" + name + "\" type=\"groupsynopsis\">");
                WriteDescription(os, Description, false);
                WriteString(os, "  </construct>");
            }
        }
        else {
            fd = (FunctionDef)FDefs.get(0);

            WriteString(os, "  <construct id=\"function-" + fd.FunctionName + "\" type=\"function\">");

            WriteString(os, "    <function returntype=\"" + fd.ReturnType + "\">");

            WriteString(os, "      <prototype>");
            String[] parms = fd.Parameters;
            if (parms == null)
                System.out.println("null parms for findex " + fd.FunctionName);
            else {
                for (int i = 0; i < parms.length; i++) {
                    String parm = TexinfoUtils.stripProtoTags(parms[i]);
                    WriteString(os, "        <parameter content=\"" + parm + "\"/>");
                }
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

            if (null != Description) WriteDescription(os, Description, false);

            WriteString(os, "    </function>");
            WriteString(os, "  </construct>");
        }
        FDefs.clear();
    }

    public static void BuildXMLFromTexinfo2(String srcdir, String builddir, BufferedWriter os, String lib) {
        try {
            srcdir = srcdir.endsWith("/") ? srcdir + lib : srcdir + "/" + lib;
            builddir = builddir.endsWith("/") ? builddir + lib : builddir + "/" + lib;
            String qFile = srcdir + "/" + lib + ".texinfo";

            try {
                BufferedReader is = new BufferedReader(new FileReader(qFile));
                String il;
                boolean ignore = false;

                while (is != null) {
                    while (null != (il = is.readLine())) {
                        if (!ignore && il.startsWith("@findex")) {
                            HandleFunction(os, is, il, builddir);
                        }
                        else if (!ignore && il.startsWith("@include")) {
                            is = HandleInclude(is, builddir, il);
                        }
                        else if (il.startsWith("@ignore")) {
                            ignore = true;
                        }
                        else if (il.startsWith("@end ignore"))
                            ignore = false;
                    }
                    is.close();
                    is = (BufferedReader)readers.pop();
                }
            }
            catch (IOException e) {
                System.out.println("Input File IOException: " + e);
                return;
            }
            catch (EmptyStackException f) {
                // ok, we expect to get here
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("NullPointerException: " + e);
            return;
        }

    }

    public static void BuildXMLFromTexinfo(String srcdir, String builddir, String dstdir) {
        try {
            BufferedWriter os = new BufferedWriter(new FileWriter(dstdir));

            CreateHeader(os);
//            CreateLicense(os);

            WriteString(os, "<descriptions>");
            BuildXMLFromTexinfo2(srcdir, builddir, os, "libc");
            BuildXMLFromTexinfo2(srcdir, builddir, os, "libm");


            CreateTrailer(os);

            os.close();
        }
        catch (IOException e) {
            System.out.println("Output File IOException: " + e);
            return;
        }
    }

    public static void main(String[] args) {
        BuildXMLFromTexinfo(args[0], args[1], args[2]);
    }

}
