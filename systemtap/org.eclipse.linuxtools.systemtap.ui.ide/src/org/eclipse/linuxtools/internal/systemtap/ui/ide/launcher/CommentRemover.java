package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

class CommentRemover {

	public static String exec(String contents) {
		if (contents.isEmpty()) {
			return ""; //$NON-NLS-1$
		}

		char curchar, nxtchar;
		boolean inQuotes = false;
		boolean inComment = false;

		int c = 0;
		StringBuffer buffer = new StringBuffer();

		do {
			curchar = contents.charAt(c++);
			nxtchar = c < contents.length() ? contents.charAt(c) : '\0';

			// Comment tags don't count if they are in a string.
			if (!inQuotes) {
				if (!inComment) {
					if (curchar == '#' || (curchar == '/' && nxtchar == '/')) {
						inQuotes = false;
						c = contents.indexOf('\n', c + 1);
						continue;
					}
					if (curchar == '/' && nxtchar == '*') {
						inComment = true;
						c++; //Skip the * on the next character scan.
						continue;
					}
				}
				else if (curchar == '*' && nxtchar == '/') {
					inComment = false;
					c++; //Skip the / on the next character scan.
					continue;
				}
			}

			// Quotes only count if they aren't commented out.
			if (!inComment) {
				if (curchar == '\"') {
					inQuotes = !inQuotes;
				}
				else if (curchar == '\n') {
					inQuotes = false;
				}

				buffer.append(curchar);
			}

		} while (c != -1 && c < contents.length());

		return buffer.toString();
	}
}