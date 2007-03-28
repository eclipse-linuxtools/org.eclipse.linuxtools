package org.eclipse.linuxtools.rpm.ui.editor.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpecfilePackageContainer extends SpecfileElement {
	List packages;
	
	public SpecfilePackageContainer() {
		packages = new ArrayList();
	}
	
	public SpecfilePackage[] getPackages() {
		try {
			Object [] objs = packages.toArray();
			SpecfilePackage [] packs = new SpecfilePackage[objs.length];
			for (int i = 0; i <  objs.length; i++) {
				SpecfilePackage pack = (SpecfilePackage) objs[i];
				packs[i] = pack;
			}
			return packs;
		}catch (Exception e){
			e.printStackTrace();
		}
		return new SpecfilePackage[0] ;
	}
	
	void addPackage(SpecfilePackage subPackage) {
		packages.add(subPackage);
	}
	
	public int getLineStartPosition() {
		if ((packages == null) || (packages.size() == 0))
			return 0;
		
		int lowestStartLine = Integer.MAX_VALUE;
		
		for (Iterator iter = packages.iterator(); iter.hasNext();) {
			SpecfilePackage subPackage = (SpecfilePackage) iter.next();
			if (subPackage.getLineStartPosition() < lowestStartLine)
				lowestStartLine = subPackage.getLineStartPosition();
		}
		
		if (lowestStartLine == Integer.MAX_VALUE)
			return 0;
		return lowestStartLine;
	}
	
	public int getLineEndPosition() {
		if ((packages == null) || (packages.size() == 0))
			return 0;

		int highestEndLine = 0;

		for (Iterator iter = packages.iterator(); iter.hasNext();) {
			SpecfilePackage subPackage = (SpecfilePackage) iter.next();
			if (subPackage.getLineStartPosition() > highestEndLine)
				highestEndLine = subPackage.getLineEndPosition();
		}

		if (highestEndLine < 0)
			return 0;
		return highestEndLine;
	}

	public SpecfilePackage getPackage(String packageName) {
		for (Iterator iter = packages.iterator(); iter.hasNext();) {
			SpecfilePackage thisPackage = (SpecfilePackage) iter.next();
			if (thisPackage.getPackageName().equals(thisPackage.resolve(packageName))) {
				return thisPackage;
			}
		}
		return null;
	}

	public boolean contains(SpecfilePackage subPackage){
		return packages.contains(subPackage);
	}

	public boolean hasChildren() {
		if (packages != null && packages.size() > 0)
			return true;
		return false;
	}
}
