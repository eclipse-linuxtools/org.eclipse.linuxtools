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

public interface STNMSymbolsHandler {

    public void foundUndefSymbol(String symbol);

    public void foundTextSymbol(String symbol, String address);

    public void foundBssSymbol(String symbol, String address);

    public void foundDataSymbol(String symbol, String address);

}
