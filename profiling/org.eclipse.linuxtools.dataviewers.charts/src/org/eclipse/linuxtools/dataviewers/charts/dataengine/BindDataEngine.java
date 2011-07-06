/*******************************************************************************
 * Copyright (c) 2008 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.charts.dataengine;

import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.factory.IDataRowExpressionEvaluator;
import org.eclipse.birt.chart.factory.RunTimeContext;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.QueryImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.eclipse.birt.core.archive.IDocArchiveReader;
import org.eclipse.birt.core.archive.IDocArchiveWriter;
import org.eclipse.birt.core.data.Constants;
import org.eclipse.birt.core.data.ExpressionUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.data.aggregation.api.IBuildInAggregation;
import org.eclipse.birt.data.engine.api.DataEngine;
import org.eclipse.birt.data.engine.api.DataEngineContext;
import org.eclipse.birt.data.engine.api.IGroupDefinition;
import org.eclipse.birt.data.engine.api.IPreparedQuery;
import org.eclipse.birt.data.engine.api.IQueryResults;
import org.eclipse.birt.data.engine.api.querydefn.Binding;
import org.eclipse.birt.data.engine.api.querydefn.GroupDefinition;
import org.eclipse.birt.data.engine.api.querydefn.OdaDataSetDesign;
import org.eclipse.birt.data.engine.api.querydefn.OdaDataSourceDesign;
import org.eclipse.birt.data.engine.api.querydefn.QueryDefinition;
import org.eclipse.birt.data.engine.api.querydefn.ScriptExpression;
import org.eclipse.birt.data.engine.core.DataException;
import org.eclipse.linuxtools.dataviewers.charts.Activator;

import com.ibm.icu.util.ULocale;

/**
 * The example demonstrates how chart works with ODA/DtE(BIRT data engine) to
 * get grouped/aggregated data set, the ODA/DtE is responsible to execute data
 * query and grouping/aggregation, it returns a grouped data set to chart and
 * chart retrieve data without grouping/aggregation by chart-self.
 * <p>
 * In the example, it only uses flat file as data source, actually it supports
 * any JDBC data source, referring to {@link org.eclipse.birt.data.engine} to
 * get detail information.
 * 
 * @since 2.3
 */
public class BindDataEngine
{
	private String[] columnsName = null;
	private FlatFileDataSource ffds = null;
	
	/**
	 * Create runtime chart model and bind data.
	 * 
	 * @return
	 * @throws ChartException
	 */
	public final Chart createWorkingWithBIRTDataEngine(FlatFileDataSource ffds)
			throws ChartException
	{
		this.ffds = ffds;
		columnsName = ffds.getCOLUMNNAME();
		String[] expressions = new String[columnsName.length]; 
		expressions[0] = ExpressionUtil.createRowExpression( columnsName[0] );
		expressions[1] = ExpressionUtil.createRowExpression( columnsName[1] );

		ChartWithAxes cwaBar = createChartModel( expressions );

		cwaBar = bindData( cwaBar, expressions );

		return cwaBar;
	}

	/**
	 * Create chart model.
	 * 
	 * @param expressions
	 *            expressions are used to set category series and value series.
	 * @return
	 */
	private static ChartWithAxes createChartModel( String[] expressions )
	{
		ChartWithAxes cwaBar = ChartWithAxesImpl.create( );
		cwaBar.setType( "Bar Chart" ); 
		cwaBar.setSubType( "Side-by-side" ); 
		// Plot
		cwaBar.getBlock( ).setBackground( ColorDefinitionImpl.WHITE( ) );
		cwaBar.getBlock( ).getOutline( ).setVisible( true );
		Plot p = cwaBar.getPlot( );
		p.getClientArea( ).setBackground( ColorDefinitionImpl.create( 255,
				255,
				225 ) );

		// Title
		cwaBar.getTitle( )
				.getLabel( )
				.getCaption( )
				.setValue( "Working with BIRT Data Engine" ); 

		// Legend
		Legend lg = cwaBar.getLegend( );
		lg.setItemType( LegendItemType.CATEGORIES_LITERAL );

		// X-Axis
		Axis xAxisPrimary = cwaBar.getPrimaryBaseAxes( )[0];

		xAxisPrimary.setType( AxisType.TEXT_LITERAL );
		xAxisPrimary.getMajorGrid( ).setTickStyle( TickStyle.BELOW_LITERAL );
		xAxisPrimary.getOrigin( ).setType( IntersectionType.MIN_LITERAL );

		// Y-Axis
		Axis yAxisPrimary = cwaBar.getPrimaryOrthogonalAxis( xAxisPrimary );
		yAxisPrimary.getMajorGrid( ).setTickStyle( TickStyle.LEFT_LITERAL );
		yAxisPrimary.setType( AxisType.LINEAR_LITERAL );
		yAxisPrimary.getLabel( ).getCaption( ).getFont( ).setRotation( 90 );
		yAxisPrimary.getTitle( ).setVisible( true );
		yAxisPrimary.getTitle( ).getCaption( ).setValue( "Customer Amount" ); //$NON-NLS-1$
		yAxisPrimary.getTitle( )
				.getCaption( )
				.setColor( ColorDefinitionImpl.GREEN( ) );

		// X-Series
		Series seCategory = SeriesImpl.create( );
		// seCategory.setDataSet( categoryValues );

		// Set category expression.
		seCategory.getDataDefinition( )
				.add( QueryImpl.create( expressions[0] ) );

		SeriesDefinition sdX = SeriesDefinitionImpl.create( );
		sdX.getSeriesPalette( ).shift( 0 );

		xAxisPrimary.getSeriesDefinitions( ).add( sdX );
		sdX.getSeries( ).add( seCategory );

		// Y-Series
		BarSeries bs1 = (BarSeries) BarSeriesImpl.create( );
		bs1.getDataDefinition( ).add( QueryImpl.create( expressions[1] ) );
		bs1.getLabel( ).setVisible( true );
		bs1.setLabelPosition( Position.INSIDE_LITERAL );

		SeriesDefinition sdY = SeriesDefinitionImpl.create( );
		yAxisPrimary.getSeriesDefinitions( ).add( sdY );
		sdY.getSeries( ).add( bs1 );
		return cwaBar;
	}

	/**
	 * Binds data into chart model.
	 * 
	 * @param cwaBar
	 * @return
	 * @throws ChartException
	 */
	private ChartWithAxes bindData( ChartWithAxes cwaBar,
			String[] expressions ) throws ChartException
	{

		RunTimeContext context = new RunTimeContext( );
		context.setULocale( ULocale.getDefault( ) );

		IDataRowExpressionEvaluator evaluator;
		try
		{
			// Create row expression evaluator for chart doing data binding.
			evaluator = prepareRowExpressionEvaluator( cwaBar, expressions );
			
			// Binding data.
			Generator.instance( ).bindData( evaluator, cwaBar, context );
		}
		catch ( BirtException e )
		{
			throw new ChartException( Activator.PLUGIN_ID,
					ChartException.DATA_BINDING,
					e );
		}
		return cwaBar;
	}

	/**
	 * Uses BIRT data engine to do query and wraps data with
	 * <code>IDataRowExpressionEvaluator</code> for chart doing data binding.
	 * 
	 * @return
	 * @throws BirtException
	 */
	private IDataRowExpressionEvaluator prepareRowExpressionEvaluator(
			ChartWithAxes chart, String[] expressions ) throws BirtException
	{

		// Initialize data source and data set.
		OdaDataSourceDesign odaDataSource = newDataSource( );
		OdaDataSetDesign odaDataSet = newDataSet( odaDataSource );

		// Create query definition.
		QueryDefinition query = createQueryDefinition( odaDataSet, expressions );

		// Create data engine and execute query.
		DataEngine dataEngine = newDataEngine( );
		dataEngine.defineDataSource( odaDataSource );
		dataEngine.defineDataSet( odaDataSet );
		IPreparedQuery preparedQuery = dataEngine.prepare( query );

		IQueryResults queryResults = preparedQuery.execute( null );

		// Create row expression evaluator.
		return new GroupedRowExpressionsEvaluator( queryResults.getResultIterator( ),
				true );
	}

	/**
	 * Create query definition.
	 * 
	 * @param odaDataSet
	 * @param expressions
	 * @return
	 * @throws ChartException
	 */
	private QueryDefinition createQueryDefinition(
			OdaDataSetDesign odaDataSet, String[] expressions )
			throws ChartException
	{

		QueryDefinition queryDefn = new QueryDefinition( );
		queryDefn.setDataSetName( odaDataSet.getName( ) );

		try
		{
			initDefaultBindings( queryDefn );

			// Add group definitions and aggregation binding.
			String groupName = "Group_Country"; //$NON-NLS-1$
			GroupDefinition gd = new GroupDefinition( groupName );
			gd.setKeyExpression( expressions[0] );
			gd.setInterval( IGroupDefinition.NO_INTERVAL );
			gd.setIntervalRange( 0 );

			// Add expression bindings.
			for ( int i = 0; i < expressions.length; i++ )
			{
				String expr = (String) expressions[i];
				Binding colBinding = new Binding( expr );
				colBinding.setExpression( new ScriptExpression( expr ) );
				if ( i == 1 )
				{
					colBinding.setExpression( null );
					colBinding.setAggrFunction( IBuildInAggregation.TOTAL_COUNT_FUNC );
					colBinding.addAggregateOn( groupName );
					colBinding.addArgument( new ScriptExpression( expressions[i] ) );
				}

				queryDefn.addBinding( colBinding );
			}

			queryDefn.addGroup( gd );
		}
		catch ( DataException e )
		{
			throw new ChartException( Activator.PLUGIN_ID,
					ChartException.DATA_BINDING,
					e );
		}

		return queryDefn;
	}

	/**
	 * Initialize default column bindings for original columns.
	 * 
	 * @param queryDefn
	 * @throws DataException
	 */
	private  void initDefaultBindings( QueryDefinition queryDefn )
			throws DataException
	{

		for(int i=0;i<columnsName.length;i++){
			Binding colBinding = new Binding( columnsName[i] );
			colBinding.setExpression( new ScriptExpression( ExpressionUtil.createDataSetRowExpression( columnsName[i]) ) );
			queryDefn.addBinding( colBinding );
		}
	}

	/**
	 * Create a new data engine.
	 * 
	 * @return
	 * @throws BirtException
	 */
	private static DataEngine newDataEngine( ) throws BirtException
	{
		DataEngineContext context = DataEngineContext.newInstance( DataEngineContext.DIRECT_PRESENTATION,null,(IDocArchiveReader) null,(IDocArchiveWriter) null);

		// context.setTmpdir( this.getTempDir( ) );
		DataEngine myDataEngine = DataEngine.newDataEngine( context );
		return myDataEngine;
	}

	/**
	 * Create a new data set.
	 * 
	 * @param dataSourceDesign
	 * @return
	 */
	private OdaDataSetDesign newDataSet(
			OdaDataSourceDesign dataSourceDesign )
	{
		OdaDataSetDesign dataSet = new OdaDataSetDesign( "Data Set1" ); //$NON-NLS-1$

		dataSet.setDataSource( dataSourceDesign.getName( ) );
		dataSet.setExtensionID( FlatFileDataSource.DATA_SET_TYPE );
		dataSet.setQueryText( ffds.getQuery() );
		return dataSet;
	}

	/**
	 * Create a new data source.
	 * 
	 * @return
	 * @throws BirtException
	 */
	private OdaDataSourceDesign newDataSource( ) throws BirtException
	{
		OdaDataSourceDesign dataSource = new OdaDataSourceDesign( "Data Source1" ); //$NON-NLS-1$
		dataSource.setExtensionID( FlatFileDataSource.DATA_SOURCE_TYPE );
		dataSource.addPrivateProperty( "HOME", ffds.HOME ); //$NON-NLS-1$
		dataSource.addPrivateProperty( "CHARSET", FlatFileDataSource.CHARSET ); //$NON-NLS-1$
		dataSource.addPrivateProperty( "DELIMTYPE", //$NON-NLS-1$
				FlatFileDataSource.DELIMTYPE );
		dataSource.addPrivateProperty( "INCLTYPELINE", //$NON-NLS-1$
				FlatFileDataSource.INCLTYPELINE );
		dataSource.addPrivateProperty( Constants.ODA_PROP_CONFIGURATION_ID,
				dataSource.getExtensionID( )
						+ Constants.ODA_PROP_CONFIG_KEY_SEPARATOR
						+ dataSource.getName( ) );

		return dataSource;
	}
	

}
