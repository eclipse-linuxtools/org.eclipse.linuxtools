package org.eclipse.linuxtools.valgrind.massif.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot;
import org.eclipse.swt.graphics.Image;

public class RootTreeElement extends MassifTreeElement {
	protected MassifSnapshot[] snapshots;
	
	public RootTreeElement(MassifSnapshot[] snapshots) {
		this.snapshots = snapshots;
		parent = null;
		children = new ArrayList<MassifTreeElement>();
		for (MassifSnapshot snapshot : snapshots) {
			children.add(new SnapshotTreeElement(this, snapshot));
		}
	}
	
	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getText() {
		return null;
	}
	
	public MassifTreeElement getElement(MassifSnapshot snapshot) {
		MassifTreeElement e = null;
		int ix = Arrays.asList(snapshots).indexOf(snapshot);
		if (ix >= 0) {
			e = children.get(ix);
		}
		return e;
	}

}
