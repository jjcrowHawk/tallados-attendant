package org.cbe.talladosatendant.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*

import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.util.Utils
import org.cbe.talladosatendant.viewmodels.ExportAttendanceViewModel
import java.util.*
import kotlin.coroutines.CoroutineContext

class ExportAttendanceFragment : Fragment(), AdapterView.OnItemSelectedListener, CoroutineScope {

    companion object {
        fun newInstance() = ExportAttendanceFragment()
    }

    private lateinit var viewModel: ExportAttendanceViewModel

    private lateinit var spinner_course: Spinner
    private lateinit var spinner_type: Spinner
    private lateinit var tv_date_from: TextView
    private lateinit var tv_date_To: TextView
    private lateinit var btn_date_from: ImageButton
    private lateinit var btn_date_to: ImageButton
    private lateinit var btn_export: Button
    private lateinit var layout_custom_date: LinearLayout

    private var waiting_dialog: Dialog? = null

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_export_attendance, container, false)

        spinner_course= view.findViewById(R.id.course_spinner_export)
        spinner_type= view.findViewById(R.id.type_spinner_export)
        tv_date_from= view.findViewById(R.id.tv_date_from_export)
        tv_date_To= view.findViewById(R.id.tv_date_to_export)
        btn_date_from= view.findViewById(R.id.btn_date_from_export)
        btn_date_to= view.findViewById(R.id.btn_date_to_export)
        btn_export= view.findViewById(R.id.btn_export)
        layout_custom_date= view.findViewById(R.id.layout_date_range_export)

        spinner_course.isEnabled= false
        layout_custom_date.visibility= View.GONE

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ExportAttendanceViewModel::class.java)


        spinner_type.adapter = ArrayAdapter<String>(activity!!.applicationContext,
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.export_types)
        spinner_type.onItemSelectedListener= this

        //OBSERVERS

        viewModel.courses.observe(this.viewLifecycleOwner, Observer {
                courses ->
            Log.i("REVIEWATTENDANCE","THIS courses: ${viewModel.courses.value!!}")
            spinner_course.adapter = ArrayAdapter<Course>(activity!!.applicationContext,
                android.R.layout.simple_spinner_dropdown_item,
                viewModel.courses.value!!)
            spinner_course.onItemSelectedListener= this
            spinner_course.isEnabled= true
        })

        viewModel.date_from_label.observe(this.viewLifecycleOwner, Observer {string_date ->
            this.tv_date_from.text= string_date
        })

        viewModel.date_to_label.observe(this.viewLifecycleOwner, Observer {string_date ->
            this.tv_date_To.text= string_date
        })

        viewModel.showSelectDateButtons.observe(this.viewLifecycleOwner, Observer {value ->
            if(value){
                this.btn_date_to.visibility= View.VISIBLE
                this.btn_date_from.visibility= View.VISIBLE
            }
            else{
                this.btn_date_to.visibility= View.GONE
                this.btn_date_from.visibility= View.GONE
            }
        })

        viewModel.showExportingDialog.observe(this.viewLifecycleOwner, Observer{value ->
            if(value){
                waiting_dialog = Utils.showWaitingDialog(context!!,"Recopilando asistencias...")

            }
            else{
                waiting_dialog?.dismiss()
            }
        })

        viewModel.showNoDataFoundDialog.observe(this.viewLifecycleOwner, Observer {value ->
            if(value){
                Utils.showInfoDialog(context!!,"Aviso de Exporte", "No se encontraron datos para exportar"){
                    dialog,_->
                        dialog.dismiss()
                        viewModel.showNoDataFoundDialog.value= false
                }
            }
        })


        //LISTENERS

        btn_date_from.setOnClickListener {
            val c = Calendar.getInstance()
            val c_year = c.get(Calendar.YEAR)
            val c_month = c.get(Calendar.MONTH)
            val c_day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val cal= Calendar.getInstance()
                cal.set(year,monthOfYear,dayOfMonth)
                viewModel.date_from = cal.time
                viewModel.date_from_label.value= Utils.getFormattedLatinDate(cal.time)

            }, c_year, c_month, c_day)
            dpd.show()
        }

        btn_date_to.setOnClickListener {
            val c = Calendar.getInstance()
            val c_year = c.get(Calendar.YEAR)
            val c_month = c.get(Calendar.MONTH)
            val c_day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val cal= Calendar.getInstance()
                cal.set(year,monthOfYear,dayOfMonth)
                viewModel.date_to = cal.time
                viewModel.date_to_label.value= Utils.getFormattedLatinDate(cal.time)

            }, c_year, c_month, c_day)
            dpd.show()
        }

        btn_export.setOnClickListener{
            Log.i("ATTEXPRT","ready to export")
            viewModel.showExportingDialog.value= true
            var attendances_records_map : Map<*,*>? = null
            launch {
                val deferred= async(Dispatchers.IO){

                    attendances_records_map= viewModel.getAttendancesData()
                }

                deferred.await()


                attendances_records_map?.let {
                    waiting_dialog?.findViewById<TextView>(R.id.wd_title)?.text = "Generando hoja de reporte...."



                } ?: run {
                    viewModel.showExportingDialog.value= false
                    viewModel.showNoDataFoundDialog.value= true
                }




            }

        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if(parent!!.id == R.id.course_spinner_export) {
            viewModel.selected_course= viewModel.courses.value!![position]
        }
        else if(parent!!.id == R.id.type_spinner_export){
            viewModel.setDateRange(viewModel.export_types[position])
            layout_custom_date.visibility= View.VISIBLE
        }
    }

}