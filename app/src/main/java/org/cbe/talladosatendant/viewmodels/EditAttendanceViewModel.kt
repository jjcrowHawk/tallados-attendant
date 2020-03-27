package org.cbe.talladosatendant.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.*
import org.cbe.talladosatendant.pojo.AttendanceRecordStudent
import org.cbe.talladosatendant.util.AttendanceRepository
import org.cbe.talladosatendant.util.Utils
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class EditAttendanceViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: AttendanceRepository

  val courses: LiveData<List<Course>>
  val attendances_statuses: LiveData<List<AttendanceStatus>>
  private var _attendances_statuses: List<AttendanceStatus> = ArrayList<AttendanceStatus>()
  var attendances_dates: MutableList<String>? = mutableListOf<String>()
  val attendances: MediatorLiveData<List<Attendance>>
  private lateinit var students: List<Student>
  lateinit var students_records: MediatorLiveData<List<AttendanceRecordStudent>>
  lateinit var selected_attendance: Attendance

  var show_saving_dialog: MutableLiveData<Boolean> = MutableLiveData(false)
  var show_success_dialog: MutableLiveData<Boolean> = MutableLiveData(false)
  var show_comment_dialog: MutableLiveData<Boolean> = MutableLiveData(false)

  init {
	 val database= AttendanceDatabase.getDatabase(application,viewModelScope)
	 this.repository= AttendanceRepository(database)
	 this.courses= repository.courses
	 this.attendances_statuses= repository.getAttendanceStatuses()
	 this.attendances = MediatorLiveData()
	 this.students_records= MediatorLiveData()

  }

  fun populateAttendanceDates(course_id:Int){
	 attendances_dates?.let { it.clear() }
	 //this.attendances = repository.getUntakenAttendancesFromCourse(course_id)

	 val start_date= Utils.createDate(1,0,Calendar.getInstance().get(Calendar.YEAR))
	 val end_date= Utils.createDate(31,11,Calendar.getInstance().get(Calendar.YEAR))
	 this.attendances.addSource(repository.getTakenAttendancesFromCourse(course_id,start_date,end_date)){ attendances ->
		Log.i("EDITATTVM","attendances $attendances")
		this.attendances.value= attendances
	 }
  }

  fun getStudentsRecordsFromCourse(){

	 students_records.addSource(repository.getStudentsFromCourse(this.selected_attendance.course), Observer { students->
		this.students= students
		students_records.addSource(repository.getAttendanceRecordsFromAttendance(this.selected_attendance.attendance_id),
		  Observer { records ->
			 // We collect the students that are new and don't have a record of the attendance
			 val new_students= this.students.filter {student ->
				records.find {record -> record.student!![0].equals(student) } == null
			 }
			 val records = records.toMutableList()
			 new_students.forEach { st ->
				val record= AttendanceRecordStudent()
				record.student= listOf(st)
				records.add(record)
			 }

			 students_records.value= records
		  })
	 })
  }

  fun saveStudentsAttendanceToDatabase(student_status_map: MutableMap<Student,String>, attendance: Attendance, comment: String): Boolean{
		this.show_saving_dialog.value = true
		Log.i("EDITATTENDANCE VM","${_attendances_statuses} ${_attendances_statuses.size}")
		val records : MutableList<AttendanceRecord> = ArrayList()

	 	this.students_records.value?.map { st_record ->
		  val student_status=  student_status_map[st_record.student!![0]]

		  try {
			 val att_status = _attendances_statuses.single { a_status -> a_status.status.equals(student_status) }

			 records.add(
				AttendanceRecord(record_id =st_record.record!!.record_id, student = st_record.student!![0].student_id,
				  attendance = attendance.attendance_id, status = att_status.id)
			 )
		  } catch(e: Exception){
			 e.printStackTrace()
			 return false
		  }
		}
		viewModelScope.launch {
			 val rows_updated= repository.updateAttendanceRecords(records)
			 val new_attendance = attendance.copy(observations = comment)
			 val row_att= repository.updateAttendance(new_attendance)
			 if(rows_updated >0 && row_att > 0){
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

  fun setAttendanceStatuses(value:List<AttendanceStatus>){
	 this._attendances_statuses= value
  }

}
