package org.eclipse.linuxtools.valgrind.massif.tests;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.linuxtools.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;

public class BasicMassifTest extends AbstractMassifTest {
	
	@Override
	protected void setUp() throws Exception {
		proj = createProject("alloctest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
	}
	
	public void testSnapshots() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		config.launch(ILaunchManager.PROFILE_MODE, null, true);
				
		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		assertEquals(11, view.getSnapshots().length);
	}
}
