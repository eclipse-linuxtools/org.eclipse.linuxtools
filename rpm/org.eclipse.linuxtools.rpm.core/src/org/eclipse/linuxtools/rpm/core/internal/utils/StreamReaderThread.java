/*******************************************************************************
 * Copyright (c) 2005, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.internal.utils;

import java.io.InputStreamReader;
import java.io.InputStream;

/**
 * Thread for reading input and output streams
 */
public class StreamReaderThread extends Thread
{
    StringBuffer mOut;
    InputStreamReader mIn;
    
    public StreamReaderThread(InputStream in, StringBuffer out)
    {
    mOut=out;
    mIn=new InputStreamReader(in);
    }
    
    @Override
	public void run()
    {
    int ch;
    try {
        while(-1 != (ch=mIn.read()))
            mOut.append((char)ch);
        }
    catch (Exception e)
        {
        mOut.append("\nRead error:"+e.getMessage()); //$NON-NLS-1$
        }
    }
}
