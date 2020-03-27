package org.cbe.talladosatendant.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.databases.entities.StudentCourse
import org.cbe.talladosatendant.util.AttendanceRepository
import java.util.*

class ViewStudentViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: AttendanceRepository
  val students: LiveData<List<Student>>
  var selected_student: Student? = null
  val map_student_courses : MutableMap<StudentCourse,Course> = TreeMap(kotlin.Comparator { sc1, sc2 -> sc1.created_date.compareTo(sc2.created_date)})

  private val show_waiting_dialog= MutableLiveData<Boolean>(false)
  init {
	 val database= AttendanceDatabase.getDatabase(application,viewModelScope)
	 repository= AttendanceRepository(database)
	 students= repository.students

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

  suspend fun getStudentAttendances(){

  }

}
