package org.cbe.talladosatendant.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.databases.entities.StudentCourse
import org.cbe.talladosatendant.util.AttendanceRepository
import org.cbe.talladosatendant.util.Utils

class EditStudentViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: AttendanceRepository
  val students: LiveData<List<Student>>
  val courses: MutableLiveData<List<Course>>
  val _courses: MediatorLiveData<List<Course>>
  var student_pic_uri: Uri? = null
  var is_valid_form: Boolean= false

  lateinit var st_name: String
  lateinit var st_lname: String
  lateinit var st_address: String
  lateinit var st_phone: String
  lateinit var st_dob: String
  var selected_course: Course? = null
  var selected_student: Student? = null
  var student_courses: List<StudentCourse>? = null

  private val show_waiting_dialog= MutableLiveData<Boolean>(false)
  private val show_success_dialog= MutableLiveData<Boolean>(false)
  private val show_error_dialog= MutableLiveData<Boolean>(false)
  private val show_delete_success_dialog= MutableLiveData<Boolean>(false)
  private val show_delete_error_dialog= MutableLiveData<Boolean>(false)

  var showAgeExceedError: MutableLiveData<Boolean> = MutableLiveData(false)

  init {
	 val database= AttendanceDatabase.getDatabase(application,viewModelScope)
	 repository= AttendanceRepository(database)
	 students= repository.students
	 _courses= MediatorLiveData()
	 courses= MutableLiveData()
	 this._courses.addSource(repository.courses){ course_list ->
		Log.i("EDITSTUDENT VM","courses: ${course_list}")
		courses.value = course_list
	 }
  }

  suspend fun setCourseOnDataSource(student_id: Int){
	 val sc_list= repository.getStudentCourseByStudentSync(student_id)
	 if(sc_list.size > 0){
		student_courses= sc_list
		val course_id = sc_list[0].course
		reorderCoursesWithStudentCourse(course_id)
	 }
  }

  fun reorderCoursesWithStudentCourse(course_id:Int) : Boolean{
	 courses.value!!.forEach { course ->
		Log.i("EDITSTUDENT VM","${course.min_year_student} ${course.max_year_student}")
	 }
	 val found_course= courses.value?.find {course -> course.course_id == course_id }

	 found_course?.let {
		val newList= courses.value!!.toMutableList()
		newList.remove(found_course)
		newList.add(0,found_course)
		courses.postValue(newList)
		return true
	 } ?: kotlin.run { showAgeExceedError.value= true }
	 return false
  }

  suspend fun updateStudent() : Boolean{
	 val dob= Utils.parseDateFromString(st_dob)

	 val new_student= this.selected_student!!.copy(
		name = st_name,
		last_name = st_lname,
		address = st_address,
		phone = st_phone,
		dob = dob!!
	 )

	 val rows= repository.updateStudentOnDatabase(new_student)

	 // DELETING STUDENT COURSE RECORDS
	 student_courses?.let{
		val list = it.map { sc -> sc.copy(active = false) }
		repository.updateStudentCoursesRecords(list)
	 }

	 //CREATING NEW STUDENT COURSE RECORDS
	 val new_sc= StudentCourse(student = selected_student!!.student_id, course = selected_course!!.course_id)
	 repository.addStudentCourseRow(new_sc)

	 return rows > 0
  }

  suspend fun addPictureToStudent(student_id:Int,picture_path:String): Int{
	 val student= repository.getStudentSync(student_id)
	 val student_wpic= student.copy(picture = picture_path)
	 return repository.updateStudentOnDatabase(student_wpic)
  }

  suspend fun deleteSelectedStudent(): Boolean{
	 this.selected_student?.let{student ->

		val new_student= student.copy(active = false)
		val rows= repository.updateStudentOnDatabase(new_student)

		// DELETING STUDENT COURSE RECORDS
		student_courses?.let{
		  val list = it.map { sc -> sc.copy(active = false) }
		  repository.updateStudentCoursesRecords(list)
		}

		return rows > 0

	 }

	 return false
  }

  fun getShowWaitingDialog(): LiveData<Boolean> {
	 return show_waiting_dialog
  }

  fun setShowWaitingDialog(value:Boolean){
	 show_waiting_dialog.value= value
  }

  fun getShowSuccessDialog(): LiveData<Boolean> {
	 return show_success_dialog
  }

  fun setShowSuccessDialog(value:Boolean){
	 show_success_dialog.value= value
  }

  fun getShowErrorDialog(): LiveData<Boolean> {
	 return show_error_dialog
  }

  fun setShowErrorDialog(value:Boolean){
	 show_error_dialog.value= value
  }

  fun getShowSuccessDeleteDialog(): LiveData<Boolean> {
	 return show_delete_success_dialog
  }

  fun setShowSuccessDeleteDialog(value:Boolean){
	 show_delete_success_dialog.value= value
  }

  fun getShowErrorDeleteDialog(): LiveData<Boolean> {
	 return show_delete_error_dialog
  }

  fun setShowErrorDeleteDialog(value:Boolean){
	 show_delete_error_dialog.value= value
  }
}
