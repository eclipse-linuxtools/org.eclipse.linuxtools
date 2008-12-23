package org.eclipse.linuxtools.valgrind.massif.tests;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.linuxtools.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.valgrind.ui.ValgrindViewPart;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

public class BasicMassifTest extends AbstractMassifTest {
	
	@Override
	protected void setUp() throws Exception {
		proj = createProject("alloctest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
	}
	
	public void testEditorName() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		config.launch(ILaunchManager.PROFILE_MODE, null, true);
				
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IAction chartAction = getChartAction(view);
		assertNotNull(chartAction);
		chartAction.run();
		
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		assertEquals("Heap Chart - alloctest", part.getTitle()); //$NON-NLS-1$
	}
	
	private IAction getChartAction(IViewPart view) {
		IAction result = null;
		IToolBarManager manager = view.getViewSite().getActionBars().getToolBarManager();
		for (IContributionItem item : manager.getItems()) {
			if (item instanceof ActionContributionItem) {
				ActionContributionItem actionItem = (ActionContributionItem) item;
				if (actionItem.getAction().getId().equals(MassifViewPart.CHART_ACTION)) {
					result = actionItem.getAction();
				}
			}
		}
		return result;
	}
}
