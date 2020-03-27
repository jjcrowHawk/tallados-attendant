package org.cbe.talladosatendant.util

abstract class SheetsWriter {

   protected open val MERGE_ALL_CELLS: String get()= "MERGE_ALL"
   protected open val COLUMN_MERGE : String get()= "MERGE_COLUMNS"
   protected open val ROW_MERGE : String get()= "MERGE_ROWS"
   protected open val BACKGROUND_COLOR: String get()="BackgroundColor"
   protected open val TEXT_FORMAT: String get()= "TextFormat"
   protected open val HORIZONTAL_ALIGMENT: String get()= "horizontalAlignment"
   protected open val VERTICAL_ALIGMENT: String get()= "verticalAligment"

  protected val column_names= arrayOf("A","B","C","D","E","F","G","H","I","J","K"
	 ,"L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z")

  abstract fun writeValueToCell(sheet: String,row:Int, col:Int, value:Any): Int

  abstract fun writeValuesToColumn(sheet:String, col:Int, row_start:Int, row_end:Int, values:List<Any>): Int

  abstract fun writeValuesToRow(sheet:String, row:Int, col_start:Int, col_end:Int, values:List<Any>) : Int

  abstract fun writeValuesToCellRange(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int, values:List<List<Any>>): Int

  abstract fun writeValueToMergedCells(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int, value:Any): Int

  abstract fun setSheetTitle(sheet_index:Int, title:String) : Boolean

}