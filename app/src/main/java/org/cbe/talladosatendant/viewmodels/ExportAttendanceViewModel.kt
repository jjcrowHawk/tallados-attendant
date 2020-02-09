package org.cbe.talladosatendant.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.pojo.AttendanceSectionedModel
import org.cbe.talladosatendant.pojo.CourseAttendance
import org.cbe.talladosatendant.util.AttendanceRepository
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Attendance
import org.cbe.talladosatendant.pojo.AttendanceRecordStudent
import org.cbe.talladosatendant.util.Utils
import java.util.*

class ExportAttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository
    //private lateinit var attendances: LiveData<List<CourseAttendance>>
    //var attendances_sectioned_list: MediatorLiveData<List<AttendanceSectionedModel>> = MediatorLiveData()
    val courses: LiveData<List<Course>>
    val export_types : List<String>
    lateinit var date_from : Date
    lateinit var date_to: Date
    var date_from_label: MutableLiveData<String> = MutableLiveData("")
    var date_to_label: MutableLiveData<String> = MutableLiveData("")
    var selected_course: Course? = null

    val showSelectDateButtons : MutableLiveData<Boolean> = MutableLiveData(true)
    val showExportingDialog: MutableLiveData<Boolean> = MutableLiveData(false)
    val showNoDataFoundDialog: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        val database= AttendanceDatabase.getDatabase(application, viewModelScope)
        repository= AttendanceRepository(database)
        courses= repository.courses
        export_types= listOf(
            application.getString(R.string.all_year),
            application.getString(R.string.all_month),
            application.getString(R.string.custom)
        )
    }

    fun setDateRange(type:String){
        when(type){
             export_types[0]->{
                 val c_year = Calendar.getInstance().get(Calendar.YEAR)

                 val cal_from= Calendar.getInstance()
                 cal_from.set(c_year,Calendar.JANUARY,1)
                 val cal_to= Calendar.getInstance()
                 cal_to.set(c_year,Calendar.DECEMBER,31)

                 showSelectDateButtons.value= false

                 date_from= cal_from.time
                 date_to= cal_to.time
                 date_from_label.value = Utils.getFormattedLatinDate(cal_from.time)
                 date_to_label.value = Utils.getFormattedLatinDate(cal_to.time)
            }
            export_types[1]->{
                val c_year = Calendar.getInstance().get(Calendar.YEAR)
                val c_month = Calendar.getInstance().get(Calendar.MONTH)
                val end_day= Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)


                val cal_from= Calendar.getInstance()
                cal_from.set(c_year,c_month,1)
                val cal_to= Calendar.getInstance()
                cal_to.set(c_year,c_month,end_day)

                showSelectDateButtons.value= false

                date_from= cal_from.time
                date_to= cal_to.time
                date_from_label.value = Utils.getFormattedLatinDate(cal_from.time)
                date_to_label.value = Utils.getFormattedLatinDate(cal_to.time)
            }
            export_types[2]->{
                date_from_label.value = "Seleccione fecha"
                date_to_label.value = "Seleccione Fecha"
                showSelectDateButtons.value= true
            }
        }

    }

    suspend fun getAttendancesData(): Map<Attendance,List<AttendanceRecordStudent>>?{
        val attendances : List<Attendance> = repository.getAttendancesFromCourseWithDateRange(selected_course!!.course_id,
            date_from,
            date_to)
        Log.i("VMEXPRT","attendances: $attendances")

        if(attendances.isNotEmpty()) {
            val map_attendance_record: MutableMap<Attendance, List<AttendanceRecordStudent>> =
                TreeMap(Comparator { a1, a2 -> a1.date.compareTo(a2.date) })
            for (attendance in attendances) {
                val records = repository.getRecordsFromAttendance(attendance.attendance_id)
                records.let { map_attendance_record[attendance] = records }
            }
            map_attendance_record.forEach { e ->
                Log.i("VMEXPRT", "Attendance ${e.key.date} with records: ${e.value}")
            }

            return map_attendance_record
        }

        return null
    }

    fun exportAttendanceToXLS(){

    }

}
