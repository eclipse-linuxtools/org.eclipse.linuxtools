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
package com.st.flexperf.binutils.utils;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import com.st.flexperf.binutils.Activator;

/**
 * This class Is a utility on top of c++filt and addr2line.
 * 
 * It allows an easy conversion between address and source location,
 * and between mangled and demangled symbols.
 * 
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class STSymbolManager {

	/**
	 * Auto dispose timeout:
	 * If some tools has been unused since more that this time (in ms),
	 * they are disposed.
	 */
	private final static long AUTO_DISPOSE_TIMEOUT = 30000;

	/**
	 * Singleton instance
	 */
	public final static STSymbolManager sharedInstance = new STSymbolManager();

	private final class AutoDisposeAddr2line {
		private Addr2line addr2line;
		private long startTime;
	}

	private final class AutoDisposeCPPFilt {
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
					Status s = new Status(Status.ERROR, Activator.PLUGIN_ID, _.getMessage(), _);
					Activator.getDefault().getLog().log(s);
				}
			}
		};
		new Thread(worker, "ST System Analysis Symbol Manager").start(); //$NON-NLS-1$
		// TODO: perhaps this thread has to be lazy-created ?
		// and perhaps this thread has to destroy itself when it is no longer used ?
	}

	/**
	 * each {@link #AUTO_DISPOSE_TIMEOUT} ms, the unused addr2line and c++filt programs
	 * are disposed.
	 */
	private synchronized void cleanup() {
		long currentTime = System.currentTimeMillis();
		Iterator<Entry<IBinaryObject, AutoDisposeAddr2line> > iter = addr2lines.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<IBinaryObject, AutoDisposeAddr2line> entry = iter.next();
			AutoDisposeAddr2line ada2l = entry.getValue();
			long diff = currentTime-ada2l.startTime;
			if (diff > AUTO_DISPOSE_TIMEOUT) {
				ada2l.addr2line.dispose();
				ada2l.addr2line = null;
				iter.remove();
			}
		}

		Iterator<Entry<String, AutoDisposeCPPFilt> > iter2 = cppfilts.entrySet().iterator();
		while (iter2.hasNext()) {
			Entry<String, AutoDisposeCPPFilt> entry = iter2.next();
			AutoDisposeCPPFilt adcppf = entry.getValue();
			long diff = currentTime-adcppf.startTime;
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
	 * @return
	 */
	public synchronized String demangle(ISymbol symbol) {
		String cpu = symbol.getBinaryObject().getCPU();
		String symbolName = symbol.getName();
		return demangleImpl(symbolName, cpu);
	}

	/**
	 * Demangle the given symbol
	 * @param program 
	 * @param symbolName 
	 * @return
	 */
	public synchronized String demangle(IBinaryObject program, String symbolName) {
		String cpu = program.getCPU();
		return demangleImpl(symbolName, cpu);
	}


	/**
	 * Demangle the given symbol
	 * @param symbol
	 * @return
	 */
	private synchronized String demangleImpl(String symbolName, String cpu) {
		CPPFilt cppfilt = getCppFilt(cpu);
		if (cppfilt != null && (symbolName.startsWith("_Z") || symbolName.startsWith("_G"))) {
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
	public synchronized String getLine(ISymbol symbol) {
		IBinaryObject binary = symbol.getBinaryObject();
		IAddress address = symbol.getAddress();
		return getLine(binary, address);
	}


	/**
	 * @param program 
	 * @param address 
	 * @return the location (as returned by addr2line, e.g. file:line) of the given address
	 */
	public synchronized String getLine(IBinaryObject program, IAddress address) {
		Addr2line addr2line = getAddr2line(program);
		if (addr2line == null) return "??:0";
		try {
			return addr2line.getLine(address);
		} catch (IOException _) {
			// TODO: log the error ?;
			// Perhaps log the error only once, because 
			// this method is called many many times...
			return "??:0";
		}
	}

	/**
	 * @param program 
	 * @param address an address, in hex, octal or decimal format (0xabcdef, 07654, 12345)
	 * @return the location (as returned by addr2line, e.g. file:line) of the given address
	 */
	public synchronized String getLine(IBinaryObject program, String address) {
		IAddress addr = program.getAddressFactory().createAddress(address);
		return getLine(program, addr);
	}

	/**
	 * @param program 
	 * @param address
	 * @return the location (as returned by addr2line, e.g. file:line) of the given address
	 */
	public synchronized String getLine(IBinaryObject program, long address) {
		IAddress addr = program.getAddressFactory().createAddress(Long.toString(address));
		return getLine(program, addr);
	}

	/**
	 * @param program 
	 * @param address 
	 * @return the line number of the given address
	 */
	public synchronized int getLineNumber(IBinaryObject program, IAddress address) {
		Addr2line addr2line = getAddr2line(program);
		if (addr2line == null) return -1;
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
	public synchronized int getLineNumber(IBinaryObject program, String address) {
		IAddress addr = program.getAddressFactory().createAddress(address);
		return getLineNumber(program, addr);
	}

	/**
	 * @param program 
	 * @param address 
	 * @return the line number of the given address
	 */
	public synchronized int getLineNumber(IBinaryObject program, long address) {
		IAddress addr = program.getAddressFactory().createAddress(Long.toString(address));
		return getLineNumber(program, addr);
	}

	/**
	 * 
	 * @param symbol
	 * @return the line number of the given symbol
	 */
	public int getLineNumber(ISymbol symbol) {
		IBinaryObject obj = symbol.getBinaryObject();
		IAddress address = symbol.getAddress();
		return getLineNumber(obj,address);
	}

	/**
	 * 
	 * @param program 
	 * @param address 
	 * @return the file name of the given address
	 */
	public synchronized String getFileName(IBinaryObject program, IAddress address) {
		Addr2line addr2line = getAddr2line(program);
		if (addr2line == null) return null;
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
	 * 
	 * @param program 
	 * @param address 
	 * @return the file name of the given address
	 */
	public synchronized String getFileName(IBinaryObject program, String address) {
		IAddress addr = program.getAddressFactory().createAddress(address);
		return getFileName(program, addr);
	}

	/**
	 * 
	 * @param program 
	 * @param address 
	 * @return the file name of the given address
	 */
	public synchronized String getFileName(IBinaryObject program, long address) {
		IAddress addr = program.getAddressFactory().createAddress(Long.toString(address));
		return getFileName(program, addr);
	}

	/**
	 * 
	 * @param symbol
	 * @return the filename of the given symbol
	 */
	public String getFilename(ISymbol symbol) {
		IBinaryObject obj = symbol.getBinaryObject();
		IAddress address = symbol.getAddress();
		return getFileName(obj,address);
	}


	/**
	 * @param program 
	 * @param address an address
	 * @return the function name of the given address, based on addr2line output
	 */
	public synchronized String getFunctionName(IBinaryObject program, long address) {
		IAddress addr = program.getAddressFactory().createAddress(Long.toString(address));
		return getFunctionName(program, addr);
	}

	/**
	 * @param program 
	 * @param address an address, in hex, octal or decimal format (0xabcdef, 07654, 12345)
	 * @return the function name of the given address, based on addr2line output
	 */
	public synchronized String getFunctionName(IBinaryObject program, String address) {
		IAddress addr = program.getAddressFactory().createAddress(address);
		return getFunctionName(program, addr);
	}

	/**
	 * @param program 
	 * @param address an address
	 * @return the function name of the given address, based on addr2line output
	 */
	public synchronized String getFunctionName(IBinaryObject program, IAddress address) {
		Addr2line addr2line = getAddr2line(program);
		if (addr2line == null) return null;
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
	 * Gets the c++filt support for the given program
	 * Note that the instance if kept in a local hashmap, and discarded after 30 seconds of inactivity.
	 * @param program
	 * @return an instance of CPPFilt suitable for the given program
	 */
	private synchronized CPPFilt getCppFilt(String cpu) {
		AutoDisposeCPPFilt adCppfilt = cppfilts.get(cpu);
		if (adCppfilt == null) {
			adCppfilt = new AutoDisposeCPPFilt();
			cppfilts.put(cpu, adCppfilt);
		}
		if (adCppfilt.cppfilt == null) {
			try {
				adCppfilt.cppfilt = STCPPFiltFactory.getCPPFilt(cpu);
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
	 * Gets the addr2line support for the given program
	 * Note that the instance if kept in a local hashmap, and discarded after 30 seconds of inactivity.
	 * @param program
	 * @return an instance of Addr2line suitable for the given program
	 */
	private synchronized Addr2line getAddr2line(IBinaryObject program) {
		AutoDisposeAddr2line adAddr2line = addr2lines.get(program);
		if (adAddr2line == null) {
			adAddr2line = new AutoDisposeAddr2line();
			addr2lines.put(program, adAddr2line);
		}
		if (adAddr2line.addr2line == null) {
			try {
				adAddr2line.addr2line = STAddr2LineFactory.getAddr2line(program.getCPU(), program.getPath().toOSString());
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
	 * Gets the IBinaryObject corresponding to the given path (absolute path in filesystem).
	 * If a IBinaryObject corresponding to the given path has been already built by eclipse, return it.
	 * Otherwise build a new IBinaryObject, according to project preferences.
	 * Note that it may return null if the path is invalid, or is not a valid binary file.
	 * @param path
	 * @return a IBinaryObject
	 */
	public IBinaryObject getBinaryObject(String loc) {
		IPath path = new Path(loc);
		return getBinaryObject(path);
	}


	/**
	 * Gets the IBinaryObject corresponding to the given path (absolute path in filesystem).
	 * If a IBinaryObject corresponding to the given path has been already built by eclipse, return it.
	 * Otherwise build a new IBinaryObject, according to project preferences.
	 * Note that it may return null if the path is invalid, or is not a valid binary file.
	 * @param path
	 * @return a IBinaryObject
	 */
	public IBinaryObject getBinaryObject(IPath path) {

		return getBinaryObject(path, null);
	}

	/**
	 * Gets the IBinaryObject corresponding to the given path (absolute path in filesystem).
	 * If a IBinaryObject corresponding to the given path has been already built by eclipse, return it.
	 * Otherwise build a new IBinaryObject, according to project preferences.
	 * Note that it may return null if the path is invalid, or is not a valid binary file.
	 * @param path
	 * @param default parser
	 * @return a IBinaryObject
	 */
	public IBinaryObject getBinaryObject(IPath path, IBinaryParser defaultparser) {
		IFile c = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
		if (c != null) {
			IBinaryObject object = getAlreadyExistingBinaryObject(c);
			if (isBinaryParserCompatible(object, defaultparser)) {
				return object;
			} else if (defaultparser == null) {
				defaultparser = getBinaryParser(c.getProject());
			}
		}
		if (defaultparser == null) {
			try {
				defaultparser = CCorePlugin.getDefault().getDefaultBinaryParser();
			} catch (CoreException _) {
				Activator.getDefault().getLog().log(_.getStatus());
			}
		}
		return buildBinaryObject(path, defaultparser);
	}
	
	private boolean isBinaryParserCompatible(IBinaryObject o, IBinaryParser parser) {
		if (o == null) return false;
		if (parser == null) return true;
		IBinaryParser p = o.getBinaryParser();
		return (p.getClass().equals(parser.getClass()));
	}


	private IBinaryObject buildBinaryObject(IPath path, IBinaryParser parser) {
		if (parser == null) return null;
		IBinaryFile bf = null;
		try {
			bf = parser.getBinary(path);
		} catch (IOException _) {
			// do nothing ?
		}
		if (bf instanceof IBinaryObject) {
			IBinaryObject object = (IBinaryObject) bf;
			String s = null;
			try {
				s = object.getCPU(); // 
			} catch (Exception _) {}
			if (s.length() > 0) {
				return object;
			}
		}
		return null;
	}


	private IBinaryObject getAlreadyExistingBinaryObject(IFile c) {
		IProject project = c.getProject();
		if (project != null && project.exists()) {
			ICProject cproject = CoreModel.getDefault().create(project);
			if (cproject != null) {
				try {
					IBinary[] b = cproject.getBinaryContainer()
					.getBinaries();
					for (IBinary binary : b) {
						if (binary.getResource().equals(c) && binary instanceof IBinaryObject) {
							return ((IBinaryObject)binary);
						}
					}
				} catch (CModelException _) {
				}
			}
		}
		return null;
	}

	private IBinaryParser getBinaryParser(IProject project) {
		try {
			ICExtensionReference[] parserRefs = CCorePlugin.getDefault().getBinaryParserExtensions(project);
			for (ICExtensionReference parserRef: parserRefs) {
				ICExtension extension = parserRef.createExtension();
				if (extension instanceof IBinaryParser) {
					return (IBinaryParser) extension;
				}
			}
		} catch (CoreException _) {
			Activator.getDefault().getLog().log(_.getStatus());
		}
		return null;
	}


}
