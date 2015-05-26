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
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel.MountType;

/**
 * @author xcoulon
 *
 */
public class DataVolumeModel extends BaseDatabindingModel
		implements Comparable<DataVolumeModel> {

	public static final String CONTAINER_PATH = "containerPath";

	public static final String MOUNT_TYPE = "mountType";

	public static final String MOUNT = "mount";

	public static final String HOST_PATH_MOUNT = "hostPathMount";

	public static final String READ_ONLY_VOLUME = "readOnly";

	public static final String CONTAINER_MOUNT = "containerMount";

	private String containerPath;

	private MountType mountType;

	private String mount;

	private String hostPathMount;

	private String containerMount;

	private boolean readOnly = false;

	public DataVolumeModel() {
	}

	public DataVolumeModel(final String containerPath) {
		this.containerPath = containerPath;
		this.mountType = MountType.NONE;
	}

	public DataVolumeModel(final DataVolumeModel selectedDataVolume) {
		this.containerPath = selectedDataVolume.getContainerPath();
		this.mountType = selectedDataVolume.getMountType();
		if (this.mountType != null) {
			switch (this.mountType) {
			case CONTAINER:
				this.containerMount = selectedDataVolume.getMount();
				break;
			case HOST_FILE_SYSTEM:
				this.hostPathMount = selectedDataVolume.getMount();
				this.readOnly = selectedDataVolume.isReadOnly();
				break;
			case NONE:
				break;
			}
		} else {
			this.mountType = MountType.NONE;
		}
	}

	public String getContainerPath() {
		return this.containerPath;
	}

	public void setContainerPath(final String containerPath) {
		firePropertyChange(CONTAINER_PATH, this.containerPath,
				this.containerPath = containerPath);
	}

	public String getMount() {
		return mount;
	}

	public void setMount(final String mount) {
		firePropertyChange(MOUNT, this.mount, this.mount = mount);
	}

	public MountType getMountType() {
		return mountType;
	}

	public void setMountType(final MountType mountType) {
		// ignore 'null' assignments that may come from the UpdateStrategy
		// in
		// the EditDataVolumePage when a radion button is unselected.
		if (mountType == null) {
			return;
		}
		firePropertyChange(MOUNT_TYPE, this.mountType,
				this.mountType = mountType);
		if (this.mountType == MountType.NONE) {
			setMount("");
		}

	}

	public String getHostPathMount() {
		return hostPathMount;
	}

	public void setHostPathMount(final String hostPathMount) {
		firePropertyChange(HOST_PATH_MOUNT, this.hostPathMount,
				this.hostPathMount = hostPathMount);
		if (this.mountType == MountType.HOST_FILE_SYSTEM) {
			setMount(this.hostPathMount);
		}
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
		firePropertyChange(READ_ONLY_VOLUME, this.readOnly,
				this.readOnly = readOnly);
	}

	public String getContainerMount() {
		return this.containerMount;
	}

	public void setContainerMount(final String containerMount) {
		firePropertyChange(CONTAINER_MOUNT, this.containerMount,
				this.containerMount = containerMount);
		if (this.mountType == MountType.CONTAINER) {
			setMount(this.containerMount);
		}
	}

	@Override
	public int compareTo(final DataVolumeModel other) {
		return this.getContainerPath().compareTo(other.getContainerPath());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((containerPath == null) ? 0 : containerPath.hashCode());
		result = prime * result + ((mount == null) ? 0 : mount.hashCode());
		result = prime * result
				+ ((mountType == null) ? 0 : mountType.hashCode());
		result = prime * result + (readOnly ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataVolumeModel other = (DataVolumeModel) obj;
		if (containerPath == null) {
			if (other.containerPath != null)
				return false;
		} else if (!containerPath.equals(other.containerPath))
			return false;
		if (mount == null) {
			if (other.mount != null)
				return false;
		} else if (!mount.equals(other.mount))
			return false;
		if (mountType != other.mountType)
			return false;
		if (readOnly != other.readOnly)
			return false;
		return true;
	}

}
