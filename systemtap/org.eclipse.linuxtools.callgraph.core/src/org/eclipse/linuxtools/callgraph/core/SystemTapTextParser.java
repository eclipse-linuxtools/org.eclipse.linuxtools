package org.eclipse.linuxtools.callgraph.core;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class SystemTapTextParser extends SystemTapParser{

	protected String contents;

	@Override
	public IStatus nonRealTimeParsing() {
		contents = Helper.readFile(sourcePath);
		System.out.println(contents);
		return Status.OK_STATUS;
	}

	@Override
	protected void initialize() {
		System.out.println("INITIALIZING");
	}

	@Override
	public IStatus realTimeParsing() {
		if (!(internalData instanceof BufferedReader))
			return Status.CANCEL_STATUS;

		BufferedReader buff = (BufferedReader) internalData;
		StringBuffer text = new StringBuffer();

		String line;
		try {
			while ((line = buff.readLine()) != null) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				text.append(line);
			}
			setData(text.toString());
			view.update();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return Status.OK_STATUS;
	}

}
