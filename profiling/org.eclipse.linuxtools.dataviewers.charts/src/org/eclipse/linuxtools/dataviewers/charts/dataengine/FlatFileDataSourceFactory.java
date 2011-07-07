/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.charts.dataengine;

public class FlatFileDataSourceFactory {
	public FlatFileDataSourceFactory(){
	}
	
	public String createHomeVariable(){
		return null;
	}
	
	public String createFileName(){
		return null;
		
	}
	
	public String createQuery(){
		return null;
		
	}
	
	public String[] createColumns(){
		return null;
	}
	
	public FlatFileDataSource createFlatFileDataSource(){
		return new FlatFileDataSource(createHomeVariable(),createFileName(),createQuery(),createColumns());
	}
	
	
}
