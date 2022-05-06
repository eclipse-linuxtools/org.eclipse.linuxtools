/*******************************************************************************
 * Copyright (c) 2009, 2021 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *    Ingenico  - Vincent Guignot <vincent.guignot@ingenico.com> - Add binutils strings
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.binutils.utils.STStrings;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.internal.gcov.model.CovFileTreeElement;
import org.eclipse.linuxtools.internal.gcov.model.CovFolderTreeElement;
import org.eclipse.linuxtools.internal.gcov.model.CovFunctionTreeElement;
import org.eclipse.linuxtools.internal.gcov.model.CovRootTreeElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class CovManager implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5582066617970911413L;

    private static String winOSType = ""; //$NON-NLS-1$

    // input
    private final String binaryPath;
    // results
    private final ArrayList<Folder> allFolders = new ArrayList<>();
    private final ArrayList<SourceFile> allSrcs = new ArrayList<>();
    private final ArrayList<GcnoFunction> allFnctns = new ArrayList<>();
    private final HashMap<String, SourceFile> sourceMap = new HashMap<>();
    private long nbrPgmRuns = 0;
    // for view
    private CovRootTreeElement rootNode;
	//FIXME EK-LINUXTOOLS: private final IProject project;
	//FIXME EK-LINUXTOOLS: Need project for finding local STRINGS etc. Can't be final
    private IProject project;

    /**
     * Constructor
     * @param binaryPath
     * @param project
     *            the project that will be used to get the path to run commands
     */
    public CovManager(String binaryPath, IProject project) {
        this.binaryPath = binaryPath;
        this.project = project;
    }

    /**
     * Constructor
     * @param binaryPath
     */
    public CovManager(String binaryPath)
    {
        this(binaryPath, null);
    	//FIXME EK-LINUXTOOLS: Need project for finding local STRINGS etc.
		this.project = STSymbolManager.sharedInstance.getProjectFromFile(new Path(binaryPath));
    }

    /**
     * parse coverage files, execute resolve graph algorithm, process counts for functions, lines and folders.
     * @param List of coverage files paths
     * @throws CoreException, IOException, InterruptedException
     */

    public void processCovFiles(List<String> covFilesPaths, String initialGcda) throws CoreException, IOException {
        GcdaRecordsParser daRcrd = null;
        DataInput traceFile;

        Map<File, File> sourcePath = new HashMap<>();

        if (initialGcda != null) {
            File initialGcdaFile = new File(initialGcda).getAbsoluteFile();
            for (String s : covFilesPaths) {
                File gcda = new File(s).getAbsoluteFile();
                if (gcda.getName().equals(initialGcdaFile.getName()) && !gcda.equals(initialGcdaFile)) {
                    if (!sourcePath.isEmpty()) {
                        // hum... another file has the same name...
                        // sorry, we have to clean sourcePath
                        sourcePath.clear();
                        break;
                    } else {
                        addSourceLookup(sourcePath, initialGcdaFile, gcda);
                    }
                }
            }
        }

        for (String gcdaPath : covFilesPaths) {
            String gcnoPath = gcdaPath.replace(".gcda", ".gcno"); //$NON-NLS-1$ //$NON-NLS-2$
            // parse GCNO file
            traceFile = openTraceFileStream(gcnoPath, ".gcno", sourcePath); //$NON-NLS-1$
            if (traceFile == null) {
                return;
            }
            GcnoRecordsParser noRcrd = new GcnoRecordsParser(sourceMap, allSrcs);
            noRcrd.parseData(traceFile);

            // add new functions parsed to AllSrcs array
            for (GcnoFunction f : noRcrd.getFnctns()) {
                allFnctns.add(f);
            }

            // close the input stream
            if (traceFile.getClass() == DataInputStream.class) {
                ((DataInputStream) traceFile).close();
            }

            // parse GCDA file
            traceFile = openTraceFileStream(gcdaPath, ".gcda", sourcePath); //$NON-NLS-1$
            if (traceFile == null) {
                return;
            }
            if (noRcrd.getFnctns().isEmpty()) {
                String message = NLS.bind(Messages.CovManager_No_Funcs_Error, gcnoPath);
				IStatus status = Status.error(message);
                throw new CoreException(status);
            }
            daRcrd = new GcdaRecordsParser(noRcrd.getFnctns());
            daRcrd.parseGcdaRecord(traceFile);

            // close the input stream
            if (traceFile.getClass() == DataInputStream.class) {
                ((DataInputStream) traceFile).close();
            }
        }

        // to fill the view title
        if (daRcrd != null) {
            nbrPgmRuns = daRcrd.getPgmSmryNbrPgmRuns();
        }

        /* process counts from data parsed */

        // solve graph for each function
        for (GcnoFunction gf : allFnctns) {
            gf.solveGraphFnctn();
        }

        // allocate lines
        for (SourceFile sourceFile : allSrcs) {
			sourceFile.createLines(allSrcs);
        }

        // add line counts
        for (GcnoFunction gf : allFnctns) {
            gf.addLineCounts(allSrcs);
        }

        // accumulate lines
        for (SourceFile sf : allSrcs) {
            sf.accumulateLineCounts();
        }

        /* compute counts by folder */

        // make the folders list
        for (SourceFile sf : allSrcs) {
            File srcFile = new File(sf.getName());
            String folderName = srcFile.getParent();
            if (folderName == null) {
                folderName = "?"; //$NON-NLS-1$
            }
            Folder folder = null;
            for (Folder f : allFolders) {
                if (f.getPath().equals(folderName)) {
                    folder = f;
                }
            }
            if (folder == null) {
                folder = new Folder(folderName);
                allFolders.add(folder);
            }
            folder.addSrcFiles(sf);
        }

        // assign sourcesList for each folder
        for (Folder f : allFolders) {
            f.accumulateSourcesCounts();
        }
    }

    /**
     * fill the model by count results
     * @throws CoreException, IOException, InterruptedException
     */

    public void fillGcovView() {
        // process counts for summary level
        int summaryTotal = 0, summaryInstrumented = 0, summaryExecuted = 0;
        for (Folder f : allFolders) {
            summaryTotal += f.getNumLines();
            summaryInstrumented += f.getLinesInstrumented();
            summaryExecuted += f.getLinesExecuted();
        }

        // fill rootNode model: the entry of the contentProvider
        rootNode = new CovRootTreeElement(Messages.CovManager_Summary, summaryTotal, summaryExecuted,
                summaryInstrumented);
        IBinaryObject binaryObject = STSymbolManager.sharedInstance.getBinaryObject(new Path(binaryPath));

        for (Folder fldr : allFolders) {
            String folderLocation = fldr.getPath();
            CovFolderTreeElement fldrTreeElem = new CovFolderTreeElement(rootNode, folderLocation, fldr.getNumLines(),
                    fldr.getLinesExecuted(), fldr.getLinesInstrumented());
            rootNode.addChild(fldrTreeElem);

            for (SourceFile src : fldr.getSrcFiles()) {
                CovFileTreeElement srcTreeElem = new CovFileTreeElement(fldrTreeElem, src.getName(), src.getNumLines(),
                        src.getLinesExecuted(), src.getLinesInstrumented());
                fldrTreeElem.addChild(srcTreeElem);

                for (GcnoFunction fnctn : src.getFnctns()) {
                    String name = fnctn.getName();
                    name = STSymbolManager.sharedInstance.demangle(binaryObject, name, project);
                    srcTreeElem.addChild(new CovFunctionTreeElement(srcTreeElem, name, fnctn.getSrcFile(), fnctn
                            .getFirstLineNmbr(), fnctn.getCvrge().getLinesExecuted(), fnctn.getCvrge()
                            .getLinesInstrumented()));
                }
            }
        }
    }

    // Get the Windows OS Type.  We might have to change a path over to Windows format
    // and this is different on Cygwin vs MingW.
    private String getWinOSType() {
        if (winOSType.equals("")) { //$NON-NLS-1$
            try {
                Process process = Runtime.getRuntime().exec(new String[] {"sh", "-c", "echo $OSTYPE"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                String firstLine = null;
                try (BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    firstLine = stdout.readLine();
                }
                if (firstLine != null) {
                    winOSType = firstLine.trim();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return winOSType;
    }

    // Get the OS path string.  For Cygwin, translate We add a Win check to handle MingW.
    // For MingW, we would rather represent C:\a\b as /C/a/b which
    // doesn't cause Makefile to choke. For Cygwin we use /cygdrive/C/a/b
    private String getTransformedPathString(IPath path) {
        String s = path.toOSString();
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            if (getWinOSType().equals("cygwin")) { //$NON-NLS-1$
                s = s.replaceAll("^\\\\cygdrive\\\\([a-zA-Z])", "$1:"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                s = s.replaceAll("^\\\\([a-zA-Z])", "$1:"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return s;
    }

	private class StringHolder {
		private String s;

		public String getString() {
			return s;
		}

		public void setString(String s) {
			this.s = s;
		}
	}

    private DataInput openTraceFileStream(String filePath, String extension, Map<File, File> sourcePath)
            throws FileNotFoundException {
        Path p = new Path(filePath);
        // get the file path transformed to work on local OS (e.g. Windows)
        filePath = getTransformedPathString(p);
        File f = new File(filePath).getAbsoluteFile();
        String filename = f.getName();
        if (f.isFile() && f.canRead()) {
            FileInputStream fis = new FileInputStream(f);
            InputStream inputStream = new BufferedInputStream(fis);
            return new DataInputStream(inputStream);
        } else {
            String postfix = ""; //$NON-NLS-1$
            File dir = null;
            do {
                if (postfix.isEmpty()) {
                    postfix = f.getName();
                } else {
                    postfix = f.getName() + File.separator + postfix;
                }
                f = f.getParentFile();
                if (f != null) {
                    dir = sourcePath.get(f);
                } else {
                    break;
                }
            } while (dir == null);

            if (dir != null) {
                f = new File(dir, postfix);
                if (f.isFile() && f.canRead()) {
                    return openTraceFileStream(f.getAbsolutePath(), extension, sourcePath);
                }
            }

			// We need to ask user for location. Open up a dialog.
			final StringHolder holder = new StringHolder();
			final String filePathToUse = filePath;
			Display.getDefault().syncExec(() -> {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FileDialog fg = new FileDialog(shell, SWT.OPEN);
				fg.setFilterExtensions(new String[] { "*" + extension, "*.*", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				fg.setFileName(filename);
				fg.setText(NLS.bind(Messages.CovManager_No_FilePath_Error, new Object[] { filePathToUse, filename }));
				String s = fg.open();
				holder.setString(s);
			});
			// transform String path to stream
			String s = holder.getString();
            if (s == null) {
				return null;
            } else {
				f = new File(s).getAbsoluteFile();
				addSourceLookup(sourcePath, f, new File(filePath).getAbsoluteFile());
				if (f.isFile() && f.canRead()) {
					FileInputStream fis = new FileInputStream(f);
					InputStream inputStream = new BufferedInputStream(fis);
					return new DataInputStream(inputStream);
				}
            }
        }
        return null;
    }

    public ArrayList<SourceFile> getAllSrcs() {
        return allSrcs;
    }

    public ArrayList<GcnoFunction> getAllFnctns() {
        return allFnctns;
    }

    public CovRootTreeElement getRootNode() {
        return rootNode;
    }

    public String getBinaryPath() {
        return binaryPath;
    }

    public SourceFile getSourceFile(String sourcePath) {
        return sourceMap.get(sourcePath);
    }

    public long getNbrPgmRuns() {
        return nbrPgmRuns;
    }

    /**
     * Retrieve a list containing gcda paths from a binary file
     * @return
     * @throws InterruptedException
     */
    public List<String> getGCDALocations() throws InterruptedException {
        IBinaryObject binaryObject = STSymbolManager.sharedInstance.getBinaryObject(new Path(binaryPath));
        String binaryPath = binaryObject.getPath().toOSString();
        STStrings strings = STSymbolManager.sharedInstance.getStrings(binaryObject, project);
        List<String> l = new LinkedList<>();
        Process p = getStringsProcess(strings.getName(), strings.getArgs(), binaryPath);
        if (p == null) {
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				Display.getDefault().syncExec(() -> {
					MessageDialog.openError(new Shell(), Messages.CovManager_Retrieval_Error_title,
							Messages.CovManager_No_Strings_Windows_Error);
				});
			} else {
				IStatus status = Status.error(Messages.CovManager_Retrieval_Error, new IOException());
				Platform.getLog(FrameworkUtil.getBundle(CovManager.class)).log(status);
			}
            return l;
        }
        ThreadConsumer t = new ThreadConsumer(p, l);
        t.start();
        p.waitFor();
        t.join();
        return l;
    }

    private Process getStringsProcess(String stringsTool, String[] stringsArgs, String binaryPath) {
        String[] runtimeStr = new String[stringsArgs.length + 2];
        System.arraycopy(stringsArgs, 0, runtimeStr, 1, stringsArgs.length);
        runtimeStr[0] = stringsTool;
        runtimeStr[runtimeStr.length-1] = binaryPath;
        try {
            return Runtime.getRuntime().exec(runtimeStr);
        } catch (IOException e) {
            return null;
        }
    }

    private static final class ThreadConsumer extends Thread {
        private final Process p;
        private final List<String> list;

        ThreadConsumer(Process p, List<String> files) {
            super();
            this.p = p;
            this.list = files;
        }

        @Override
        public void run() {
            try {
                populateGCDAFiles(p.getInputStream());
            } catch (IOException e) {
            }
        }

        private void populateGCDAFiles(InputStream s) throws IOException {
            InputStreamReader isr = new InputStreamReader(s);
            LineNumberReader lnr = new LineNumberReader(isr);
            String line = null;
            while ((line = lnr.readLine()) != null) {
                if (line.endsWith(".gcda")) //$NON-NLS-1$
                {
                    // absolute .gcda filepaths retrieved using the "strings" tool may
                    // be prefixed by random printable characters so strip leading
                    // characters until the filepath starts with "X:/", "X:\", "/"  or "\"
                    // FIXME: need a more robust mechanism to locate .gcda files [Bugzilla 329710]
                    while ((line.length() > 6) && !line.matches("^([A-Za-z]:)?[/\\\\].*")) { //$NON-NLS-1$
                        line = line.substring(1);
                    }
                    IPath p = new Path(line);
                    String filename = p.toString();

                    if (!list.contains(filename)) {
                        list.add(filename);
                    }
                }
            }
        }
    }

    /**
     * @return the sourceMap
     */
    public HashMap<String, SourceFile> getSourceMap() {
        return sourceMap;
    }

    private void addSourceLookup(Map<File, File> map, File hostPath, File compilerPath) {
        while (hostPath.getName().equals(compilerPath.getName())) {
            hostPath = hostPath.getParentFile();
            compilerPath = compilerPath.getParentFile();
        }
        map.put(compilerPath, hostPath);
    }

}
