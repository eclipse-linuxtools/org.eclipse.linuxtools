package org.eclipse.linuxtools.valgrind.core;

import java.io.IOException;

import org.eclipse.osgi.util.NLS;

public class AbstractValgrindTextParser {

	public AbstractValgrindTextParser() {
		super();
	}

	protected Long parseLongValue(String line, String delim)
			throws IOException {
				Long result = null;
				String[] parts = line.split(delim);
				if (parts.length > 1 && isNumber(parts[1])) {
					result = Long.parseLong(parts[1]);
				}
				else {
					fail(line);
				}
				return result;
			}

	protected String parseStrValue(String line, String delim)
			throws IOException {
				String result = null;
				String[] parts = line.split(delim);
				if (parts.length > 1) {
					result = parts[1];
				}
				else {
					fail(line);
				}
				return result;
			}

	protected void fail(String line) throws IOException {
		throw new IOException(NLS.bind(Messages.getString("AbstractValgrindTextParser.Parsing_output_failed"), line)); //$NON-NLS-1$
	}

	protected boolean isNumber(String string) {
		boolean result = true;
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isDigit(chars[i])) {
				result = false;
			}
		}
		return result;
	}

}