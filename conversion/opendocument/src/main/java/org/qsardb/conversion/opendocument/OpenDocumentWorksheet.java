/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.opendocument;

import org.qsardb.conversion.spreadsheet.*;

import org.jdom.*;
import org.jopendocument.dom.spreadsheet.*;

public class OpenDocumentWorksheet extends Worksheet {

	private Sheet sheet = null;

	private Dimension size = null;


	public OpenDocumentWorksheet(Sheet sheet){
		super(getName(sheet));

		this.sheet = sheet;
	}

	@Override
	public Dimension getSize(){

		if(this.size == null){
			this.size = calculateSize();
		}

		return this.size;
	}

	private Dimension calculateSize(){
		int rows = this.sheet.getRowCount();
		int columns = this.sheet.getColumnCount();

		rows:
		for(int row = rows - 1; row > -1; row--){

			for(int column = columns - 1; column > -1; column--){

				if(hasValueAt(row, column)){
					break rows;
				}
			}

			rows--;
		}

		columns:
		for(int column = columns - 1; column > -1; column--){

			for(int row = rows - 1; row > -1; row--){

				if(hasValueAt(row, column)){
					break columns;
				}
			}

			columns--;
		}

		return new Dimension(rows, columns);
	}

	@Override
	public String getValueAt(int row, int column){
		Cell<?> cell = this.sheet.getCellAt(column, row);

		Object value = cell.getValue();
		if(value != null){
			return String.valueOf(value);
		}

		return null;
	}

	private boolean hasValueAt(int row, int column){
		String value = getValueAt(row, column);

		return value != null && value.length() > 0;
	}

	static
	private String getName(Sheet sheet){
		Attribute attribute = (sheet.getElement()).getAttribute("name", (sheet.getSpreadSheet()).getNS().getTABLE());
		if(attribute == null){
			throw new NullPointerException();
		}

		return attribute.getValue();
	}
}