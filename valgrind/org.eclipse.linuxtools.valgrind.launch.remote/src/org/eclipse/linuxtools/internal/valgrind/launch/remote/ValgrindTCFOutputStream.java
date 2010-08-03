/*******************************************************************************
 * Copyright (c) 2010 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch.remote;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.tcf.services.IStreams.DoneWrite;

public class ValgrindTCFOutputStream extends OutputStream {
	private IStreams streamsService;
	private String streamId;
	private transient boolean done;
	private Exception ex;
	
	public ValgrindTCFOutputStream(IChannel channel, String streamId) {
		this.streamId = streamId;
		streamsService = channel.getRemoteService(IStreams.class);
	}


	@Override
	public void write(int b) throws IOException {
		write1(new byte[] { (byte) b }, 0, 1);
	}


	private void write1(final byte[] b, final int off, final int len) throws IOException {
		done = false;
		ex = null;

		streamsService.write(streamId, b, off, len, new DoneWrite() {

			public void doneWrite(IToken token, Exception error) {
				if (error != null) {
					ex = null;
				}
				else {
					done = true;
				}
			}
		});
		
		try {
			while (!done) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
		}
		
		if (ex != null) {
			throw new IOException(ex);
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0)
				|| ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		
		write1(b, off, len);
	}
}
