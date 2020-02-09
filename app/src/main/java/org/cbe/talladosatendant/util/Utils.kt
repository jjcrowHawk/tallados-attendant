package org.cbe.talladosatendant.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ListAdapter
import android.widget.ListView
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.android.synthetic.main.dialog_waiting.view.*
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Course
import java.util.*
import kotlin.collections.ArrayList


class Utils{

    companion object{
        fun getSampleCourses() : ArrayList<Course>{
            return arrayListOf<Course>(
                Course(name = "Niños de 3 - 4 años"),
                Course(name = "Niños de 5 - 6 años"),
                Course(name = "Niños de 7 - 8 años"),
                Course(name ="Niños de 9 - 11 años")
                )
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
            builder.show()
        }

        fun showInfoDialog(context: Context, title : String= "", text : String= "",action_listener: (DialogInterface,Int) -> Unit){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(text)
            builder.setIcon(R.drawable.info_dialog)
            builder.setPositiveButton("OK",action_listener)
            builder.show()
        }

        fun showEditDialog(context: Context, title: String= "",message: String = "",positive_btn_text: String= "OK",action_listener: (DialogInterface,Int) -> Unit){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
            val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.dialog_edit, null)
            builder.setView(view)
            builder.setNegativeButton("Cancelar",{dialog,_ -> dialog.dismiss()})
            builder.setPositiveButton(positive_btn_text,action_listener)
            builder.show()
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
    }
}