package org.eclipse.linuxtools.valgrind.massif.model;

import java.util.ArrayList;

import org.eclipse.linuxtools.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot;
import org.eclipse.swt.graphics.Image;

public class SnapshotTreeElement extends MassifTreeElement {
	protected MassifSnapshot snapshot;
	
	public SnapshotTreeElement(MassifTreeElement parent, MassifSnapshot snapshot) {
		this.parent = parent;
		this.snapshot = snapshot;
		// must have a root element
		MassifHeapTreeNode root = snapshot.getRoot();
		children = new ArrayList<MassifTreeElement>(root.getChildren().length);
	}
	
	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getText() {
		return "Snapshot " + snapshot.getNumber();
	}
	
	public MassifSnapshot getSnapshot() {
		return snapshot;
	}
}
