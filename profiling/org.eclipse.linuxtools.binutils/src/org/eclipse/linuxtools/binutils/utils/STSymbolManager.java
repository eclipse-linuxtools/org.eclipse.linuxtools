/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.binutils.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.binutils.Activator;

/**
 * This class Is a utility on top of c++filt and addr2line. It allows an easy conversion between address and source
 * location, and between mangled and demangled symbols.
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class STSymbolManager {

    /**
     * Auto dispose timeout: If some tools has been unused since more that this time (in ms), they are disposed.
     */
    private final static long AUTO_DISPOSE_TIMEOUT = 30000;

    /**
     * Singleton instance
     */
    public final static STSymbolManager sharedInstance = new STSymbolManager();

    private final static class AutoDisposeAddr2line {
        private Addr2line addr2line;
        private long startTime;
    }

    private final static class AutoDisposeCPPFilt {
        private CPPFilt cppfilt;
        private long startTime;
    }

    /** Map of all living instance of addr2line */
    private final HashMap<IBinaryObject, AutoDisposeAddr2line> addr2lines = new HashMap<IBinaryObject, AutoDisposeAddr2line>();
    /** Map of all living instance of cppfilt */
    private final HashMap<String, AutoDisposeCPPFilt> cppfilts = new HashMap<String, AutoDisposeCPPFilt>();

    /**
     * Constructor
     */
    private STSymbolManager() {
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                try {
                    do {
                        try {
                            Thread.sleep(AUTO_DISPOSE_TIMEOUT);
                        } catch (InterruptedException e) {
                            break;
                        }
                        cleanup();
                    } while (true);
                } catch (Exception _) {
                    Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, _.getMessage(), _);
                    Activator.getDefault().getLog().log(s);
                }
            }
        };
        new Thread(worker, "ST System Analysis Symbol Manager").start(); //$NON-NLS-1$
        // TODO: perhaps this thread has to be lazy-created ?
        // and perhaps this thread has to destroy itself when it is no longer
        // used ?
    }

    /**
	 * @since 4.1
	 */
    public synchronized void reset() {
        Iterator<Entry<IBinaryObject, AutoDisposeAddr2line>> iter = addr2lines.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<IBinaryObject, AutoDisposeAddr2line> entry = iter.next();
            AutoDisposeAddr2line ada2l = entry.getValue();
            ada2l.addr2line.dispose();
            ada2l.addr2line = null;
            iter.remove();
        }

        Iterator<Entry<String, AutoDisposeCPPFilt>> iter2 = cppfilts.entrySet().iterator();
        while (iter2.hasNext()) {
            Entry<String, AutoDisposeCPPFilt> entry = iter2.next();
            AutoDisposeCPPFilt adcppf = entry.getValue();
            adcppf.cppfilt.dispose();
            adcppf.cppfilt = null;
        }
    }

    /**
     * each {@link #AUTO_DISPOSE_TIMEOUT} ms, the unused addr2line and c++filt programs are disposed.
     */
    private synchronized void cleanup() {
        long currentTime = System.currentTimeMillis();
        Iterator<Entry<IBinaryObject, AutoDisposeAddr2line>> iter = addr2lines.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<IBinaryObject, AutoDisposeAddr2line> entry = iter.next();
            AutoDisposeAddr2line ada2l = entry.getValue();
            long diff = currentTime - ada2l.startTime;
            if (diff > AUTO_DISPOSE_TIMEOUT) {
                ada2l.addr2line.dispose();
                ada2l.addr2line = null;
                iter.remove();
            }
        }

        Iterator<Entry<String, AutoDisposeCPPFilt>> iter2 = cppfilts.entrySet().iterator();
        while (iter2.hasNext()) {
            Entry<String, AutoDisposeCPPFilt> entry = iter2.next();
            AutoDisposeCPPFilt adcppf = entry.getValue();
            long diff = currentTime - adcppf.startTime;
            if (diff > AUTO_DISPOSE_TIMEOUT) {
                if (adcppf.cppfilt != null) {
                    adcppf.cppfilt.dispose();
                    adcppf.cppfilt = null;
                }
                iter2.remove();
            }
        }
    }

    /**
     * Demangle the given symbol
     * @param symbol
     * @return The demangled symbol.
     */
    public synchronized String demangle(ISymbol symbol, IProject project) {
        String cpu = symbol.getBinaryObject().getCPU();
        String symbolName = symbol.getName();
        return demangleImpl(symbolName, cpu, project);
    }

    /**
     * Demangle the given symbol
     * @param program
     * @param symbolName
     * @return The demangled symbol.
     */
    public synchronized String demangle(IBinaryObject program, String symbolName, IProject project) {
        String cpu = program.getCPU();
        return demangleImpl(symbolName, cpu, project);
    }

    /**
     * Demangle the given symbol
     * @param symbol
     * @return
     */
    private synchronized String demangleImpl(String symbolName, String cpu, IProject project) {
        CPPFilt cppfilt = getCppFilt(cpu, project);
        if (cppfilt != null && (symbolName.startsWith("_Z") || symbolName.startsWith("_G"))) { //$NON-NLS-1$ //$NON-NLS-2$
            try {
                symbolName = cppfilt.getFunction(symbolName);
            } catch (IOException _) {
                // TODO: log the error ?
            }
        }
        return symbolName;
    }

    /**
     * @param symbol
     * @return the location (as returned by addr2line, e.g. file:line) of the given address
     */
    public synchronized String getLine(ISymbol symbol, IProject project) {
        IBinaryObject binary = symbol.getBinaryObject();
        IAddress address = symbol.getAddress();
        return getLine(binary, address, project);
    }

    /**
     * @param program
     * @param address
     * @return the location (as returned by addr2line, e.g. file:line) of the given address
     */
    public synchronized String getLine(IBinaryObject program, IAddress address, IProject project) {
        Addr2line addr2line = getAddr2line(program, project);
        if (addr2line == null)
            return "??:0"; //$NON-NLS-1$
        try {
            return addr2line.getLine(address);
        } catch (IOException _) {
            // TODO: log the error ?;
            // Perhaps log the error only once, because
            // this method is called many many times...
            return "??:0"; //$NON-NLS-1$
        }
    }

    /**
     * @param program
     * @param address
     *            an address, in hex, octal or decimal format (0xabcdef, 07654, 12345)
     * @return the location (as returned by addr2line, e.g. file:line) of the given address
     */
    public synchronized String getLine(IBinaryObject program, String address, IProject project) {
        IAddress addr = program.getAddressFactory().createAddress(address);
        return getLine(program, addr, project);
    }

    /**
     * @param program
     * @param address
     * @return the location (as returned by addr2line, e.g. file:line) of the given address
     */
    public synchronized String getLine(IBinaryObject program, long address, IProject project) {
        IAddress addr = program.getAddressFactory().createAddress(Long.toString(address));
        return getLine(program, addr, project);
    }

    /**
     * @param program
     * @param address
     * @return the line number of the given address
     */
    public synchronized int getLineNumber(IBinaryObject program, IAddress address, IProject project) {
        Addr2line addr2line = getAddr2line(program, project);
        if (addr2line == null)
            return -1;
        try {
            return addr2line.getLineNumber(address);
        } catch (IOException _) {
            // TODO: log the error ?;
            // Perhaps log the error only once, because
            // this method is called many many times...
            return -1;
        }
    }

    /**
     * @param program
     * @param address
     * @return the line number of the given address
     */
    public synchronized int getLineNumber(IBinaryObject program, String address, IProject project) {
        IAddress addr = program.getAddressFactory().createAddress(address);
        return getLineNumber(program, addr, project);
    }

    /**
     * @param program
     * @param address
     * @return the line number of the given address
     */
    public synchronized int getLineNumber(IBinaryObject program, long address, IProject project) {
        IAddress addr = program.getAddressFactory().createAddress(Long.toString(address));
        return getLineNumber(program, addr, project);
    }

    /**
     * @param symbol
     * @return the line number of the given symbol
     */
    public int getLineNumber(ISymbol symbol, IProject project) {
        IBinaryObject obj = symbol.getBinaryObject();
        IAddress address = symbol.getAddress();
        return getLineNumber(obj, address, project);
    }

    /**
     * @param program
     * @param address
     * @return the file name of the given address
     */
    public synchronized String getFileName(IBinaryObject program, IAddress address, IProject project) {
        Addr2line addr2line = getAddr2line(program, project);
        if (addr2line == null)
            return null;
        try {
            return addr2line.getFileName(address);
        } catch (IOException _) {
            // TODO: log the error ?;
            // Perhaps log the error only once, because
            // this method is called many many times...
            return null;
        }
    }

    /**
     * @param program
     * @param address
     * @return the file name of the given address
     */
    public synchronized String getFileName(IBinaryObject program, String address, IProject project) {
        IAddress addr = program.getAddressFactory().createAddress(address);
        return getFileName(program, addr, project);
    }

    /**
     * @param program
     * @param address
     * @return the file name of the given address
     */
    public synchronized String getFileName(IBinaryObject program, long address, IProject project) {
        IAddress addr = program.getAddressFactory().createAddress(Long.toString(address));
        return getFileName(program, addr, project);
    }

    /**
     * @param symbol
     * @return the filename of the given symbol
     */
    public String getFilename(ISymbol symbol, IProject project) {
        IBinaryObject obj = symbol.getBinaryObject();
        IAddress address = symbol.getAddress();
        return getFileName(obj, address, project);
    }

    /**
     * @param program
     * @param address
     *            an address
     * @return the function name of the given address, based on addr2line output
     */
    public synchronized String getFunctionName(IBinaryObject program, long address, IProject project) {
        IAddress addr = program.getAddressFactory().createAddress(Long.toString(address));
        return getFunctionName(program, addr, project);
    }

    /**
     * @param program
     * @param address
     *            an address, in hex, octal or decimal format (0xabcdef, 07654, 12345)
     * @return the function name of the given address, based on addr2line output
     */
    public synchronized String getFunctionName(IBinaryObject program, String address, IProject project) {
        IAddress addr = program.getAddressFactory().createAddress(address);
        return getFunctionName(program, addr, project);
    }

    /**
     * @param program
     * @param address
     *            an address
     * @return the function name of the given address, based on addr2line output
     */
    public synchronized String getFunctionName(IBinaryObject program, IAddress address, IProject project) {
        Addr2line addr2line = getAddr2line(program, project);
        if (addr2line == null)
            return null;
        try {
            return addr2line.getFunction(address);
        } catch (IOException _) {
            // TODO: log the error ?;
            // Perhaps log the error only once, because
            // this method is called many many times...
            return null;
        }
    }

    /**
     * Gets the c++filt support for the given program Note that the instance if kept in a local hashmap, and discarded
     * after 30 seconds of inactivity.
     * @param program
     * @return an instance of CPPFilt suitable for the given program
     */
    private synchronized CPPFilt getCppFilt(String cpu, IProject project) {
        AutoDisposeCPPFilt adCppfilt = cppfilts.get(cpu);
        if (adCppfilt == null) {
            adCppfilt = new AutoDisposeCPPFilt();
            cppfilts.put(cpu, adCppfilt);
        }
        if (adCppfilt.cppfilt == null) {
            try {
                adCppfilt.cppfilt = STCPPFiltFactory.getCPPFilt(cpu, project);
            } catch (IOException _) {
                // TODO: log the error ?;
                // Perhaps log the error only once, because
                // this method is called many many times...
                return null;
            }
        }
        adCppfilt.startTime = System.currentTimeMillis();
        return adCppfilt.cppfilt;
    }

    /**
     * Gets the addr2line support for the given program Note that the instance if kept in a local hashmap, and discarded
     * after 30 seconds of inactivity.
     * @param program
     * @return an instance of Addr2line suitable for the given program
     */
    private synchronized Addr2line getAddr2line(IBinaryObject program, IProject project) {
        AutoDisposeAddr2line adAddr2line = addr2lines.get(program);
        if (adAddr2line == null) {
            adAddr2line = new AutoDisposeAddr2line();
            addr2lines.put(program, adAddr2line);
        }
        if (adAddr2line.addr2line == null) {
            try {
                adAddr2line.addr2line = STAddr2LineFactory.getAddr2line(program.getCPU(), program.getPath()
                        .toOSString(), project);
            } catch (IOException _) {
                // TODO: log the error ?;
                // Perhaps log the error only once, because
                // this method is called many many times...
                return null;
            }
        }
        adAddr2line.startTime = System.currentTimeMillis();
        return adAddr2line.addr2line;
    }

    /**
     * Gets the IBinaryObject corresponding to the given path (absolute path in filesystem). If a IBinaryObject
     * corresponding to the given path has been already built by eclipse, return it. Otherwise build a new
     * IBinaryObject, according to project preferences. Note that it may return null if the path is invalid, or is not a
     * valid binary file.
     * @param loc
     * @return a IBinaryObject
     */
    public IBinaryObject getBinaryObject(String loc) {
        IPath path = new Path(loc);
        return getBinaryObject(path);
    }

    /**
     * Gets the IBinaryObject corresponding to the given path (absolute path in filesystem). If a IBinaryObject
     * corresponding to the given path has been already built by eclipse, return it. Otherwise build a new
     * IBinaryObject, according to project preferences. Note that it may return null if the path is invalid, or is not a
     * valid binary file.
     * @param path
     * @return a IBinaryObject
     */
    public IBinaryObject getBinaryObject(IPath path) {

        return getBinaryObject(path, null);
    }

    /**
     * Gets the IBinaryObject corresponding to the given path (absolute path in filesystem). If a IBinaryObject
     * corresponding to the given path has been already built by eclipse, return it. Otherwise build a new
     * IBinaryObject, according to project preferences. Note that it may return null if the path is invalid, or is not a
     * valid binary file.
     * @param path
     * @param defaultparser
     * @return a IBinaryObject
     */
    public IBinaryObject getBinaryObject(IPath path, IBinaryParser defaultparser) {
        IFile c = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
        List<IBinaryParser> parsers;
        if (c != null) {
            IBinaryObject object = getAlreadyExistingBinaryObject(c);
            if (object != null)
                return object;
            parsers = getBinaryParser(c.getProject());
        } else {
            parsers = new LinkedList<IBinaryParser>();
        }

        if (defaultparser == null) {
            try {
                defaultparser = CCorePlugin.getDefault().getDefaultBinaryParser();
            } catch (CoreException _) {
                Activator.getDefault().getLog().log(_.getStatus());
            }
        }
        if (defaultparser != null) {
            parsers.add(defaultparser);
        }
        IBinaryObject ret = buildBinaryObject(path, parsers);
        if (ret == null) { // trying all BinaryParsers...
            parsers.clear();
            IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
                    CCorePlugin.BINARY_PARSER_SIMPLE_ID);
            for (IExtension extension : extensionPoint.getExtensions()) {
                if (extension != null) {
                    IConfigurationElement element[] = extension.getConfigurationElements();
                    for (IConfigurationElement element2 : element) {
                        if (element2.getName().equalsIgnoreCase("cextension")) { //$NON-NLS-1$
                            IBinaryParser parser;
                            try {
                                parser = (IBinaryParser) element2.createExecutableExtension("run"); //$NON-NLS-1$
                                parsers.add(parser);
                            } catch (CoreException e) {
                            }
                        }
                    }
                }
            }
            ret = buildBinaryObject(path, parsers);
        }
        return ret;
    }

    /**
     * Validate the binary file. In particular, verify that this binary file can be decoded.
     * @param o
     * @return the binary object, or null.
     */
    private IBinaryObject validateBinary(IBinaryFile o) {
        if (o instanceof IBinaryObject) {
            IBinaryObject object = (IBinaryObject) o;
            String s = null;
            try {
                s = object.getCPU(); //
            } catch (Exception _) {
            }
            if (s != null && s.length() > 0) {
                return object;
            }
        }
        return null;
    }

    private IBinaryObject buildBinaryObject(IPath path, List<IBinaryParser> parsers) {
        for (IBinaryParser iBinaryParser : parsers) {
            IBinaryObject o = buildBinaryObject(path, iBinaryParser);
            if (o != null)
                return o;
        }
        return null;
    }

    /**
     * Build a binary object with the given file and parser. Also verify that the builded binary object is valid (@see
     * #validateBinary)
     */
    private IBinaryObject buildBinaryObject(IPath path, IBinaryParser parser) {
        if (parser == null)
            return null;
        IBinaryFile bf = null;
        try {
            bf = parser.getBinary(path);
        } catch (IOException _) {
            // do nothing ?
        }
        return validateBinary(bf);
    }

    /**
     * Ask the workbench to find if a binary object already exist for the given file
     */
    private IBinaryObject getAlreadyExistingBinaryObject(IFile c) {
        IProject project = c.getProject();
        if (project != null && project.exists()) {
            ICProject cproject = CoreModel.getDefault().create(project);
            if (cproject != null) {
                try {
                    IBinary[] b = cproject.getBinaryContainer().getBinaries();
                    for (IBinary binary : b) {
                        IResource r = binary.getResource();
                        if (r.equals(c)) {
                            IBinaryObject binaryObject = (IBinaryObject) binary.getAdapter(IBinaryObject.class);
                            return validateBinary(binaryObject);
                        }
                    }
                } catch (CModelException _) {
                }
            }
        }
        return null;
    }

    /**
     * Retrieve the list of binary parsers defined for the given project
     */
    private List<IBinaryParser> getBinaryParser(IProject project) {
        List<IBinaryParser> parsers = new LinkedList<IBinaryParser>();

        ICProjectDescription projDesc = CCorePlugin.getDefault().getProjectDescription(project);
        if (projDesc == null)
            return parsers;
        ICConfigurationDescription[] cfgs = projDesc.getConfigurations();
        String[] binaryParserIds = CoreModelUtil.getBinaryParserIds(cfgs);

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
                CCorePlugin.BINARY_PARSER_SIMPLE_ID);
        for (String id : binaryParserIds) {
            IExtension extension = extensionPoint.getExtension(id);
            if (extension != null) {
                IConfigurationElement element[] = extension.getConfigurationElements();
                for (IConfigurationElement element2 : element) {
                    if (element2.getName().equalsIgnoreCase("cextension")) { //$NON-NLS-1$
                        try {
                            IBinaryParser parser = (IBinaryParser) element2.createExecutableExtension("run"); //$NON-NLS-1$
                            if (parser != null)
                                parsers.add(parser);
                        } catch (CoreException _) {
                            // TODO: handle exception ?
                        }
                    }
                }
            }
        }
        return parsers;
    }

}
