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
package com.st.stgprof.utils;

import java.io.File;
import java.io.IOException;
import java.util.WeakHashMap;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.runtime.Path;

import com.st.flexperf.binutils.utils.STNMFactory;
import com.st.flexperf.binutils.utils.STNMSymbolsHandler;
import com.st.flexperf.binutils.utils.STSymbolManager;

public class STGprofProgramChecker implements STNMSymbolsHandler {

	private boolean mcountFound = false;
	private boolean mcleanupFound = false;
	private long timestamp;
	private final static WeakHashMap<File, STGprofProgramChecker> map = new WeakHashMap<File, STGprofProgramChecker>();
	/** Private Constructor */
	private STGprofProgramChecker(long timestamp) {
		this.timestamp = timestamp;
	}

	private static STGprofProgramChecker getProgramChecker(IBinaryObject object) throws IOException {
		File program = object.getPath().toFile();
		STGprofProgramChecker pg = map.get(program);
		if (pg == null) {
			pg = new STGprofProgramChecker(program.lastModified());
			STNMFactory.getNM(object.getCPU(), object.getPath().toOSString(), pg);
			map.put(program, pg);
		} else {
			long fileTime = program.lastModified();
			if (fileTime > pg.timestamp) {
				pg.timestamp = fileTime;
				pg.mcleanupFound = false;
				pg.mcountFound = false;
				STNMFactory.getNM(object.getCPU(), object.getPath().toOSString(), pg);
			}
		}
		return pg;
	}

	public static boolean isGProfCompatible(String s) throws IOException {
		IBinaryObject object = STSymbolManager.sharedInstance.getBinaryObject(new Path(s));
		if (object == null) return false;
		return isGProfCompatible(object);
	}

	public static boolean isGProfCompatible(IBinaryObject object) throws IOException {
		STGprofProgramChecker pg = getProgramChecker(object);
		return pg.mcleanupFound && pg.mcountFound;
	}

	@Override
	public void foundBssSymbol(String symbol, String address) {
	}

	@Override
	public void foundDataSymbol(String symbol, String address) {
	}

	@Override
	public void foundTextSymbol(String symbol, String address) {
		if ("mcount".equals(symbol)
				|| "_mcount".equals(symbol)
				|| "__mcount".equals(symbol))
		{
			mcountFound = true;
		} else if ("mcleanup".equals(symbol)
				|| "_mcleanup".equals(symbol)
				|| "__mcleanup".equals(symbol))
		{
			mcleanupFound = true;
		}	
	}

	@Override
	public void foundUndefSymbol(String symbol) {
		if (symbol.startsWith("mcount@@GLIBC")
				|| symbol.startsWith("_mcount@@GLIBC")
				|| symbol.startsWith("__mcount@@GLIBC"))
		{
			mcountFound = true;
		} else if (symbol.startsWith("_mcleanup@@GLIBC")
				|| symbol.startsWith("mcleanup@@GLIBC")
				|| symbol.startsWith("__mcleanup@@GLIBC"))
		{
			mcleanupFound = true;
		}	
	}

}
