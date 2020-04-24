package org.cbe.talladosatendant.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.AttendanceStatus
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.databases.entities.StudentCourse
import org.cbe.talladosatendant.pojo.RecordWithAttendance
import org.cbe.talladosatendant.util.AttendanceRepository
import java.util.*

class ViewStudentViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: AttendanceRepository
  val students: LiveData<List<Student>>
  private var statuses: List<AttendanceStatus>? = null
  var selected_student: Student? = null
  val map_student_courses : MutableMap<StudentCourse,Course> = TreeMap(kotlin.Comparator { sc1, sc2 -> sc1.created_date.compareTo(sc2.created_date)})
  lateinit var attendances: MutableMap<String,List<RecordWithAttendance>>

  private val show_waiting_dialog= MutableLiveData<Boolean>(false)
  init {
	 val database= AttendanceDatabase.getDatabase(application,viewModelScope)
	 repository= AttendanceRepository(database)
	 students= repository.students
	 MediatorLiveData<List<AttendanceStatus>>().addSource(repository.getAttendanceStatuses(), Observer { statuses ->
		this.statuses = statuses
	 })
  }

  suspend fun getStudentCourseInformation(){
	 map_student_courses.clear()
	 selected_student?.let { student ->
		val sc_list= repository.getStudentCourseByStudentSync(student.student_id)
		sc_list.forEach { studentcourse ->
		  val course = repository.getCourseSync(studentcourse.course)
		  map_student_courses[studentcourse] = course
		}
	 }
  }

  suspend fun getStudentAttendances(context: Context){
	 this.attendances.clear()
	 selected_student?.let {student ->
		Log.i("VIEWSTUDENTVM","ENTERED TO GET ATTENDANCES!")
		val attendances_list =  repository.getRecordsFromStudentSync(student.student_id)
		statuses?.forEach { status ->
		 this.attendances[status.status] = attendances_list.filter { record_attendance ->
			record_attendance.status!![0].status == status.status
		 }
		}
	 }
  }

}
