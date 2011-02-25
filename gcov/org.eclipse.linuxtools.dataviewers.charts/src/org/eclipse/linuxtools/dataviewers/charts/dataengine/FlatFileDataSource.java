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

/**
 * Resource of flat file data.
 */
public class FlatFileDataSource {
		static final String DATA_SOURCE_TYPE = "org.eclipse.datatools.connectivity.oda.flatfile"; 
		static final String DATA_SET_TYPE = "org.eclipse.datatools.connectivity.oda.flatfile.dataSet"; 
		static final String CHARSET = "UTF-8"; 
		static final String DELIMTYPE = "TAB"; 
		static final String INCLTYPELINE = "NO"; 
		protected String HOME = null;
	
		protected String dataFile = "";
		protected String query = ""; 
		protected String[] columnsName = null;
		
		public FlatFileDataSource(){
		}
				
		public FlatFileDataSource(String HOME,String dataFile,String query,String[] columns){
			this.HOME = HOME;
			this.dataFile = dataFile;
			this.query = query;
			this.columnsName = columns;
		}
		
		protected void setHOME_VARIABLE(String home){
			HOME = home;
		}
		
		protected String getHOME(){
			return HOME;
		}
		
		protected void setDataFile(String fileName){
			dataFile = fileName;
		}
		
		protected String getDataFile(){
			return dataFile; 
		}
		
		protected void setQuery(String query){
			this.query = query;
		}
		
		protected String getQuery(){
			return query;
		}
		
		protected String[] getCOLUMNNAME(){
			return columnsName;
		}
		
		protected void setCOLUMNName(String[] cn){
			columnsName = cn;
		}

}
