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

import java.util.UUID;

import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel.MountType;

/**
 * @author xcoulon
 *
 */
public class DataVolumeModel extends BaseDatabindingModel
		implements Comparable<DataVolumeModel> {

	private static final String SEPARATOR = ":"; //$NON-NLS-1$

	public static final String CONTAINER_PATH = "containerPath"; //$NON-NLS-1$

	public static final String MOUNT_TYPE = "mountType"; //$NON-NLS-1$

	public static final String MOUNT = "mount"; //$NON-NLS-1$

	public static final String HOST_PATH_MOUNT = "hostPathMount"; //$NON-NLS-1$

	public static final String READ_ONLY_VOLUME = "readOnly"; //$NON-NLS-1$

	public static final String CONTAINER_MOUNT = "containerMount"; //$NON-NLS-1$

	public static final String SELECTED = "selected"; //$NON-NLS-1$

	private final String id = UUID.randomUUID().toString();

	private String containerPath;

	private MountType mountType;

	private String mount;

	private String hostPathMount;

	private String containerMount;

	private boolean readOnly = false;

	private boolean selected;

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

	/**
	 * Create a DataVolumeModel from a toString() output.
	 * 
	 * @param fromString
	 * @return DataVolumeModel
	 */
	public static DataVolumeModel createDataVolumeModel(
			final String fromString) {
		final DataVolumeModel model = new DataVolumeModel();
		final String[] items = fromString.split(SEPARATOR); // $NON-NLS-1$
		model.containerPath = items[0];
		model.mountType = MountType.valueOf(items[1]);
		switch (model.mountType) {
		case CONTAINER:
			model.setContainerMount(items[2]);
			model.setSelected(Boolean.valueOf(items[3]));
			break;
		case HOST_FILE_SYSTEM:
			model.setHostPathMount(items[2]);
			model.setReadOnly(Boolean.valueOf(items[3]));
			model.setSelected(Boolean.valueOf(items[4]));
			break;
		case NONE:
			model.setSelected(Boolean.valueOf(items[2]));
			break;
		}
		return model;
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

	public boolean getSelected() {
		return selected;
	}

	public void setSelected(final boolean selected) {
		firePropertyChange(SELECTED, this.selected, this.selected = selected);
	}

	@Override
	public int compareTo(final DataVolumeModel other) {
		return this.getContainerPath().compareTo(other.getContainerPath());
	}

	// FIXME we should have a dedicated method to serialize the bean
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(
				this.containerPath + SEPARATOR + getMountType() + SEPARATOR);
		switch (getMountType()) {
		case CONTAINER:
			buffer.append(getContainerMount());
			break;
		case HOST_FILE_SYSTEM:
			buffer.append(getHostPathMount() + SEPARATOR); // $NON-NLS-1$
			buffer.append(isReadOnly());
			break;
		case NONE:
			break;
		}
		buffer.append(SEPARATOR).append(this.selected);
		return buffer.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
