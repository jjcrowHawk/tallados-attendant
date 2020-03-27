package org.cbe.talladosatendant.util

import android.util.Log
import androidx.core.graphics.ColorUtils
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.delay
import org.apache.poi.ss.usermodel.BorderStyle
import java.io.IOException


class SheetsAPIWriterManager(val service : Sheets) : SheetsWriter() {

    companion object{
        val COLUMN_DIM= "COLUMNS"
        val ROW_DIM= "ROWS"

        val DASHED_BORDER= "DASHED"
        val DOTTED_BORDER= "DOTTED"
        val SOLID_BORDER= "SOLID"

        val BORDER_TOP_ONLY= "TOP"
        val BORDER_BOTTOM_ONLY= "BOTTOM"
        val BORDER_LEFT_ONY= "LEFT"
        val BORDER_RIGHT_ONLY= "RIGHT"
        val BORDER_TOP_BOTTOM= "TOP_BOTTOM"
        val BORDER_LEFT_RIGHT= "LEFT_RIGHT"
        val BORDER_ALL= "ALL"
    }

    private lateinit var spread_sheet : Spreadsheet
    private val sheets_indexes: MutableMap<String,Int> = HashMap()

    override val MERGE_ALL_CELLS= "MERGE_ALL"
    override val COLUMN_MERGE= "MERGE_COLUMNS"
    override val ROW_MERGE= "MERGE_ROWS"

    override val BACKGROUND_COLOR="BackgroundColor"
    override val TEXT_FORMAT= "TextFormat"
    override val HORIZONTAL_ALIGMENT= "horizontalAlignment"

    private var requests_done: Int= 0
    private val MAX_REQUESTS: Int= 80

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

    override fun writeValueToCell(sheet: String,row:Int, col:Int, value:Any): Int{

        if(!this.checkWritingSheetDimensions(sheet,row,col)){
            return -1
        }

        val colname= getColNamebyIndex(col)
        val range= "$sheet!$colname$row:$colname$row"

        val cell_values: List<List<Any>> = listOf(
            listOf(value)
        )
        val body: ValueRange = ValueRange()
            .setValues(cell_values)

        this.checkRequestLimit()
        val result = service.spreadsheets().values().update(spread_sheet.spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute()
       Log.i("SHEETMAN","%d cells updated.${result.updatedCells}")
        return result.updatedCells
    }

    override fun writeValuesToColumn(sheet:String, col:Int, row_start:Int, row_end:Int, values:List<Any>): Int{
        if(!this.checkWritingSheetDimensions(sheet,row_end,col)){
            return -1
        }

        val colname= getColNamebyIndex(col)
        val range= "$sheet!$colname$row_start:$colname$row_end"

        val cell_values: List<List<Any>> = listOf(
            values
        )
        val body: ValueRange = ValueRange()
            .setValues(cell_values)
            .setMajorDimension(COLUMN_DIM)

        this.checkRequestLimit()
        val result = service.spreadsheets().values().update(spread_sheet.spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute()
        Log.i("SHEETMAN","cells updated ${result.updatedCells}")

        return result.updatedCells
    }

    override fun writeValuesToRow(sheet:String, row:Int, col_start:Int, col_end:Int, values:List<Any>) : Int{
        if(!this.checkWritingSheetDimensions(sheet,row,col_end)){
            return -1
        }

        val c_start= getColNamebyIndex(col_start)
        val c_end = getColNamebyIndex(col_end)
        val range= "$sheet!$c_start$row:$c_end$row"

        val cell_values: List<List<Any>> = listOf(
            values
        )
        val body: ValueRange = ValueRange()
            .setValues(cell_values)
            .setMajorDimension(ROW_DIM)

        this.checkRequestLimit()
        val result = service.spreadsheets().values().update(spread_sheet.spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute()
        Log.i("SHEETMAN","cells updated ${result.updatedCells}")
        return result.updatedCells
    }

    override fun writeValuesToCellRange(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int, values:List<List<Any>>): Int{
        if(!this.checkWritingSheetDimensions(sheet,row_end,col_end)){
            return -1
        }

        val c_start= getColNamebyIndex(col_start)
        val c_end = getColNamebyIndex(col_end)
        val range= "$sheet!$c_start$row_start:$c_end$row_end"

        val cell_values: List<List<Any>> = values

        val body: ValueRange = ValueRange()
            .setValues(cell_values)
            .setMajorDimension(ROW_DIM)

        this.checkRequestLimit()
        val result = service.spreadsheets().values().update(spread_sheet.spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute()
        Log.i("SHEETMAN","cells updated ${result.updatedCells}")
        return result.updatedCells

    }

    override fun writeValueToMergedCells(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int, value:Any): Int{
        if(!this.checkWritingSheetDimensions(sheet,row_end,col_end)){
            return -1
        }

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

        this.checkRequestLimit()
        val result = service.spreadsheets().values().update(spread_sheet.spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute()
        Log.i("SHEETMAN","cells updated ${result.updatedCells}")
        return result.updatedCells
    }

    /* CELLS STYLING */

    fun setBorderOnCells(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int,
                         type:String= BORDER_ALL,style:String= SOLID_BORDER) : Boolean{
        val request= UpdateBordersRequest()
            .setRange(
                GridRange()
                    .setSheetId(spread_sheet.sheets[sheets_indexes[sheet]!!].properties.sheetId)
                    .setStartRowIndex(row_start - 1)
                    .setEndRowIndex(row_end - 1)
                    .setStartColumnIndex(col_start)
                    .setEndColumnIndex(col_end + 1)
            )
            .setTop(
                Border()
                    .setStyle(style)
                    .setColor(Color().setRed(0f).setGreen(0f).setBlue(0f))
                    .setWidth(1)
            )
            .setBottom(
                Border()
                    .setStyle(style)
                    .setColor(Color().setRed(0f).setGreen(0f).setBlue(0f))
                    .setWidth(1)
            )
            .setLeft(
                Border()
                    .setStyle(style)
                    .setColor(Color().setRed(0f).setGreen(0f).setBlue(0f))
                    .setWidth(1)
            )
            .setRight(
                Border()
                    .setStyle(style)
                    .setColor(Color().setRed(0f).setGreen(0f).setBlue(0f))
                    .setWidth(1)
            )
            .setInnerHorizontal(
                Border()
                    .setStyle(style)
                    .setColor(Color().setRed(0f).setGreen(0f).setBlue(0f))
                    .setWidth(1)
            )
            .setInnerVertical(
                Border()
                    .setStyle(style)
                    .setColor(Color().setRed(0f).setGreen(0f).setBlue(0f))
                    .setWidth(1)
            )

        val req= Request().setUpdateBorders(request)
        val batchUpdateRequest = BatchUpdateSpreadsheetRequest()
            .setRequests(listOf(req))

        this.checkRequestLimit()
        val response= service.spreadsheets()
            .batchUpdate(spread_sheet.spreadsheetId, batchUpdateRequest).execute()

        return response.replies.size > 0
    }

    fun setBoldContentOnCells(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int){
        val cellData= CellData().setUserEnteredFormat(
            CellFormat()
                .setTextFormat(
                    TextFormat()
                        .setBold(true)
                )
        )

        setStyleOnCells(sheet,row_start,row_end,col_start,col_end,TEXT_FORMAT,cellData)
    }

    fun setFontOnCells(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int,font:String,font_size:Int){
        val cellData= CellData().setUserEnteredFormat(
            CellFormat()
                .setTextFormat(
                    TextFormat()
                        .setFontFamily(font)
                        .setFontSize(font_size)
                )
        )

        setStyleOnCells(sheet,row_start,row_end,col_start,col_end,TEXT_FORMAT,cellData)
    }

    fun setBackgroundColorOnCells(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int,color: Color){

        val cellData = CellData().setUserEnteredFormat(
            CellFormat()
                .setBackgroundColor(color)
        )

        setStyleOnCells(sheet,row_start,row_end,col_start,col_end,BACKGROUND_COLOR,cellData)
    }

    private fun setStyleOnCells(sheet:String, row_start:Int, row_end:Int, col_start:Int, col_end:Int, style_type:String,cellData: CellData) : Boolean{

        val fields= "userEnteredFormat." +style_type

        val repeatCellRequest= RepeatCellRequest()
            .setCell(cellData)
            .setRange(
                GridRange()
                    .setSheetId(spread_sheet.sheets[sheets_indexes[sheet]!!].properties.sheetId)
                    .setStartRowIndex(row_start - 1)
                    .setEndRowIndex(row_end)
                    .setStartColumnIndex(col_start)
                    .setEndColumnIndex(col_end)
            )
            .setFields(fields)

        val request = Request()
        request.setRepeatCell(repeatCellRequest)

        val batchUpdateRequest = BatchUpdateSpreadsheetRequest()
            .setRequests(listOf(request))

        this.checkRequestLimit()
        val response= service.spreadsheets()
            .batchUpdate(spread_sheet.spreadsheetId, batchUpdateRequest).execute()

        return response.replies.size > 0
    }

    /* CELLS UTILS */

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

    /**
     * This function is based on zero-index start. This function resize the cells from [start_index]  to [end_index]
     * (not included)
     *
     * @return a boolean value that determine whether the operation was done.
     */
    fun autoResizeCells(sheet:String, start_index:Int, end_index:Int, dimension: String = COLUMN_DIM) : Boolean{
        val resize_req = AutoResizeDimensionsRequest()
            .setDimensions(
                DimensionRange()
                    .setSheetId(spread_sheet.sheets[sheets_indexes[sheet]!!].properties.sheetId)
                    .setDimension(dimension)
                    .setStartIndex(start_index)
                    .setEndIndex(end_index)
            )
        val request= Request().setAutoResizeDimensions(resize_req)
        val batchUpdate= BatchUpdateSpreadsheetRequest().setRequests(listOf(request))

        this.checkRequestLimit()
        val response= service.spreadsheets()
            .batchUpdate(spread_sheet.spreadsheetId,batchUpdate).execute()

        return response.replies.size > 0
    }

    /* SHEET UTILS */

    override fun setSheetTitle(sheet_index:Int, title:String) : Boolean{
        val sheet_prop= spread_sheet.sheets[sheet_index].properties
        sheet_prop.title= title
        val update_req= UpdateSheetPropertiesRequest()
            .setProperties(sheet_prop)
            .setFields("title")
        val request= Request().setUpdateSheetProperties(update_req)
        val body= BatchUpdateSpreadsheetRequest().setRequests(listOf(request))

        this.checkRequestLimit()
        val response= service.spreadsheets().batchUpdate(spread_sheet.spreadsheetId,body).execute()
        this.sheets_indexes[title]= sheet_index
        return response.replies.size > 0
    }

    private fun getUpdatedSheet(sheet: String){
        this.checkRequestLimit()
        val response=  service.spreadsheets().get(spread_sheet.spreadsheetId).execute()
        spread_sheet.sheets[this.sheets_indexes[sheet]!!]= response.sheets[this.sheets_indexes[sheet]!!]
    }

    private fun checkWritingSheetDimensions(sheet:String,row_index:Int= -1,col_index:Int= -1) : Boolean{
        if(row_index < 0 && col_index <0){
            return false
        }

        val props= spread_sheet.sheets[sheets_indexes[sheet]!!].properties
        val row_count= props.gridProperties.rowCount
        val col_count= props.gridProperties.columnCount

        Log.i("SHEETMAN","column cnt: ${col_count} with indexes: $col_index")


        if(row_index > 0 && col_index > 0  && row_index >= row_count - 1 && col_index>= col_count - 1){

            return this.expandGridDimensions(sheet,30,COLUMN_DIM) &&
                    this.expandGridDimensions(sheet,30,ROW_DIM)
        }
        if(row_index > 0 && row_index >= row_count - 1){
            return this.expandGridDimensions(sheet,30,ROW_DIM)
        }
        if(col_index > 0 && col_index >= col_count - 1){
            return this.expandGridDimensions(sheet,30,COLUMN_DIM)
        }

        return true


    }

    private fun expandGridDimensions(sheet:String, count:Int,dimension: String): Boolean{
        Log.i("SHEETMAN,","EXPANDING DIMENSIONS!")

        if(count <0){
            return false
        }


        val sheet_prop= spread_sheet.sheets[sheets_indexes[sheet]!!].properties
        val append_req= AppendDimensionRequest()
            .setSheetId(sheet_prop.sheetId)
            .setDimension(dimension)
            .setLength(count)

        val request= Request().setAppendDimension(append_req)
        val body= BatchUpdateSpreadsheetRequest().setRequests(listOf(request))
        try{
            this.checkRequestLimit()
            val response= service.spreadsheets().batchUpdate(spread_sheet.spreadsheetId,body).execute()
            Log.i("SHEETMAN","DIMS EXPANDED")
            getUpdatedSheet(sheet)
            return true
        }
        catch(e:Exception){
            e.printStackTrace()
        }
        Log.i("SHEETMAN","DIMS NOT EXPANDED")
        return false
    }

    private fun checkRequestLimit(){
        Log.i("SHEETMAN","REQUESTS DONE: "+ this.requests_done)
        this.requests_done++
        if(this.requests_done == this.MAX_REQUESTS){
            Log.i("SHEETMAN","MAX REQUESTS REACHED!")
            Thread.sleep(10000)
            Log.i("SHEETMAN","REQUESTS RESET!")
            this.requests_done= 0
        }

    }

}