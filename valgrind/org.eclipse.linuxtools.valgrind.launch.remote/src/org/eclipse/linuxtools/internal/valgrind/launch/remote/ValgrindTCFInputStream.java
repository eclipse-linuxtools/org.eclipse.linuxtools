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
import java.io.InputStream;
import java.util.LinkedList;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.tcf.services.IStreams.DoneRead;

public class ValgrindTCFInputStream extends InputStream {
	private LinkedList<RemoteLaunchStep> launchSteps;
	private IChannel channel;
	private IStreams streamsService;
	private String streamId;
	private boolean done;
	private byte[] buf;
	private Exception ex;
	
	public ValgrindTCFInputStream(IChannel channel, String streamId, LinkedList<RemoteLaunchStep> launchSteps) {
		this.channel = channel;
		this.streamId = streamId;
		this.launchSteps = launchSteps;
		streamsService = channel.getRemoteService(IStreams.class);
	}

	@Override
	public int read() throws IOException {
		read1(1);
		
		return buf[0];
	}

	private void read1(final int size) throws IOException {
		done = false;
		ex = null;
		buf = null;
		
		new RemoteLaunchStep(launchSteps, channel) {
			@Override
			public void start() throws Exception {
				streamsService.read(streamId, size, new DoneRead() {
					
					public void doneRead(IToken token, Exception error, int lost_size,
							byte[] data, boolean eos) {
						if (error != null) {
							ex = error;
						}
						else {
							buf = data;
						}
						done = true;
						done();
					}
				});
			}
		};
		
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
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		
		read1(len);
		
		// Did we read less than requested?
		if (buf.length < len) {
			len = buf.length;
		}
		
		System.arraycopy(buf, 0, b, off, len);
		return len;
	}
}
