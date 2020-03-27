package org.cbe.talladosatendant.ui

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.Sheets
import kotlinx.coroutines.*

import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Attendance
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.pojo.AttendancePeriod
import org.cbe.talladosatendant.pojo.AttendanceRecordStudent
import org.cbe.talladosatendant.util.GoogleAuthManager
import org.cbe.talladosatendant.util.SheetsAPIWriterManager
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

    private lateinit var auth_manager: GoogleAuthManager
    private lateinit var sheets_manager: SheetsAPIWriterManager


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

        viewModel.showErrorDialog.observe(this.viewLifecycleOwner, Observer {value ->
            if(value){
                Utils.showErrorDialog(context!!,"Error al Exportar", viewModel.errorMessage){
                        dialog,_->
                    dialog.dismiss()
                    viewModel.showErrorDialog.value= false
                }
            }
        })

        viewModel.showSuccesDialog.observe(this.viewLifecycleOwner, Observer {value ->
            if(value){
                Utils.showSuccessDialog(context!!,"Reporte Generado", "El reporte ha sido generado y exportado con éxito a su cuenta de google drive"){
                        dialog,_->
                    dialog.dismiss()
                    viewModel.showSuccesDialog.value= false
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
            //var attendances_records_map : Map<*,*>? = null
            launch {
                val deferred= async(Dispatchers.IO){

                   viewModel.generateAttendancesData()
                }

                deferred.await()


                viewModel.attendances_students_map?.let {
                    waiting_dialog?.findViewById<TextView>(R.id.wd_title)?.text = "Accesando a Google Drive...."
                    requestSheetApiSignIn(context!!)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GoogleAuthManager.REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener { account ->
                        Log.i("GOOGLE LOG IN","${account.displayName}")
                        auth_manager.buildSheetService(account,context!!)?.let {
                            createSheetReport(it)
                        } ?: run{
                            Log.i("ATTEXPRT", "Failed to get OAuth token")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ERROR-DRIVE",e.toString())
                    }
            }
        }
    }

    private fun requestSheetApiSignIn(context: Context) {
        /*
        GoogleSignIn.getLastSignedInAccount(context)?.also { account ->
            Timber.d("account=${account.displayName}")
        }
         */
        auth_manager= GoogleAuthManager()

        auth_manager.setUpSheetApiAuthClient(context)
        auth_manager.signOutClient(context)
        startActivityForResult(auth_manager.getClient().signInIntent, GoogleAuthManager.REQUEST_SIGN_IN)
    }

    private fun createSheetReport(service: Sheets, period: AttendancePeriod = AttendancePeriod.WEEKLY, period_value: Int = Calendar.SATURDAY) {
        waiting_dialog?.findViewById<TextView>(R.id.wd_title)?.text = "Generando Reporte...."
        sheets_manager= SheetsAPIWriterManager(service)

        launch(Dispatchers.Default) {

            try {

                /* SHEET CREATION */

                sheets_manager.createSpreadSheet(activity!!.applicationContext.resources.getString(R.string.app_name))
                var sheet_title = viewModel.selected_course!!.name
                sheets_manager.setSheetTitle(0, sheet_title)

                val initial_row = 1
                var end_row = 1

                val initial_col = 0
                var end_col = 0

                var init_statistics_col = -1

                val month_days_column_indexes: MutableMap<String, MutableMap<Int, Int>> = TreeMap(
                    kotlin.Comparator { ms1, ms2 -> Utils.getNumberFromMonthName(ms1).compareTo(Utils.getNumberFromMonthName(ms2))})


                /* HEADER CREATION */

                sheets_manager.writeValueToMergedCells(sheet_title, initial_row, initial_row, initial_col, initial_col + 6,
                    "Asistencia Tallados - ${sheet_title}")

                //Ignoring two last fields because there are fields used for reading sheets in another module
                val fields = activity!!.applicationContext.resources.getStringArray(R.array.sheet_fields).toList()
                    .subList(0,activity!!.applicationContext.resources.getStringArray(R.array.sheet_fields).size - 2)
                sheets_manager.writeValuesToRow(sheet_title, initial_row + 1, initial_col, initial_col + fields.size - 1,
                    fields)

                delay(5000)

                var start_month_col = initial_col + 7
                var next_col = initial_col + 7

                if (period == AttendancePeriod.WEEKLY) {
                    val start_date = Utils.getNearestDateOfWeekDay(period_value, viewModel.date_from)
                    val cal_start = Utils.getCalendar(start_date)


                    while (cal_start.time.compareTo(viewModel.date_to) <= 0) {
                        Log.i("EXPORTATT", "NEXT COL IND: $next_col")
                        val curr_month = cal_start.get(Calendar.MONTH)
                        val curr_day = cal_start.get(Calendar.DAY_OF_MONTH)
                        //sheets_manager.writeValueToCell(sheet_title, initial_row + 1, next_col, curr_day)

                        val month_name = Utils.getMonthNameFromNumber(curr_month)
                        if (month_days_column_indexes.containsKey(Utils.getMonthNameFromNumber(curr_month))) {
                            month_days_column_indexes[month_name]!![curr_day] = next_col
                        } else {
                            month_days_column_indexes[month_name] = TreeMap()
                            month_days_column_indexes[month_name]!![curr_day] = next_col
                            // (curr_day to next_col)
                        }

                        cal_start.add(Calendar.DATE, 7)

                        if (!curr_month.equals(cal_start.get(Calendar.MONTH))) {
                            sheets_manager.writeValueToMergedCells(
                                sheet_title, initial_row, initial_row, start_month_col, next_col,
                                Utils.getMonthNameFromNumber(curr_month)
                            )
                            val color = Utils.RGBAMapToGoogleColor(Utils.getRandomRGBAColorMap())
                            sheets_manager.setBackgroundColorOnCells(
                                sheet_title,
                                initial_row,
                                initial_row,
                                start_month_col,
                                next_col,
                                color
                            )
                            start_month_col = next_col + 1
                        }

                        next_col++
                    }

                    val days_values= mutableListOf<Any>()
                    month_days_column_indexes.entries.forEach { entry->
                        Log.i("ATTEXPRT","MONTH: "+entry.key)
                        entry.value.entries.forEach { day_entry ->
                            days_values.add(day_entry.key)
                        }
                    }
                    val entries_list= month_days_column_indexes.entries.toList()
                    val init_col= entries_list.first().value.entries.first().value
                    val end_col= entries_list.last().value.entries.last().value

                    sheets_manager.writeValuesToRow(sheet_title,2,init_col,end_col,days_values)

                }

                /*STATISTICS HEADER CREATION */

                init_statistics_col = next_col

                val results_fields =
                    activity!!.applicationContext.resources.getStringArray(R.array.sheet_results_fields).toList()
                sheets_manager.writeValuesToRow(
                    sheet_title,
                    initial_row,
                    init_statistics_col,
                    init_statistics_col + results_fields.size - 1,
                    results_fields
                )

                end_col = next_col + results_fields.size - 1

                Log.i("EXPORATT", "HEADER PRINT FINISHED")

                delay(10000)

                /* STUDENT INFO CREATION */

                val initial_st_row = initial_row + 2

                //TODO PASS PHOTO INFORMATION LINK (MAYBE LINK OF DRIVE)
                val students_info = ArrayList<List<Any>>()
                var i = 1
                viewModel.students_course?.map {
                    students_info.add(
                        listOf(
                            i,
                            it.full_name,
                            "N/A",
                            Utils.getDiffYears(it.dob, Date()),
                            it.address,
                            it.phone,
                            Utils.getFormatedDate(it.dob)
                        )
                    )
                    i++
                }

                end_row = initial_row + viewModel.students_course?.size!! + 2

                sheets_manager.writeValuesToCellRange(
                    sheet_title,
                    initial_st_row,
                    end_row,
                    initial_col,
                    initial_col + fields.size - 1,
                    students_info
                )

                Log.i("EXPORTATT", "STUDENTS LIST CREATED")

                delay(5000)


                /* ATTENDANCE TAKING CREATION */

                var students_att_data= mutableListOf<MutableList<Any>>()
                var students_stats_data = mutableListOf<List<Any>>()

                val first_att_date=Utils.getCalendar(viewModel.attendances_students_map!!.keys.first().date)
                val last_att_date= Utils.getCalendar(viewModel.attendances_students_map!!.keys.last().date)

                val first_month= Utils.getMonthNameFromNumber(first_att_date.get(Calendar.MONTH))
                val first_day= first_att_date.get(Calendar.DAY_OF_MONTH)
                val last_month= Utils.getMonthNameFromNumber(last_att_date.get(Calendar.MONTH))
                val last_day= last_att_date.get(Calendar.DAY_OF_MONTH)

                val init_records_col= month_days_column_indexes[first_month]!![first_day]!!
                val end_records_col= month_days_column_indexes[last_month]!![last_day]!!

                var student_row = initial_st_row

                viewModel.students_course?.map { student ->
                    val records_map = TreeMap<Attendance, AttendanceRecordStudent>(Comparator { att1, att2 -> att1.date.compareTo(att2.date) })
                    var justified_absences = 0
                    var total_attendances = 0
                    val att_data= mutableListOf<Any>()
                    val att_stats: List<Any>



                    viewModel.attendances_students_map?.forEach { entry ->
                        entry.value.find { record -> record.student!![0].equals(student) }?.let { record ->
                            records_map[entry.key] = record
                        }
                    }

                    records_map.forEach { entry ->

                        val record = entry.value

                        /*
                        val attendance_date = Utils.getCalendar(entry.key.date)

                        val month_name = Utils.getMonthNameFromNumber(attendance_date.get(Calendar.MONTH))
                        val month_day = attendance_date.get(Calendar.DAY_OF_MONTH)


                        val att_col_index = month_days_column_indexes[month_name]!![month_day]!!
                        */

                        var att_value = 0 //Absence will be considered as zero

                        when (record.status!![0].status) {
                            "present" -> att_value = 1
                            "sick", "unknown" -> justified_absences++
                        }

                        att_data.add(att_value)

                        total_attendances += att_value
                        // sheets_manager.writeValueToCell(sheet_title, student_row, att_col_index, att_value)
                    }

                    // adding empty values for attendances where the student doesn't have a record
                    // (because the student may was added later to list)
                    while(att_data.size < viewModel.attendances_students_map!!.size){
                        att_data.add(0,"")
                    }

                    val att_percentage =
                        (total_attendances.toFloat() / viewModel.attendances_students_map!!.keys.size.toFloat()) * 100.0f

                    /* sheets_manager.writeValuesToRow(
                        sheet_title, student_row, init_statistics_col,
                        init_statistics_col + results_fields.size - 1,
                        listOf(total_attendances, justified_absences, att_percentage)
                    ) */

                    att_stats= listOf(total_attendances, justified_absences, att_percentage)

                    students_att_data.add(att_data)
                    students_stats_data.add(att_stats)

                    student_row++
                }

                sheets_manager.writeValuesToCellRange(sheet_title,initial_st_row,end_row,init_records_col,end_records_col,
                    students_att_data)
                sheets_manager.writeValuesToCellRange(sheet_title,initial_st_row,end_row,init_statistics_col,
                    init_statistics_col + results_fields.size - 1,students_stats_data)

                Log.i("EXPORTATT", "ATTENDANCE CREATED")

                /* SHEET FORMATING */

                sheets_manager.setFontOnCells(sheet_title, initial_row, end_row, initial_col, end_col,"Calibri",11)
                sheets_manager.setBoldContentOnCells(sheet_title, initial_row, initial_row + 1, initial_col,
                    end_col + 1)
                sheets_manager.setBorderOnCells(sheet_title, initial_row, end_row, initial_col, end_col)
                sheets_manager.autoResizeCells(sheet_title, initial_col, end_col + 1, SheetsAPIWriterManager.COLUMN_DIM)

                Log.i("EXPORTATT", "STYLUS FINISHED")

                async(Dispatchers.Main){
                    viewModel.showSuccesDialog.value= true
                }
            } catch (ex :GoogleJsonResponseException){
                if(ex.details.code == 429){
                    async(Dispatchers.Main) {
                        viewModel.errorMessage =
                            "El servidor para generar los reportes se encuentra saturado, intente más tarde"
                    }
                }
                else{
                    async(Dispatchers.Main) {
                        viewModel.errorMessage = "Ha ocurrido un error inesperado, por favor intente más tarde"
                    }
                }
                async(Dispatchers.Main) {
                    viewModel.showErrorDialog.value = true
                }
            }finally {
                async(Dispatchers.Main) {
                    Log.i("EXPORTATT", "CLOSING MODAL")
                    viewModel.showExportingDialog.value= false
                }
            }
        }
    }

}
