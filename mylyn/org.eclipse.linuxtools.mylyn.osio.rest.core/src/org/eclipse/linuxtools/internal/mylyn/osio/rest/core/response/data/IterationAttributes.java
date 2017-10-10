/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class IterationAttributes implements Named {
	
	private String name;
	
	private String description;
	
	@SerializedName("created-at")
	private String created_at;
	
	@SerializedName("updated-at")
	private String updated_at;
	
	private String startAt;
	
	private String endAt;
	
	private String state;
	
	private boolean user_active;
	
	private boolean active_status;
	
	private String parent_path;
	
	private String resolved_parent_path;
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Date getCreatedAt() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss"); //$NON-NLS-1$
		Date d = null;
		try {
			d = sdf.parse(created_at.replace("Z", "").replace("T", "-")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	public Date getUpdatedAt() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss"); //$NON-NLS-1$
		Date d = null;
		try {
			d = sdf.parse(updated_at.replace("Z", "").replace("T", "-")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	public Date getStartAt() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSSSSS"); //$NON-NLS-1$
		Date d = null;
		try {
			d = sdf.parse(startAt.replace("Z", "").replace("T", "-")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	public Date getEndAt() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSSSSS"); //$NON-NLS-1$
		Date d = null;
		try {
			d = sdf.parse(endAt.replace("Z", "").replace("T", "-")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	public String getState() {
		return state;
	}
	
	public boolean isUserActive() {
		return user_active;
	}

	public boolean isActiveStatus() {
		return active_status;
	}
	
	public String getParentPath() {
		return parent_path;
	}
	
	public String getResolvedParentPath() {
		return resolved_parent_path;
	}
	
}
