package org.eclipse.linuxtools.valgrind.massif;

import java.util.HashMap;
import java.util.Map;

public class MassifOutput {
	protected Map<Integer, MassifSnapshot[]> pidMap;
	
	public MassifOutput() {
		pidMap = new HashMap<Integer, MassifSnapshot[]>();
	}
	
	public void putSnapshots(Integer pid, MassifSnapshot[] snapshots) {
		pidMap.put(pid, snapshots);
	}
	
	public MassifSnapshot[] getSnapshots(Integer pid) {
		return pidMap.get(pid);
	}
	
	public Integer[] getPids() {
		return pidMap.keySet().toArray(new Integer[pidMap.size()]);
	}
}
