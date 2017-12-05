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

public class WorkItemLinkTypeAttributes implements Named {
	
	private String name;
	
	private String description;
	
	private int version;
	
	// for testing purposes only
	public WorkItemLinkTypeAttributes (String name, String description, int version, String created_at, String updated_at,
			String forward_name, String reverse_name, String topology) {
		this.name = name;
		this.description = description;
		this.version = version;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.forward_name = forward_name;
		this.reverse_name = reverse_name;
		this.topology = topology;
	}
	
	@SerializedName("created-at")
	private String created_at;
	
	@SerializedName("updated-at")
	private String updated_at;
	
	private String forward_name;
	
	private String reverse_name;
	
	private String topology;
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getVersion() {
		return version;
	}
	
	public String getCreated_at() {
		return created_at;
	}
	
	public String getUpdated_at() {
		return updated_at;
	}
	
	public Date getCreatedAt() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSSSSS"); //$NON-NLS-1$
		Date d = null;
		try {
			d = sdf.parse(created_at.replace("Z", "").replace("T", "-")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	public Date getUpdatedAt() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSSSSS"); //$NON-NLS-1$
		Date d = null;
		try {
			d = sdf.parse(updated_at.replace("Z", "").replace("T", "-")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	public String getForwardName() {
		return forward_name;
	}
	
	public String getReverseName() {
		return reverse_name;
	}
	
	public String getTopology() {
		return topology;
	}
	
}
