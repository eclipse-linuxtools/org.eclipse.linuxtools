/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.vagrant.core;

import java.io.File;
import java.util.List;

public interface IVagrantConnection {

	public String getName();

	public void addVMListener(IVagrantVMListener listener);

	public void removeVMListener(IVagrantVMListener listener);

	public List<IVagrantVM> getVMs();

	public List<IVagrantVM> getVMs(boolean force);

	public boolean isVMsLoaded();

	public void addToTrackedKeys(String key);

	public void addBoxListener(IVagrantBoxListener listener);

	public void removeBoxListener(IVagrantBoxListener listener);

	public boolean isBoxesLoaded();

	public List<IVagrantBox> getBoxes();

	public List<IVagrantBox> getBoxes(boolean force);

	public void init(File vagrantDir);

	public Process up(File vagrantDir);

	void addBox(String name, String location) throws VagrantException, InterruptedException;

	void destroyVM(String id) throws VagrantException, InterruptedException;

	void haltVM(String id) throws VagrantException, InterruptedException;

	void startVM(String id) throws VagrantException, InterruptedException;

	void removeBox(String name) throws VagrantException, InterruptedException;
}
