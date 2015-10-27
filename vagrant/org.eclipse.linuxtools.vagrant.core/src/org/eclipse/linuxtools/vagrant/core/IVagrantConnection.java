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

	String getName();

	void addVMListener(IVagrantVMListener listener);

	void removeVMListener(IVagrantVMListener listener);

	List<IVagrantVM> getVMs();

	List<IVagrantVM> getVMs(boolean force);

	boolean isVMsLoaded();

	void addToTrackedKeys(String key);

	void addBoxListener(IVagrantBoxListener listener);

	void removeBoxListener(IVagrantBoxListener listener);

	boolean isBoxesLoaded();

	List<IVagrantBox> getBoxes();

	List<IVagrantBox> getBoxes(boolean force);

	void init(File vagrantDir);

	Process up(File vagrantDir, String provider);

	void addBox(String name, String location) throws VagrantException, InterruptedException;

	void destroyVM(String id) throws VagrantException, InterruptedException;

	void haltVM(String id) throws VagrantException, InterruptedException;

	void startVM(String id) throws VagrantException, InterruptedException;

	void removeBox(String name) throws VagrantException, InterruptedException;
}
