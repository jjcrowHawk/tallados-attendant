package org.cbe.talladosatendant.util

import org.cbe.talladosatendant.pojo.CellIndex

abstract class SheetsReader() {

  abstract fun getNumberOfSheets(): Int

  abstract fun getSheetsTitles(): List<String>

  abstract fun getNumberOfRows(sheet: Int): Int

  abstract fun readCellValue(sheet:Int,row:Int,col:Int) : Any?

  abstract fun readRowValues(sheet:Int,row:Int,start_col:Int,end_col:Int) : List<Any?>?

  abstract fun readColumnValues(sheet:Int,col:Int,start_row:Int,end_row:Int) : List<Any?>?

  abstract fun readCellRangeValues(sheet:Int,start_row:Int,end_row:Int,start_col:Int,end_col:Int) : List<List<Any?>>?

  abstract fun findValueOnCellIndex(value:String,sheet: Int,start_row: Int= -1, end_row: Int = -1,start_col: Int= -1,end_col:Int= -1): CellIndex?
}