package org.eclipse.linuxtools.valgrind.cachegrind.tests;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.cachegrind.CachegrindViewPart;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;

public class BasicCachegrindTest extends AbstractCachegrindTest {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProject("cpptest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(proj);
	}
	
	public void testNumPIDs() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testNumPIDs"); //$NON-NLS-1$
		
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		assertEquals(1, view.getOutputs().length);
	}
	
	public void testFileNames() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testNumFiles"); //$NON-NLS-1$
		
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		CachegrindOutput output = view.getOutputs()[0];
		CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		assertNotNull(file);
		file = getFileByName(output, "cpptest.h"); //$NON-NLS-1$
		assertNotNull(file);
	}
	
	public void testNumFunctions() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		doLaunch(config, "testNumFunctions"); //$NON-NLS-1$
		
		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		CachegrindOutput output = view.getOutputs()[0];
		CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		assertNotNull(file);
		assertEquals(8, file.getFunctions().length);
	}

	private CachegrindFile getFileByName(CachegrindOutput output, String name) {
		CachegrindFile file = null;
		for (CachegrindFile f : output.getFiles()) {
			if (f.getName().equals(name)) {
				file = f;
			}
		}
		return file;
	}
}
