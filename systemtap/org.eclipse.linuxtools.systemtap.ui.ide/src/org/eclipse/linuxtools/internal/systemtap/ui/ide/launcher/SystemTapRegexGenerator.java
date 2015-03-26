/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.CommentRemover;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A class that is used for generating regular expressions that capture the output of .stp scripts.
 */
public class SystemTapRegexGenerator {
    public static enum ErrResult { IO_EXCEPTION }

    /**
     * Generate a list of regular expressions that will capture the output of a given .stp script.
     * Only output coming from <code>printf</code> statements will be captured.
     * @param scriptPath The absolute path of the script to capture the output of.
     * @param maxToFind The maximum number of regexs to create and return.
     * A negative value indicates no limit.
     * @return A list of generated regexs, each paired with the number of capturing groups it has.
     */
    public static List<Entry<String, Integer>> generateFromPrintf(IPath scriptPath, int maxToFind) {
        List<Entry<String, Integer>> regexs = new ArrayList<>();
        if (maxToFind == 0) {
            return regexs;
        }

        String contents = null;
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        IEditorPart editor = ResourceUtil.findEditor(
                workbench.getActiveWorkbenchWindow().getActivePage(),
                root.getFile(scriptPath.makeRelativeTo(root.getLocation())));
        if (editor != null) {
            // If editor of this file is open, take current file contents.
            ITextEditor tEditor = editor.getAdapter(ITextEditor.class);
            IDocument document = tEditor.getDocumentProvider().
                    getDocument(tEditor.getEditorInput());
            contents = CommentRemover.exec(document.get());
        } else {
            // If chosen file is not being edited or is outside of the workspace, use the saved contents of the file itself.
            contents = CommentRemover.execWithFile(scriptPath.toString());
        }

        // Now actually search the contents for "printf(...)" statements. (^|[\s({;])printf\("(.+?)",.+\)
        Pattern pattern = Pattern.compile("(?<=[^\\w])printf\\(\"(.+?)\",.+?\\)"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(contents);

        while (matcher.find() && (maxToFind < 0 || regexs.size() < maxToFind)) {
            String regex = null;

            // Note: allow optional "long" modifier 'l'. Not captured because it doesn't impact output format.
            // Also, don't support variable width/precision modifiers (*).
            // TODO: Consider %m & %M support.
            Pattern format = Pattern.compile("%([-\\+ \\#0])?(\\d+)?(\\.\\d*)?l?([bcdiopsuxX%])"); //$NON-NLS-1$

            // Only capture until newlines to preserve the "column" format.
            // Don't try gluing together output from multiple printfs
            // since asynchronous prints would make things messy.
            String[] printls = matcher.group(1).split("\\\\n"); //$NON-NLS-1$
            for (int i = 0; i < printls.length; i++) {
                String printl = printls[i];
                // Ignore newlines if they are escaped ("\\n").
                if (printl.endsWith("\\")) { //$NON-NLS-1$
                    printls[i+1] = printl.concat("\\n" + printls[i+1]); //$NON-NLS-1$
                    continue;
                }

                Matcher fmatch = format.matcher(printl);
                int lastend = 0;
                int numColumns = 0;
                while (fmatch.find()) {
                    numColumns++;
                    char chr = fmatch.group(4) == null ? '\0' : fmatch.group(4).charAt(0);
                    if (chr == '\0') {
                        // Skip this statement if an invalid regex is found.
                        regex = null;
                        break;
                    }
                    char flag = fmatch.group(1) == null ? '\0' : fmatch.group(1).charAt(0);
                    int width = fmatch.group(2) == null ? 0 : Integer.parseInt(fmatch.group(2));
                    String precision = fmatch.group(3) == null ? null : fmatch.group(3).substring(1);

                    // First, add any non-capturing characters.
                    String pre = addRegexEscapes(printl.substring(lastend, fmatch.start()));
                    regex = lastend > 0 ? regex.concat(pre) : pre;
                    lastend = fmatch.end();

                    // Now add what will be captured.
                    String target = "("; //$NON-NLS-1$
                    if (chr == 'u' || (flag != '#' && chr == 'o')) {
                        target = target.concat("\\d+"); //$NON-NLS-1$
                    }
                    else if (chr == 'd' || chr == 'i') {
                        if (flag == '+') {
                            target = target.concat("\\+|"); //$NON-NLS-1$
                        } else if (flag == ' ') {
                            target = target.concat(" |"); //$NON-NLS-1$
                        }
                        target = target.concat("-?\\d+"); //$NON-NLS-1$
                    }
                    else if (flag == '#' && chr == 'o') {
                        target = target.concat("0\\d+"); //$NON-NLS-1$
                    }
                    else if (chr == 'p') {
                        target = target.concat("0x[a-f0-9]+"); //$NON-NLS-1$
                    }
                    else if (chr == 'x') {
                        if (flag == '#') {
                            target = target.concat("0x"); //$NON-NLS-1$
                        }
                        target = target.concat("[a-f0-9]+"); //$NON-NLS-1$
                    }
                    else if (chr == 'X') {
                        if (flag == '#') {
                            target = target.concat("0X"); //$NON-NLS-1$
                        }
                        target = target.concat("[A-F0-9]+"); //$NON-NLS-1$
                    }
                    else if (chr == 'b') {
                        target = target.concat("."); //$NON-NLS-1$
                    }
                    else if (chr == 'c') {
                        if (flag != '#') {
                            target = target.concat("."); //$NON-NLS-1$
                        } else {
                            target = target.concat("\\([a-z]|[0-9]{3})|.|\\\\"); //$NON-NLS-1$
                        }
                    }
                    else if (chr == 's') {
                        if (precision != null) {
                            target = target.concat(".{" + precision + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            target = target.concat(".+"); //$NON-NLS-1$
                        }
                    }
                    else {
                        // Invalid or unhandled format specifier. Skip this regex.
                        regex = null;
                        break;
                    }

                    target = target.concat(")"); //$NON-NLS-1$

                    // Handle the optional width specifier.
                    // Ignore it for %b, which uses the width value in a different way.
                    if (chr != 'b' && --width > 0) {
                        if (flag == '-') {
                            target = target.concat(" {0," + width + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                        } else if (flag != '0' || chr == 's' || chr == 'c') {
                            target = " {0," + width + "}".concat(target); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }

                    regex = regex.concat(target);
                }
                if (regex != null) {
                    // Finally, add the uncaptured remainder of the print statement to the regex.
                    regexs.add(new SimpleEntry<>(regex.concat(addRegexEscapes(printl.substring(lastend))), numColumns));
                }
            }
        }
        return regexs;
    }

    /**
     * This escapes all special regex characters in a string. Escapes must be added
     * to the generated regexs to capture printf output that doesn't
     * come from format specifiers (aka literal strings).
     * @param s The string to add escapes to.
     * @return The same string, after it has been modified with escapes.
     */
    private static String addRegexEscapes(String s) {
        String schars = "[^$.|?*+(){}"; //$NON-NLS-1$
        for (int i = 0; i < schars.length(); i++) {
            s = s.replaceAll("(\\" + schars.substring(i,i+1) + ")", "\\\\$1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        return s;
    }
}
