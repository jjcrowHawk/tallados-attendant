package org.cbe.talladosatendant.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.*
import org.cbe.talladosatendant.util.AttendanceRepository
import java.lang.Exception

class TakeAttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    val courses: LiveData<List<Course>>
    val attendances_statuses: LiveData<List<AttendanceStatus>>
    var _attendances_statuses: List<AttendanceStatus> = ArrayList<AttendanceStatus>()
    var untaken_dates: MutableList<String>? = mutableListOf<String>()
    lateinit var untaken_attendances : LiveData<List<Attendance>>
    lateinit var students: LiveData<List<Student>>

    var show_saving_dialog: MutableLiveData<Boolean> = MutableLiveData(false)
    var show_success_dialog: MutableLiveData<Boolean> = MutableLiveData(false)
    var show_comment_dialog: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        val database= AttendanceDatabase.getDatabase(application,viewModelScope)
        repository= AttendanceRepository(database)
        courses= repository.courses
        attendances_statuses= repository.getAttendanceStatuses()
    }

    fun populateUnTakenAttendanceDates(course_id:Int){
        untaken_dates?.let { it.clear() }
        untaken_attendances = repository.getUntakenAttendancesFromCourse(course_id)
    }

    fun getStudentsFromCourse(course_id: Int){
        students= repository.getStudentsFromCourse(course_id)
    }

    fun saveStudentsAttendanceToDatabase(student_status_map: MutableMap<Student,String>, attendance: Attendance,comment: String): Boolean{
        this.show_saving_dialog.value = true
        Log.i("ATTENDANCE VM","${_attendances_statuses} ${_attendances_statuses.size}")
        val records : MutableList<AttendanceRecord> = ArrayList<AttendanceRecord>()
        for(entry in student_status_map.entries){
            Log.i("AttendanceMap","student ${entry.key.name} ${entry.key.last_name}: ${entry.value}}")
            try {
                val att_status = _attendances_statuses.single { a_status -> a_status.status.equals(entry.value) }
                records.add(
                    AttendanceRecord(
                        student = entry.key.student_id,
                        attendance = attendance.attendance_id,
                        status = att_status!!.id
                    )
                )
            } catch(e: Exception){
                e.printStackTrace()
                return false
            }
        }
        viewModelScope.launch {
            val ids= repository.instertAttendanceRecords(records)
            val new_attendance = attendance.copy(taken = true,observations = comment)
            val row_att= repository.updateAttendance(new_attendance)
            if(ids != null && !ids.isEmpty() && row_att > 0){
                Log.i("TAKEATTENDANCE","succesful saving records!")
                show_saving_dialog.value = false
                show_success_dialog.value = true
            }
            else{
                Log.i("TAKEATTENDANCE","error saving records!")
            }

        }
        return true
    }
}
