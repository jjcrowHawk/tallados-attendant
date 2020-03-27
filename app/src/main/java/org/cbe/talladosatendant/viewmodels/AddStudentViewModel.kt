package org.cbe.talladosatendant.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.databases.entities.StudentCourse
import org.cbe.talladosatendant.util.AttendanceRepository
import org.cbe.talladosatendant.util.Utils

class AddStudentViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: AttendanceRepository
  val courses: MutableLiveData<List<Course>>
  val _courses: MediatorLiveData<List<Course>>
  var student_pic_uri: Uri? = null
  var is_valid_form: Boolean= false

  lateinit var st_name: String
  lateinit var st_lname: String
  lateinit var st_address: String
  lateinit var st_phone: String
  lateinit var st_dob: String
  lateinit var selected_course: Course

  private val show_waiting_dialog= MutableLiveData<Boolean>(false)
  private val show_success_dialog= MutableLiveData<Boolean>(false)
  private val show_error_dialog= MutableLiveData<Boolean>(false)

  var showAgeExceedError: MutableLiveData<Boolean> = MutableLiveData(false)

  init {
	 val database= AttendanceDatabase.getDatabase(application,viewModelScope)
	 repository= AttendanceRepository(database)
	 //_courses= repository.courses
	 _courses= MediatorLiveData()
	 courses= MutableLiveData()
	 this._courses.addSource(repository.courses){ course_list ->
		Log.i("ADDSTUDENT VM","courses: ${course_list}")
		courses.value = course_list
	 }
  }

  fun reorderCoursesWithStudentAge(student_age:Int) : Boolean{
	 courses.value!!.forEach {course ->
		Log.i("ADDSTUDENT VM","${course.min_year_student} ${course.max_year_student}")
	 }
	 val found_course= courses.value?.find {course ->
		student_age >= course.min_year_student && student_age <= course.max_year_student
	 }

	 found_course?.let {
		val newList= courses.value!!.toMutableList()
		newList.remove(found_course)
		newList.add(0,found_course)
		courses.value= newList
		return true
	 } ?: kotlin.run { showAgeExceedError.value= true }
	 return false
  }

  suspend fun addStudent() : Long{
	 val dob= Utils.parseDateFromString(st_dob)

	 val new_student= Student(
		name = st_name,
		last_name = st_lname,
		address = st_address,
		phone = st_phone,
		dob = dob!!
	 )

	 val new_id= repository.addStudentToDatabase(new_student)

	 if(new_id >0){
		val student_course= StudentCourse(student = new_id.toInt(), course = selected_course.course_id, active = true)
		repository.addStudentCourseRow(student_course)
	 }

	 return new_id
  }

  suspend fun addPictureToStudent(student_id:Int,picture_path:String): Int{
	 val student= repository.getStudentSync(student_id)
	 val student_wpic= student.copy(picture = picture_path)
	 return repository.updateStudentOnDatabase(student_wpic)
  }

  fun getShowWaitingDialog(): LiveData<Boolean>{
	 return show_waiting_dialog
  }

  fun setShowWaitingDialog(value:Boolean){
	 show_waiting_dialog.value= value
  }

  fun getShowSuccessDialog(): LiveData<Boolean>{
	 return show_success_dialog
  }

  fun setShowSuccessDialog(value:Boolean){
	 show_success_dialog.value= value
  }

  fun getShowErrorDialog(): LiveData<Boolean>{
	 return show_error_dialog
  }

  fun setShowErrorDialog(value:Boolean){
	 show_error_dialog.value= value
  }


}
