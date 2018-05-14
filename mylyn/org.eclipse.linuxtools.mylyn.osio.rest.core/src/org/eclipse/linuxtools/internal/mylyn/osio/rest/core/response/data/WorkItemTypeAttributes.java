/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat.
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
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class WorkItemTypeAttributes {
	
	private String name;
	
	private String description;
	
	private int version;
	
	private String extendedTypeName;
	
	@SerializedName("created-at")
	private String created_at;
	
	@SerializedName("updated-at")
	private String updated_at;
	
	private Map<String, WorkItemTypeField> fields;
	
	private String icon;
	
	// for testing purposes only
	public WorkItemTypeAttributes (String name, String description, int version,
			String extendedTypeName, String created_at, String updated_at,
			Map<String, WorkItemTypeField> fields) {
		this.name = name;
		this.description = description;
		this.version = version;
		this.extendedTypeName = extendedTypeName;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.fields = fields;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getVersion() {
		return version;
	}
	
	public String getExtendedTypeName() {
		return extendedTypeName;
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

	public Map<String, WorkItemTypeField> getFields() {
		return fields;
	}
	
	public String getIcon() {
		return icon;
	}
	
}
