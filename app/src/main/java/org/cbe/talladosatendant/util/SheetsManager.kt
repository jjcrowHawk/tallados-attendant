package org.cbe.talladosatendant.util

import android.util.Log
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import java.io.IOException


class SheetsManager(val service : Sheets) {

    private lateinit var spread_sheet : Spreadsheet
    private val column_names= arrayOf("A","B","C","D","E","F","G","H","I","J","K"
        ,"L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z")

    private val COLUMN_DIM= "COLUMNS"
    private val ROW_DIM= "ROWS"

    private val MERGE_ALL_CELLS= "MERGE_ALL"
    private val COLUMN_MERGE= "MERGE_COLUMNS"
    private val ROW_MERGE= "MERGE_ROWS"

    fun createSpreadSheet(title: String): Boolean{
        var spreadsheet = Spreadsheet()
            .setProperties(
                SpreadsheetProperties()
                    .setTitle(title)
            )
        try{
            this.spread_sheet= service.spreadsheets().create(spreadsheet).execute()
            Log.i("SHEETMAN","Spread Sheet Created")
            return true
        }
        catch(e:IOException){
            e.printStackTrace()
        }
        return false
    }

    fun setSheetTitle(sheet_index:Int, title:String) : Boolean{
        val sheet_prop= spread_sheet.sheets[sheet_index].properties
        sheet_prop.title= title
        val update_req= UpdateSheetPropertiesRequest()
            .setProperties(sheet_prop)
            .setFields("title")
        val request= Request().setUpdateSheetProperties(update_req)
        val body= BatchUpdateSpreadsheetRequest().setRequests(listOf(request))
        val response= service.spreadsheets().batchUpdate(spread_sheet.spreadsheetId,body).execute()
        return response.replies.size > 0
    }

    fun writeValueToCell(sheet: String,row:Int, col:Int, value:Any): Int{
        val colname= getColNamebyIndex(col)
        val range= "$sheet!$colname$row:$colname$row"

        val cell_values: List<List<Any>> = listOf(
            listOf(value)
        )
        val body: ValueRange = ValueRange()
            .setValues(cell_values)

        val result = service.spreadsheets().values().update(spread_sheet.spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute()
        System.out.printf("%d cells updated.", result.updatedCells)
        return result.updatedCells
    }

    fun writeValuesToColumn(sheet:String, col:Int, row_start:Int, row_end:Int, values:List<Any>): Int{
        val colname= getColNamebyIndex(col)
        val range= "$sheet!$colname$row_start:$colname$row_end"

        val cell_values: List<List<Any>> = listOf(
            values
        )
        val body: ValueRange = ValueRange()
            .setValues(cell_values)
            .setMajorDimension(COLUMN_DIM)

        val result = service.spreadsheets().values().update(spread_sheet.spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute()
        System.out.printf("%d cells updated.", result.updatedCells)

        return result.updatedCells
    }

    fun writeValuesToRow(sheet:String, row:Int, col_start:Int, col_end:Int, values:List<Any>) : Int{
        val c_start= getColNamebyIndex(col_start)
        val c_end = getColNamebyIndex(col_end)
        val range= "$sheet!$c_start$row:$c_end$row"

        val cell_values: List<List<Any>> = listOf(
            values
        )
        val body: ValueRange = ValueRange()
            .setValues(cell_values)
            .setMajorDimension(ROW_DIM)

        val result = service.spreadsheets().values().update(spread_sheet.spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute()
        System.out.printf("%d cells updated.", result.updatedCells)
        return result.updatedCells
    }

    fun writeValuesToCellRange(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int, values:List<Any>){

    }


    fun writeValueToMergedCells(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int, value:Any): Int{
        val c_start= getColNamebyIndex(col_start)
        val c_end = getColNamebyIndex(col_end)
        val range= "$sheet!$c_start$row_start:$c_end$row_end"
        val grid_range= GridRange()
            .setSheetId(0)
            .setStartColumnIndex(col_start)
            .setEndColumnIndex(col_end + 1)
            .setStartRowIndex(row_start - 1)
            .setEndRowIndex(row_end)
        val merge_req= Request().setMergeCells(MergeCellsRequest()
            .setMergeType(MERGE_ALL_CELLS)
            .setRange(grid_range))
        val req_body = BatchUpdateSpreadsheetRequest().setRequests(listOf(merge_req))
        val response= service.spreadsheets().batchUpdate(spread_sheet.spreadsheetId,req_body).execute()
        if(response.replies.size == 0){
            return -1
        }

        val cell_values: List<List<Any>> = listOf(
            listOf(value)
        )

        val body: ValueRange = ValueRange()
            .setValues(cell_values)

        val result = service.spreadsheets().values().update(spread_sheet.spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute()
        System.out.printf("%d cells updated.", result.updatedCells)
        return result.updatedCells
    }

    private fun getColNamebyIndex(index: Int) : String{
        var _index= index
        var name= ""
        if(_index <= column_names.size - 1){
            name= column_names[_index]
        }
        else{
            while(_index >= 0){
                val char_index= _index / (column_names.size)
                name+= if(char_index >0) column_names[(char_index - 1) % column_names.size] else column_names[_index]
                _index-= column_names.size * (if(char_index>0) char_index else 1)
            }
        }

        return name
    }
}