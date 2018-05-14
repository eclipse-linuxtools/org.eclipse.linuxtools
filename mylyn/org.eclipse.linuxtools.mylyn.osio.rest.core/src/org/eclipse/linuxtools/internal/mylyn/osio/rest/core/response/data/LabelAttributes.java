/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc. and others.
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

import com.google.gson.annotations.SerializedName;

public class LabelAttributes implements Named {
	
	private String name;
	
	@SerializedName("created-at")
	private String created_at;
	
	@SerializedName("updated-at")
	private String updated_at;
	
	private String version;
	
	@SerializedName("text-color")
	private String text_color;
	
	@SerializedName("background-color")
	private String background_color;
	
	@SerializedName("border-color")
	private String border_color;
	
	// for testing purposes only
	public LabelAttributes (String name, String created_at, String updated_at,
			String version, String text_color, String background_color,
			String border_color) {
		this.name = name;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.version = version;
		this.text_color = text_color;
		this.background_color = background_color;
		this.border_color = border_color;
	}
	
	public String getName() {
		return name;
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

	public String getVersion() {
		return version;
	}
	
	public String getBackgroundColor() {
		return background_color;
	}
	
	public String getTextColor() {
		return text_color;
	}
	
	public String getBorderColor() {
		return border_color;
	}
	
}
