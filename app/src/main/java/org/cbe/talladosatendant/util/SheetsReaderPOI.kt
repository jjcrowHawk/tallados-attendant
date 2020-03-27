package org.cbe.talladosatendant.util

import android.util.Log
import androidx.core.text.isDigitsOnly
import com.google.api.services.sheets.v4.model.CellData
import com.microsoft.schemas.office.visio.x2012.main.impl.CellTypeImpl
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.cbe.talladosatendant.pojo.CellIndex
import java.io.FileInputStream
import java.util.*

class SheetsReaderPOI(filepath: String) : SheetsReader() {

  private val work_book: Workbook


  init {
	 val inputStream = FileInputStream(filepath)
	 //Instantiate Excel workbook using existing file:
	 work_book = WorkbookFactory.create(inputStream)
  }

  override fun getNumberOfSheets(): Int {
	 return work_book.numberOfSheets
  }

  override fun getSheetsTitles(): List<String> {
	 val names= mutableListOf<String>()
	 for(i in 0 until work_book.numberOfSheets){
		names.add(work_book.getSheetName(i))
	 }
	 return names
  }

  override fun getNumberOfRows(sheet: Int) : Int{
	 return work_book.getSheetAt(0).lastRowNum - work_book.getSheetAt(0).firstRowNum
  }

  override fun readCellValue(sheet:Int,row: Int, col: Int): Any? {
	 if(row < 0 || col < 0) return null
	 val sheet: Sheet = work_book.getSheetAt(sheet) ?: return null
	 val row= sheet.getRow(row) ?: return null
	 val cell= row.getCell(col) ?: return null



	 when(cell.cellTypeEnum){
		CellType.NUMERIC -> return if(HSSFDateUtil.isCellDateFormatted(cell)) Utils.getFormatedDate(cell.dateCellValue) else cell.numericCellValue
		CellType.BOOLEAN -> return cell.booleanCellValue
		CellType.STRING ->  return cell.stringCellValue
		CellType.FORMULA -> return cell.cellFormula
		else -> return null
	 }
  }

  override fun readRowValues(sheet:Int,row: Int, start_col: Int, end_col: Int): List<Any?>? {
	 val sheet: Sheet = work_book.getSheetAt(sheet) ?: return null
	 val row= sheet.getRow(row) ?: return null

	 val values: MutableList<Any?> = mutableListOf()

	 for(i in start_col..end_col){
		val cell = row.getCell(i)
		cell?.let {
		  when (cell.cellTypeEnum) {
			 CellType.NUMERIC -> if (HSSFDateUtil.isCellDateFormatted(cell)) values!!.add(Utils.getFormatedDate(cell.dateCellValue)) else values!!.add(
				cell.numericCellValue
			 )
			 CellType.BOOLEAN -> values!!.add(cell.booleanCellValue)
			 CellType.STRING -> values!!.add(cell.stringCellValue)
			 CellType.FORMULA -> values!!.add(cell.cellFormula)
			 else -> values!!.add(null)
		  }
		}
	 }

	 return if(values.size > 0 && Collections.frequency(values,null) < values.size) values else null
  }

  override fun readColumnValues(sheet:Int,col: Int, start_row: Int, end_row: Int): List<Any?>? {
	 val sheet: Sheet = work_book.getSheetAt(sheet) ?: return null
	 val values: MutableList<Any?> = mutableListOf()

	 for(i in start_row..end_row){
		val row = sheet.getRow(i) ?: return null
		val cell = row.getCell(i)

		cell?.let {
		  when (cell.cellTypeEnum) {
			 CellType.NUMERIC -> if (HSSFDateUtil.isCellDateFormatted(cell)) values!!.add(Utils.getFormatedDate(cell.dateCellValue)) else values!!.add(
				cell.numericCellValue
			 )
			 CellType.BOOLEAN -> values!!.add(cell.booleanCellValue)
			 CellType.STRING -> values!!.add(cell.stringCellValue)
			 CellType.FORMULA -> values!!.add(cell.cellFormula)
			 else -> values!!.add(null)
		  }
		}
	 }

	 return if(values.size > 0) values else null
  }

  override fun readCellRangeValues(sheet:Int,start_row: Int, end_row: Int, start_col: Int, end_col: Int): List<List<Any?>>? {
	 val sheet: Sheet = work_book.getSheetAt(sheet) ?: return null
	 val values: MutableList<List<Any?>>? = mutableListOf()

	 for(i in start_row..end_row){
		val row = sheet.getRow(i)

		val cells_values: MutableList<Any?> = mutableListOf()

		for(j in start_col..end_col){
		  val cell = row.getCell(j)
		  cell?.let {
			 when (cell.cellTypeEnum) {
				CellType.NUMERIC -> if (HSSFDateUtil.isCellDateFormatted(cell)) cells_values!!.add(
				  Utils.getFormatedDate(
					 cell.dateCellValue
				  )
				) else cells_values!!.add(cell.numericCellValue)
				CellType.BOOLEAN -> cells_values!!.add(cell.booleanCellValue)
				CellType.STRING -> cells_values!!.add(cell.stringCellValue)
				CellType.FORMULA -> cells_values!!.add(cell.cellFormula)
				else -> cells_values!!.add(null)
			 }
		  }
		}

		values!!.add(cells_values)
	 }

	 return values
  }

  override fun findValueOnCellIndex(value:String,sheet: Int,start_row: Int, end_row: Int,start_col: Int,end_col:Int): CellIndex?{
	 val sheet: Sheet = work_book.getSheetAt(sheet) ?: return null
	 val frow=  if(start_row == -1 || start_row > sheet.firstRowNum)  sheet.firstRowNum else start_row
	 val lrow= if(end_row == -1 || end_row > sheet.lastRowNum) sheet.lastRowNum else end_row

	 for(i in frow..lrow){
		val row= sheet.getRow(i)

		val fcol= if(start_col == -1 || start_col > row.firstCellNum)  row.firstCellNum.toInt() else start_col
		val lcol= if(end_col == -1 || end_col > row.lastCellNum)  row.lastCellNum.toInt() else end_col

		val col= row.indexOfFirst{cell -> (cell.columnIndex in fcol until lcol) &&
				  when(cell.cellTypeEnum){
					 CellType.STRING -> Utils.getLevenshteinScore(cell.stringCellValue.trim().toLowerCase(),value.trim().toLowerCase()) <= 2
					 CellType.NUMERIC -> if(value.isDigitsOnly()) cell.numericCellValue.equals(value.toDouble()) else false
					 CellType.BOOLEAN -> cell.booleanCellValue.equals(value.toBoolean())
					 else -> false
				  }
		}
		if(col!= -1){
		  return CellIndex(i,col)
		}
	 }

	 return null
  }

}