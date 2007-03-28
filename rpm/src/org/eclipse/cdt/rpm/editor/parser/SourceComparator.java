package org.eclipse.cdt.rpm.editor.parser;

import java.util.Comparator;

public class SourceComparator implements Comparator {
	public int compare(Object arg0, Object arg1) {
		SpecfileSource source0 = (SpecfileSource) arg0;
		SpecfileSource source1 = (SpecfileSource) arg1;
		if (source0 == null)
			return -1;
		if (source1 == null)
			return 1;
		if (source0.getNumber() < source1.getNumber())
			return -1;
		else if (source0.getNumber() == source1.getNumber())
			return 0;
		else
			return 1;
	}
}
