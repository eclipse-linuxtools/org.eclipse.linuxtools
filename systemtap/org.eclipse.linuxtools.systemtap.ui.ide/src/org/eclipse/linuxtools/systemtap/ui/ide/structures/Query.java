package org.eclipse.linuxtools.systemtap.ui.ide.structures;

import java.util.StringTokenizer;

public final class Query {

	public String tableName;
	public String where;
	public String groupBy;
	public String orderBy;
	public String[] columnNames;
	public String[] newColumnNames;

	/**
	 * This constructor is only used internally to set the various fields
	 * to null.
	 */
	private Query() {
		this.where = null;
		this.groupBy = null;
		this.orderBy = null;
		this.newColumnNames = null;
	}
	
	public Query(final String tableName, final String[] columnNames) {
		this();
		this.tableName = tableName;
		this.columnNames = columnNames;
	}

	public Query(final Query query) {
		tableName = query.tableName;
		columnNames = query.columnNames;
		newColumnNames = query.newColumnNames;
		where = query.where;
		groupBy = query.groupBy;
		orderBy = query.orderBy;
	}

	public Query(final String tableName) {
		this();
		this.tableName = tableName;
		this.columnNames = null;
	}

	public Query(final String tableName, final String[] columnNames,
			final String[] newColumnNames, final String where,
			final String groupBy, final String orderBy) {
		this.tableName = tableName;
		this.columnNames = columnNames;
		this.newColumnNames = newColumnNames;
		this.where = where;
		this.groupBy = groupBy;
		this.orderBy = orderBy;
	}

	@Override
	public String toString() {
		return buildQuery(tableName, columnNames, newColumnNames, where,
				groupBy, orderBy);
	}

	private String buildQuery(final String tableName, final String[] fields,
			final String[] columnNames, final String whereClauses,
			final String groupBy, final String orderBy) {
		String query = "SELECT ";
		if (fields == (null))
			query += "* ";
		else
			for (int i = 0; i < fields.length; i++) {
				query += fields[i];
				if ((columnNames != null)
						&& (columnNames.length == fields.length))
					query += " AS " + columnNames[i];
				if (i != (fields.length - 1))
					query += ", ";
			}
		query += " FROM " + tableName;
		if (whereClauses != null)
			query += " WHERE " + whereClauses;
		if (groupBy != null)
			query += " GROUP BY " + groupBy;
		if (orderBy != null)
			query += " ORDER BY " + orderBy;
		return query;
	}

	/**
	 * Turns the query into a string that can be written to a file and easily
	 * recreated as a Query object.
	 * 
	 * @return
	 */
	public String toSavableQueryString() {
		String query = tableName + ":: COLS ";
		if (columnNames != null)
			for (final String element : columnNames)
				query += element + " ";
		else
			query += "*";

		query += ":: NEW_COLS ";
		if (columnNames != null)
			for (final String element : newColumnNames)
				query += element + " ";
		query += ":: WHERE " + where;
		query += ":: GROUP " + groupBy;
		query += ":: ORDER " + orderBy;
		return query;
	}

	/**
	 * Creates a Query object from a string created by toSavableQueryString()
	 * 
	 * @param query
	 *            The query string to be turned into a Query object.
	 * @return a new Query object created by the given string.
	 */
	public static Query getQueryFromString(final String query) {
		if (query == null)
			return null;
		final String[] subQuery = query.split("::");
		final Query tempQuery = new Query(subQuery[0]);
		for (int i = 1; i < subQuery.length; i++) {
			subQuery[i] = subQuery[i].trim();
			if (subQuery[i].startsWith("COLS ")) {
				String temp = subQuery[i].replace("COLS ", "");
				temp = temp.trim();
				final StringTokenizer tok = new StringTokenizer(temp);
				final String next = tok.nextToken();
				if (next.equals("null") || next.equals("*"))
					tempQuery.columnNames = null;
				else {
					tempQuery.columnNames = new String[tok.countTokens() + 1];
					tempQuery.columnNames[0] = next;
					for (int j = 1; j < tempQuery.columnNames.length; j++)
						tempQuery.columnNames[j] = tok.nextToken();
				}
			} else if (subQuery[i].startsWith("NEW_COLS ")) {
				String temp = subQuery[i].replace("NEW_COLS ", "");
				temp = temp.trim();
				final StringTokenizer tok = new StringTokenizer(temp);
				final String next = tok.nextToken();
				if (next.equals("null"))
					tempQuery.newColumnNames = null;
				else {
					tempQuery.newColumnNames = new String[tok.countTokens() + 1];
					tempQuery.newColumnNames[0] = next;
					for (int j = 1; j < tempQuery.newColumnNames.length; j++)
						tempQuery.newColumnNames[j] = tok.nextToken();
				}
			} else if (subQuery[i].startsWith("WHERE ")) {
				String temp = subQuery[i].replace("WHERE ", "");
				temp = temp.trim();
				if (temp.equals("null"))
					tempQuery.where = null;
				else
					tempQuery.where = temp;
			} else if (subQuery[i].startsWith("GROUP ")) {
				String temp = subQuery[i].replace("GROUP ", "");
				temp = temp.trim();
				if (temp.equals("null"))
					tempQuery.groupBy = null;
				else
					tempQuery.groupBy = temp;
			} else if (subQuery[i].startsWith("ORDER ")) {
				String temp = subQuery[i].replace("ORDER ", "");
				temp = temp.trim();
				if (temp.equals("null"))
					tempQuery.orderBy = null;
				else
					tempQuery.orderBy = temp;
			}
		}
		return tempQuery;
	}
}
