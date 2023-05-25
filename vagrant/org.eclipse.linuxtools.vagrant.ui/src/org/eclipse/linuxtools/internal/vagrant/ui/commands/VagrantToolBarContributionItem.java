/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.vagrant.ui.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.vagrant.ui.Activator;
import org.eclipse.linuxtools.internal.vagrant.ui.SWTImagesFactory;
import org.eclipse.linuxtools.vagrant.core.EnumVMStatus;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.IVagrantVMListener;
import org.eclipse.linuxtools.vagrant.core.VagrantService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

public class VagrantToolBarContributionItem extends ContributionItem
		implements IVagrantVMListener {

	private List<IVagrantVM> vms = new ArrayList<>();

	public VagrantToolBarContributionItem() {
		IVagrantConnection conn = VagrantService.getInstance();
		conn.addVMListener(this);
		Thread t = new Thread(() -> {
			vms = conn.getVMs();
		});
		t.start();
	}

	public VagrantToolBarContributionItem(String id) {
		super(id);
	}

	@Override
	public void fill(Menu menu, int index) {
		// The menu passed in doesn't allow us to have sub-menus
		// Ignore it, get the parent IMenuManager and create the structure
		if (getParent() instanceof IMenuManager mm) {
			IContributionItem v = mm.find(getId());
			// Menu manager contributions get aggregated so remove them first
			mm.removeAll();
			// dynamic menu contribution ensures fill() gets called
			mm.add(v);
			for (IVagrantVM vm : vms) {
				EnumVMStatus containerStatus = EnumVMStatus.fromStatusMessage(vm.state());
				ImageDescriptor img = (containerStatus == EnumVMStatus.RUNNING)
						? SWTImagesFactory.DESC_CONTAINER_STARTED
						: SWTImagesFactory.DESC_CONTAINER_STOPPED;
				IMenuManager vmM = new MenuManager(vm.name(), img, null);
				for (IAction act : getApplicableActions(vm)) {
					vmM.add(act);
				}
				mm.add(vmM);
			}
		}
	}

	private List<IAction> getApplicableActions(IVagrantVM vm) {
		final EnumVMStatus containerStatus = EnumVMStatus.fromStatusMessage(vm.state());
		List<IAction> list = new ArrayList<>();
		if (containerStatus == EnumVMStatus.RUNNING) {
			list.add(createAction(Messages.VagrantToolBarContributionItem_stop,
					"org.eclipse.linuxtools.vagrant.ui.commands.stopVM", //$NON-NLS-1$
					SWTImagesFactory.DESC_STOP, vm));
			list.add(createAction(Messages.VagrantToolBarContributionItem_destroy,
					"org.eclipse.linuxtools.vagrant.ui.commands.destroyVM", //$NON-NLS-1$
					SWTImagesFactory.DESC_REMOVE, vm));
			list.add(createAction(Messages.VagrantToolBarContributionItem_ssh,
					"org.eclipse.linuxtools.vagrant.ui.commands.sshVM", //$NON-NLS-1$
					SWTImagesFactory.DESC_CONSOLE, vm));
		} else {
			list.add(createAction(Messages.VagrantToolBarContributionItem_start,
					"org.eclipse.linuxtools.vagrant.ui.commands.startVM", //$NON-NLS-1$
					SWTImagesFactory.DESC_START, vm));
		}
		list.add(createAction(Messages.VagrantToolBarContributionItem_package,
				"org.eclipse.linuxtools.vagrant.ui.commands.packageVM", //$NON-NLS-1$
				SWTImagesFactory.DESC_BUILD, vm));
		list.add(createAction(Messages.VagrantToolBarContributionItem_open,
				"org.eclipse.linuxtools.vagrant.ui.commands.openVFile", //$NON-NLS-1$
				SWTImagesFactory.DESC_FILE, vm));
		return list;
	}

	private IAction createAction(String label, String id, ImageDescriptor img, IVagrantVM vm) {
		return new Action(label, img) {
			@Override
			public void run() {
				execute(id, new StructuredSelection(vm));
			}
		};
	}

	private void execute(String id, IStructuredSelection selection) {
		ICommandService service = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service != null ? service.getCommand(id) : null;
		if (command != null && command.isDefined()) {
			try {
				ParameterizedCommand pCmd = ParameterizedCommand.generateCommand(command, null);
				IHandlerService handlerSvc = PlatformUI.getWorkbench().getService(IHandlerService.class);
				IEvaluationContext ctx = handlerSvc.getCurrentState();
				ctx = new EvaluationContext(ctx, selection);
				ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
				handlerSvc.executeCommandInContext(pCmd, null, ctx);
			} catch (Exception e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	@Override
	public void listChanged(IVagrantConnection connection,
			List<IVagrantVM> list) {
		this.vms = list;
		// Need to call update(false) so menu manager is up to date
		if (getParent() instanceof IMenuManager mm) {
			Display.getDefault().asyncExec(() -> mm.update(false));
		}
	}
}
