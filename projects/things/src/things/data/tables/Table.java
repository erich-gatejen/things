/**
 * THINGS/THINGER 2004
 * Copyright Erich P Gatejen (c) 2004, 2005  ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.data.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import things.common.ThingsException;
import things.common.ThingsNamespace;

/**
 * A table of Cell types.
 * <br>
 * NOT THREAD SAFE!!!
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Rewrite from another project - 18 APR 04
 * EPG - Converted to use generics - 13 MAY 06
 * </pre> 
 */
public class Table<C> implements Iterable<ArrayList<C>>{

	final static long serialVersionUID = 1;
	
	/**
	 * The list of rows.
	 */
	private ArrayList<ArrayList<C>> rows;	
	
	/**
	 * Header labels.
	 */
	private ArrayList<String> headers;	
	
	/**
	 * Known number of rows and columns.
	 */
	private int		numberRows;
	private int		numberColumns;
	
	/**
	 *  Default constructor.
	 */
	public Table() {
		rows = new ArrayList<ArrayList<C>>();
		numberRows = 0;
		numberColumns = 0;
	}
	
	/**
	 * Get the number of rows.
	 * @return the number of rows.
	 */
	public int getNumberRows() {
		return numberRows;
	}
	
	/**
	 * Get the number of columns.
	 * @return the number of columns.
	 */
	public int getNumberColumns() {
		return numberColumns;
	}
	
	/**
	 * Append a row.
	 * @param items Items in the row.
	 */
	public void append(C... items) {
		
		// Append it
		ArrayList<C> newRow = new ArrayList<C>(Arrays.asList(items));
		rows.add(newRow);
		
		// Fix statistics
		if (items.length > numberColumns) numberColumns = items.length;
		numberRows++;
	}
	
	/**
	 * Set headers.
	 * @param items Names for each header.
	 */
	public void setHeaders(String... items) {
		
		// Create and set it.
		headers = new ArrayList<String>(Arrays.asList(items));
	}
	
	/**
	 * Get a row at the specified index as an array.  The count starts from 0.
	 * It will throw a ThingsException if it is out of bounds.
	 * @param index The index of the row.
	 * @return The row as an array.
	 */
	@SuppressWarnings("unchecked")
	public C[] getRowArray(int	index) throws ThingsException {
		
		// Validate it
		if (index >= numberRows) throw new ThingsException("Row index out of bounds.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS,
				ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(index), ThingsNamespace.ATTR_DATA_INDEX_BOUNDS, Integer.toString(numberRows));

		// Get it
		ArrayList<C> theRow = rows.get(index);
		return (C[])theRow.toArray();
	}
	
	/**
	 * Get an iterator for the rows.
	 * @return The an iterator for the rows.
	 */
	public Iterator<ArrayList<C>> iterator() {
		return rows.iterator();
	}
	
	/**
	 * Get a row at the specified index.  The count starts from 0.
	 * It will throw a ThingsException if it is out of bounds.
	 * @param index The index of the row.
	 * @return The row as an ArrayList.
	 */
	public ArrayList<C> getRow(int	index) throws ThingsException {
		
		// Validate it
		if (index >= numberRows) throw new ThingsException("Row index out of bounds.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS,
				ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(index), ThingsNamespace.ATTR_DATA_INDEX_BOUNDS, Integer.toString(numberRows));

		// Get it
		return rows.get(index);
	}
	
	/**
	 * Get the headers.
	 * @return the headers as an ArrayList.
	 */
	public ArrayList<String> getHeaders() throws ThingsException {
		
		// Get it
		return headers;
	}
	
	/**
	 * Get the headers as an array.
	 * @return the headers as an Array.
	 */
	public String[] getHeadersArray() throws ThingsException {
		
		return headers.toArray(new String[headers.size()]);
	}
	
	/**
	 * Get a cell at the specified index.  The count starts from 0.  Empty cells will be returned as a null.
	 * It will throw a ThingsException if it is out of bounds.
	 * @param row The row index starting from 0.
	 * @param column The column index starting from 0.
	 * @return the object at the cell or a null if it is empty.
	 * @throws things.common.ThingsException
	 */
	public C getCell(int	row, int 	column) throws ThingsException {
		
		// Validate it
		if (row+1 > numberRows) throw new ThingsException("Row index out of bounds.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS,
				ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(row), ThingsNamespace.ATTR_DATA_INDEX_BOUNDS, Integer.toString(numberRows));
		// Validate it
		if (column+1 > numberColumns) throw new ThingsException("Column index out of bounds.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS,
				ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(column), ThingsNamespace.ATTR_DATA_INDEX_BOUNDS, Integer.toString(numberColumns));

		// find it
		ArrayList<C> theRow = rows.get(row);
		if (column+1 > theRow.size()) return null;
		return theRow.get(column);
	}
	
	/**
	 * Put a cell at the specified index.  The count starts from 0.  It will expand the rows and columns as neccesary to put the cell.
	 * If the cell is already there, it will replace it.  Techincally, only an OutOfMemory or a negative index will cause an exception in this method.
	 * @param row The row index starting from 0.
	 * @param column The column index starting from 0.
	 * @param item The item to put in the cell.
	 */
	public void putCell(int	row, int 	column, C item) throws ThingsException {
		
		// Validate it
		if (row < 0) throw new ThingsException("A negative row index is out of bounds.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS,
				ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(row), ThingsNamespace.ATTR_DATA_INDEX_BOUNDS, Integer.toString(numberRows));
		// Validate it
		if (column < 0) throw new ThingsException("A negative column index is out of bounds.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS,
				ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(column), ThingsNamespace.ATTR_DATA_INDEX_BOUNDS, Integer.toString(numberColumns));

		ArrayList<C> targetRow = null;
		
		// do we need to expand the rows?
		if (row+1 > numberRows) {
			
			// Yes, so expand the table.
			for (int rover = (numberRows-1); rover < row; rover++) {
				targetRow = new ArrayList<C>();
				rows.add(targetRow);
			}
			numberRows = row+1;
			
		} else {
			
			// No need.  Get the row that is already there.
			targetRow = rows.get(row);
		}
		
		// Do we need to expand columns?
		if (column+1 > targetRow.size()) {
			
			// Yes, so expand the row.  Add the one we will set too (thus the less-than-or-equal-to).  
			for (int rover = (targetRow.size()-1); rover <= column; rover++) {
				targetRow.add(null);
			}
			
			// Is this the biggest column?
			if (column+1 > numberColumns) {
				numberColumns = column+1;
			}
			
		}
			
		// Set it
		targetRow.set(column, item);	
	}
	
	
	
}
