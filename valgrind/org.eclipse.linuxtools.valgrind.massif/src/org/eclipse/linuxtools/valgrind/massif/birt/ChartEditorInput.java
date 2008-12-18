package org.eclipse.linuxtools.valgrind.massif.birt;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.valgrind.massif.MassifPlugin;
import org.eclipse.linuxtools.valgrind.massif.MassifSnapshot;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class ChartEditorInput implements IEditorInput {
	
	protected Chart chart;
	protected MassifSnapshot[] snapshots;
	protected String name;

	public ChartEditorInput(Chart chart, MassifSnapshot[] snapshots) {
		this.chart = chart;
		this.snapshots = snapshots;
		// Just show the program name
		String cmd = snapshots[0].getCmd();
		IPath path = new Path(cmd);
		name = path.lastSegment();
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return MassifPlugin.imageDescriptorFromPlugin(MassifPlugin.PLUGIN_ID, "icons/linecharticon.gif"); //$NON-NLS-1$
	}

	public String getName() {		
		return name;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return NLS.bind(Messages.getString("ChartEditorInput.Heap_allocation_chart_for"), name); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public Chart getChart() {
		return chart;
	}
	
	public MassifSnapshot[] getSnapshots() {
		return snapshots;
	}

}
