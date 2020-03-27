package org.cbe.talladosatendant.util

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridView
import android.widget.ListAdapter
import android.widget.ListView
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.android.synthetic.main.dialog_waiting.view.*
import org.apache.poi.ss.formula.functions.T
import org.apache.poi.util.IOUtils
import org.apache.poi.xslf.usermodel.DrawingTextPlaceholder
import org.cbe.talladosatendant.R
import java.io.*
import java.lang.Exception
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*


class Utils{

    companion object{

        private val DATE_FORMAT= "dd/MM/yyyy"

        /*
         Date util Methods
         */

        fun getNumberFromMonthName(month:String): Int{
            when(month){
                "Enero" -> return 0
                "Febrero" -> return 1
                "Marzo" -> return 2
                "Abril" -> return 3
                "Mayo" -> return 4
                "Junio" -> return 5
                "Julio" -> return 6
                "Agosto" -> return 7
                "Septiembre" -> return 8
                "Octubre" -> return 9
                "Noviembre" -> return 10
                "Diciembre" -> return 11
            }
            return -1
        }

        fun getMonthNameFromNumber(month:Int) : String{
            when(month){
                0 -> return "Enero"
                1 -> return "Febrero"
                2 -> return "Marzo"
                3 -> return "Abril"
                4 -> return "Mayo"
                5 -> return "Junio"
                6 -> return "Julio"
                7 -> return "Agosto"
                8 -> return "Septiembre"
                9 -> return "Octubre"
                10 -> return "Noviembre"
                11 -> return "Diciembre"
            }
            return ""
        }

        fun getDayNameFromNumber(day: Int) : String{
            when(day){
                1 -> return "Domingo"
                2 -> return "Lunes"
                3 -> return "Martes"
                4 -> return "Miércoles"
                5 -> return "Jueves"
                6 -> return "Viernes"
                7 -> return "Sábado"
            }
            return ""
        }

        fun getFormattedLatinDate(date: Date): String{
            val cal = Calendar.getInstance()
            cal.time= date
            val month= getMonthNameFromNumber(cal.get(Calendar.MONTH))
            val dayName= getDayNameFromNumber(cal.get(Calendar.DAY_OF_WEEK))
            return "$dayName, ${cal.get(Calendar.DAY_OF_MONTH)} de $month del ${cal.get(Calendar.YEAR)}"
        }

        fun getFormatedDate(date: Date): String{
            val cal = Calendar.getInstance()
            cal.time= date
            val formatter= SimpleDateFormat(DATE_FORMAT)
            return formatter.format(cal.time)
            // "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
        }

        fun getDiffYears(first: Date?, last: Date?): Int {
            val a: Calendar = getCalendar(first)
            val b: Calendar = getCalendar(last)
            var diff: Int = b.get(Calendar.YEAR) - a.get(Calendar.YEAR)
            if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                a.get(Calendar.MONTH) === b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE)
            ) {
                diff--
            }
            return diff
        }

        fun getCalendar(date: Date?): Calendar {
            val cal: Calendar = Calendar.getInstance()
            cal.setTime(date)
            return cal
        }

        fun createDate(day: Int,month: Int,year: Int): Date{
            val cal= getCalendar(Date())
            cal.set(Calendar.DAY_OF_MONTH,day)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.YEAR,year)
            return cal.time
        }

        fun getNearestDateOfWeekDay(day: Int,from: Date) : Date{
            val cal_from= getCalendar(from)
            while (cal_from.get(Calendar.DAY_OF_WEEK) != day) {
                cal_from.add(Calendar.DATE, 1)
            }
            Log.i("UTILS","nearest date got: ${getFormattedLatinDate(cal_from.time)}")
            return cal_from.time
        }

        fun parseDateFromString(string: String): Date?{
            try {
                val formatter = SimpleDateFormat(DATE_FORMAT)
                return formatter.parse(string)
            }catch (ex:Exception){
                ex.printStackTrace()
            }
            return null
        }


        /*
         List View util Methods
         */

        fun setListViewFullHeight(listView: ListView) {
            if(listView.adapter.count == 0)
                return
            val listAdapter: ListAdapter = listView.adapter ?: return
            var totalHeight = 0
            var i = 0
            val len: Int = listAdapter.count
            while (i < len) {
                val listItem: View = listAdapter.getView(i, null, listView)
                listItem.measure(0, 0)
                totalHeight += listItem.measuredHeight
                i++
            }
            val params: ViewGroup.LayoutParams = listView.layoutParams
            params.height = (totalHeight
                    + listView.dividerHeight * (listAdapter.count - 1))
            listView.layoutParams = params
        }

        fun setGridViewHeightBasedOnChildren(gridView: GridView, noOfColumns: Int) {
            if(gridView.adapter.count == 0)
                return
            val gridViewAdapter = gridView.adapter
                ?: // adapter is not set yet
                return
            var totalHeight: Int //total height to set on grid view
            val items = gridViewAdapter.count //no. of items in the grid
            val rows: Int //no. of rows in grid
            val listItem = gridViewAdapter.getView(0, null, gridView)
            listItem.measure(0, 0)
            totalHeight = listItem.measuredHeight
            val x: Float
            if (items > noOfColumns) {
                x = items / noOfColumns.toFloat()
                //Check if exact no. of rows of rows are available, if not adding 1 extra row
                rows = if (items % noOfColumns != 0) {
                    (x + 1).toInt()
                } else {
                    x.toInt()
                }
                totalHeight *= rows
                //Adding any vertical space set on grid view
                totalHeight += gridView.verticalSpacing * rows
            }
            //Setting height on grid view
            val params = gridView.layoutParams
            params.height = totalHeight
            gridView.layoutParams = params
        }

        /*
         Dialog Util Methods
         */

        fun showWaitingDialog(context: Context, text : String= "") : Dialog{
            val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.dialog_waiting, null)
            view.wd_title.text = text
            view.wd_bg_view.setBackgroundColor(Color.parseColor("#60000000")) //Background Color
            view.wd_cardview.setCardBackgroundColor(Color.parseColor("#70000000"))
            setColorFilter(view.wd_pbar.indeterminateDrawable,
                ResourcesCompat.getColor(context.resources, R.color.colorPrimary, null))
            val dialog= Dialog(context,R.style.CustomProgressBarTheme)
            dialog.setContentView(view)
            dialog.show()

            return dialog
        }

        fun showSuccessDialog(context: Context, title : String= "", text : String= "",action_listener: (DialogInterface,Int) -> Unit){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(text)
            builder.setIcon(R.drawable.ok_dialog)
            builder.setPositiveButton("OK",action_listener)
            builder.setCancelable(false)
            builder.show()
        }

        fun showInfoDialog(context: Context, title : String= "", text : String= "",action_listener: (DialogInterface,Int) -> Unit){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(text)
            builder.setIcon(R.drawable.info_dialog)
            builder.setPositiveButton("OK",action_listener)
            builder.setCancelable(false)
            builder.show()
        }

        fun showErrorDialog(context: Context, title : String= "", text : String= "",action_listener: (DialogInterface,Int) -> Unit){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(text)
            builder.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
            builder.setPositiveButton("OK",action_listener)
            builder.setCancelable(false)
            builder.show()
        }

        fun showEditDialog(context: Context, title: String= "",message: String = "",default_placeholder:String= "",positive_btn_text: String= "OK",action_listener: (DialogInterface,Int) -> Unit){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
            val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.dialog_edit, null)
            view.findViewById<EditText>(R.id.et_dialog).setText(default_placeholder)
            builder.setView(view)
            builder.setNegativeButton("Cancelar",{dialog,_ -> dialog.dismiss()})
            builder.setPositiveButton(positive_btn_text,action_listener)
            builder.setCancelable(false)
            builder.show()
        }

        fun showConfirmDialog(context: Context,title: String = "",message: String = "",confirm_text: String= "OK",confirm_listener: (DialogInterface,Int) -> Unit){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setIcon(R.drawable.info_dialog)
            builder.setPositiveButton(confirm_text,confirm_listener)
            builder.setNegativeButton("Cancelar"){dialog,_ -> dialog.dismiss()}
            builder.setCancelable(false)
            builder.show()
        }

        fun showChoicesDialog(context: Context,title: String= "",choices: Array<String>,checkedItems: BooleanArray,confirm_listener: (DialogInterface, Int) -> Unit){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            //builder.setMessage(message)
            builder.setMultiChoiceItems(choices,checkedItems){_, which, isChecked ->
                checkedItems[which] = isChecked
            }
            builder.setNegativeButton("Cancelar"){dialog, _ -> dialog.dismiss()}
            builder.setPositiveButton("OK",confirm_listener)
            builder.setCancelable(false)
            builder.create().show()
        }

        fun setColorFilter(@NonNull drawable: Drawable, color:Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
            } else {
                @Suppress("DEPRECATION")
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }
        }

        fun showBarChartDialog(context: Context,title: String,data:List<Float>,labels:List<String>,axis_colors:List<Int>,action_listener: (DialogInterface,Int) -> Unit){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.dialog_attendances_graph, null)

            val chart:BarChart = view.findViewById(R.id.attendances_chart)
            val bar_data: BarData = BarData()

            for( i in data.indices){
                Log.i("CHART:","${data[i]}")
                val entry: BarEntry= BarEntry(i.toFloat(),data[i])
                val dataset: BarDataSet = BarDataSet(listOf(entry),labels[i])
                dataset.valueTextSize= 14f
                dataset.valueTypeface= Typeface.DEFAULT_BOLD
                dataset.color= axis_colors[i]
                bar_data.addDataSet(dataset)
            }

            chart.data = bar_data
            chart.data.setValueFormatter(PercentFormatter())
            chart.axisLeft.valueFormatter= PercentFormatter()
            val desc= Description()
            desc.text = "Porcentajes de Asistencia"
            chart.description= desc
            chart.invalidate()

            builder.setPositiveButton("CERRAR",action_listener)

            builder.setView(view)
            val alert= builder.create()

            alert.show()


        }

        /*
         * Colors Methods
         */

        fun RGBAMapToGoogleColor(color_map: Map<String,Float>): com.google.api.services.sheets.v4.model.Color{
            return com.google.api.services.sheets.v4.model.Color()
                .setAlpha(color_map["alpha"])
                .setRed(color_map["red"])
                .setGreen(color_map["green"])
                .setBlue(color_map["blue"])
        }

        fun getRandomRGBAColorMap(): Map<String,Float>{
            return mapOf("alpha" to 0.20f,
                "red" to Random().nextFloat(),
                "green" to Random().nextFloat(),
                "blue" to Random().nextFloat())
        }

        @Throws(IOException::class)
        fun getBitmapFromUri(uri: Uri,context: Context): Bitmap {
            if(Build.VERSION.SDK_INT < 28) {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver,uri)
                return bitmap

            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                return bitmap
            }
        }

        fun getBitmapFromStream(stream:InputStream): Bitmap{
            return BitmapFactory.decodeStream(stream)
        }


        /*
         * File methods
         */

        fun createTempFileWithContent(name:String, extension: String, stream:InputStream) : String{
            val file= File.createTempFile(name,extension)
            val fileOut= FileOutputStream(file)
            IOUtils.copy(stream,fileOut)
            fileOut.close()
            return file.absolutePath
        }

        /*
         * List/Arrays methods
         */

        fun checkElementsContiguity(array:Array<Int>): Boolean{
            Arrays.sort(array)

            for (i in 1 until array.size){
                if (array[i] - array[i-1] > 1)
                    return false
            }
            return true
        }

        /*
         * String methods
         */

        fun getNamesFromFullName(name:String): Array<String>{
            val split_name= name.split(" ")

            return when(split_name.size){
                2 -> arrayOf(split_name.subList(0,1).joinToString(" "),split_name.subList(1,2).joinToString(" "))
                3 -> arrayOf(split_name.subList(0,2).joinToString(" "),split_name.subList(2,3).joinToString(" "))
                4 -> arrayOf(split_name.subList(0,2).joinToString(" "),split_name.subList(2,4).joinToString(" "))
                5 -> arrayOf(split_name.subList(0,2).joinToString(" "),split_name.subList(2,5).joinToString(" "))
                else-> split_name.toTypedArray()
            }
        }

        fun getLevenshteinScore(s: String, t: String,
                        charScore : (Char, Char) -> Int = { c1, c2 -> if (c1 == c2) 0 else 1}) : Int {

            // Special cases
            if (s == t)  return 0
            if (s == "") return t.length
            if (t == "") return s.length

            val initialRow : List<Int> = (0 until t.length + 1).map { it }.toList()
            return (0 until s.length).fold(initialRow, { previous, u ->
                (0 until t.length).fold( mutableListOf(u+1), {
                        row, v -> row.add(minOf(row.last() + 1,
                    previous[v+1] + 1,
                    previous[v] + charScore(s[u],t[v])))
                    row
                })
            }).last()

        }

        //TODO  UPDATE METHOD TO GET CODE PREFIX BASED ON COUNTRY

        fun getPhoneNumberFromExpString(exp_number: String) : String{
            //Locale.getDefault()
            val country_code= "+593"

            val regex= Regex("(\\d|-|\\+|\\.|E){7,}")
            val numbers= regex.findAll(exp_number).map{it.value}.toList()
            /* numbers.forEach { num ->
                val number= BigDecimal(exp_number.toString()).longValueExact().toString()
            } */
            val str_number= numbers.joinToString(" | "){ s: String -> "$country_code${BigDecimal(s.trim('-',' ')).longValueExact()}"}

            return str_number
        }

        /*
         Map Utils
         */

    }
}