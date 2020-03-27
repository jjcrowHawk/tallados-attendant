package org.cbe.talladosatendant.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import org.apache.poi.ss.util.NumberToTextConverter
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.databases.entities.StudentCourse
import org.cbe.talladosatendant.util.AttendanceRepository
import org.cbe.talladosatendant.util.SheetsReader
import org.cbe.talladosatendant.util.Utils
import java.math.BigDecimal
import java.util.*
import kotlin.math.roundToInt

class ImportStudentListViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: AttendanceRepository

  val _courses: MediatorLiveData<List<Course>>
  var courses: List<Course>? = null
  private var students:MutableLiveData<List<Student>> = MutableLiveData()
  var unassigned_students_count : MutableLiveData<Int> = MutableLiveData(0)
  var added_students= 0

  private val show_waiting_dialog= MutableLiveData<Boolean>(false)
  private val show_success_dialog= MutableLiveData<Boolean>(false)
  private val show_error_dialog= MutableLiveData<Boolean>(false)


  init {
	 val database= AttendanceDatabase.getDatabase(application,viewModelScope)
	 repository= AttendanceRepository(database)
	 _courses= MediatorLiveData()
	 _courses.addSource(repository.courses) { course_list ->
		Log.i("IMPORTSTVM","courses: $course_list")
		courses= course_list
	 }
  }

  //TODO SAVE STUDENT PICTURE

  suspend fun getStudentDataFromSheet(sheetsReader: SheetsReader, sheets_indexes:List<Int> = listOf(0), fields: List<String>){

	 val students_list= mutableListOf<Student>()

	 val (_, full_name,picture,age,address,phone,dob,name,last_name)= fields

	 sheets_indexes.forEach { sheet_index ->

		Log.i("IMPORTVM", "SEARCHING FOR HEADERS INDEXES")

		val fname_index = sheetsReader.findValueOnCellIndex(full_name, sheet_index)
		val name_index = sheetsReader.findValueOnCellIndex(name, sheet_index)
		val last_name_index = sheetsReader.findValueOnCellIndex(last_name, sheet_index)

		if(fname_index == null && name_index == null && last_name_index == null){
		  return
		}

		//We will assume that all headers are in the same row
		val header_row = fname_index?.row ?: name_index!!.row

		Log.i("IMPORTVM", "SEARCHING FOR ADDR")
		val address_index = sheetsReader.findValueOnCellIndex(address, sheet_index,start_row = header_row, end_row = header_row)
		Log.i("IMPORTVM", "SEARCHING FOR PHONE")
		val phone_index = sheetsReader.findValueOnCellIndex(phone, sheet_index,start_row = header_row, end_row = header_row)
		Log.i("IMPORTVM", "SEARCHING FOR DATE")
		val dob_index = sheetsReader.findValueOnCellIndex(dob, sheet_index,start_row = header_row, end_row = header_row) ?:
		  sheetsReader.findValueOnCellIndex("F. Naci.", sheet_index,start_row = header_row, end_row = header_row)

		val picture_index = sheetsReader.findValueOnCellIndex(picture, sheet_index,start_row = header_row, end_row = header_row)
		val age_index = sheetsReader.findValueOnCellIndex(age, sheet_index,start_row = header_row, end_row = header_row)


		Log.i("IMPORTVM", "HEADERS INDEXES GOT")

		//If fname_index is null we have to work with name and last_name fields
		fname_index?.let { fname_index ->
		  val col_indexes =
			 mutableListOf<Int>(fname_index.column)
		  address_index?.let { col_indexes.add(it.column) }
		  phone_index?.let { col_indexes.add(it.column) }
		  dob_index?.let { col_indexes.add(it.column) }
		  picture_index?.let { col_indexes.add(it.column) }
		  age_index?.let { col_indexes.add(it.column) }

		  val are_contiguous = Utils.checkElementsContiguity(col_indexes.toTypedArray())

		  //If fields are contiguous we can call getRowData instead of make requests per field
		  if (are_contiguous) {
			 val sorted_indexes = col_indexes.sorted()

			 for (row in (header_row + 1)..sheetsReader.getNumberOfRows(sheet_index)) {
				val student_data = sheetsReader.readRowValues(
				  sheet_index,
				  row,
				  sorted_indexes[0],
				  sorted_indexes[sorted_indexes.size - 1]
				)
				student_data?.let { student_data ->

				  Log.i("IMPORTVM", "SORTED INDEXES: $sorted_indexes")
				  Log.i("IMPORTVM", "STUDENT DATA: $student_data")

				  val st_name = student_data.getOrNull(sorted_indexes.indexOf(fname_index.column))
				  val st_address = student_data.getOrNull(sorted_indexes.indexOf(address_index?.column))
				  val st_phone = student_data.getOrNull(sorted_indexes.indexOf(phone_index?.column))
				  val st_dob = student_data.getOrNull(sorted_indexes.indexOf(dob_index?.column))
				  val st_pic = if (picture_index != null) student_data[sorted_indexes.indexOf(picture_index.column)] else ""
				  val st_age = if (age_index != null) student_data[sorted_indexes.indexOf(age_index.column)] else -1

				  //CHECKING IF THE VALUE GOT FROM ROW is not a repeated header
				  if (st_name != null && !fields.contains(st_name.toString())) {
					 val names = Utils.getNamesFromFullName(st_name.toString())
					 val date_dob = Utils.parseDateFromString(st_dob.toString())

					 val student = Student(
						name = names[1],
						last_name = names[0],
						address = st_address?.toString() ?: "",
						phone = if(st_phone != null) Utils.getPhoneNumberFromExpString(st_phone.toString()) else "",
						dob = date_dob ?: Date(),
						picture = st_pic?.toString() ?: ""
					 )

					 student.age = st_age?.toString()?.toDouble()?.roundToInt() ?: -1

					 students_list.add(student)
				  }
				}
			 }
		  } else {

			 for (row in (header_row + 1)..sheetsReader.getNumberOfRows(sheet_index)) {

				val st_name = sheetsReader.readCellValue(sheet_index, row, fname_index.column)
				val st_address = sheetsReader.readCellValue(sheet_index, row, address_index?.column ?: -1)
				val st_phone = sheetsReader.readCellValue(sheet_index, row, phone_index?.column ?: -1)
				val st_dob = sheetsReader.readCellValue(sheet_index, row, dob_index?.column ?: -1)
				val st_pic =
				  if (picture_index != null) sheetsReader.readCellValue(sheet_index, row, picture_index.column) else ""
				val st_age =
				  if (age_index != null) sheetsReader.readCellValue(sheet_index, row, age_index.column) else -1

				//CHECKING IF THE VALUE GOT FROM ROW is not a repeated header
				if (st_name != null && !fields.contains(st_name.toString())) {
				  val names = Utils.getNamesFromFullName(st_name.toString())
				  val date_dob = Utils.parseDateFromString(st_dob.toString())

				  val student = Student(
					 name = names[1],
					 last_name = names[0],
					 address = st_address?.toString() ?: "",
					 phone = if(st_phone != null) Utils.getPhoneNumberFromExpString(st_phone.toString()) else "",
					 dob = date_dob ?: Date(),
					 picture = st_pic?.toString() ?: ""
				  )
				  student.age = st_age?.toString()?.toDouble()?.roundToInt() ?: -1

				  students_list.add(student)
				}
			 }
		  }

		} ?: run {

		  val col_indexes = mutableListOf<Int>(name_index!!.column, last_name_index!!.column)
		  address_index?.let { col_indexes.add(it.column) }
		  phone_index?.let { col_indexes.add(it.column) }
		  dob_index?.let { col_indexes.add(it.column) }
		  picture_index?.let { col_indexes.add(it.column) }
		  age_index?.let { col_indexes.add(it.column) }
		  picture_index?.let { col_indexes.add(it.column) }
		  age_index?.let { col_indexes.add(it.column) }

		  val are_contiguous = Utils.checkElementsContiguity(col_indexes.toTypedArray())

		  //If fields are contiguous we can call getRowData instead of make requests per field
		  if (are_contiguous) {
			 val sorted_indexes = col_indexes.sorted()

			 for (row in (header_row + 1)..sheetsReader.getNumberOfRows(sheet_index)) {
				val student_data =
				  sheetsReader.readRowValues(
					 sheet_index,
					 row,
					 sorted_indexes[0],
					 sorted_indexes[sorted_indexes.size - 1]
				  )
				student_data?.let { student_data ->
				  val st_name = student_data[sorted_indexes.indexOf(name_index!!.column)]?.toString()
				  val st_last_name = student_data[sorted_indexes.indexOf(last_name_index!!.column)]?.toString()
				  val st_address = student_data.getOrNull(sorted_indexes.indexOf(address_index?.column))
				  val st_phone = student_data.getOrNull(sorted_indexes.indexOf(phone_index?.column))
				  val st_dob = student_data.getOrNull(sorted_indexes.indexOf(dob_index?.column))
				  val st_pic = if (picture_index != null) student_data[sorted_indexes.indexOf(picture_index.column)] else ""
				  val st_age = if (age_index != null) student_data[sorted_indexes.indexOf(age_index.column)] else -1

				  //CHECKING IF THE VALUE GOT FROM ROW is not a repeated header
				  if (st_name != null && st_last_name != null && !fields.contains(st_name)) {
					 val date_dob = Utils.parseDateFromString(st_dob.toString())

					 val student = Student(
						name = st_name,
						last_name = st_last_name,
						address = st_address?.toString() ?: "",
						phone = if(st_phone != null) Utils.getPhoneNumberFromExpString(st_phone.toString()) else "",
						dob = date_dob ?: Date(),
						picture = st_pic?.toString() ?: ""
					 )
					 student.age = st_age?.toString()?.toDouble()?.roundToInt() ?: -1

					 students_list.add(student)
				  }
				}
			 }
		  } else {
			 for (row in (header_row + 1)..sheetsReader.getNumberOfRows(sheet_index)) {

				val st_name = sheetsReader.readCellValue(sheet_index, row, name_index!!.column)
				val st_last_name = sheetsReader.readCellValue(sheet_index, row, last_name_index!!.column)
				val st_address = sheetsReader.readCellValue(sheet_index, row, address_index?.column ?: -1)
				val st_phone = sheetsReader.readCellValue(sheet_index, row, phone_index?.column ?: -1)
				val st_dob = sheetsReader.readCellValue(sheet_index, row, dob_index?.column ?: -1)
				val st_pic =
				  if (picture_index != null) sheetsReader.readCellValue(sheet_index, row, picture_index.column) else ""
				val st_age =
				  if (age_index != null) sheetsReader.readCellValue(sheet_index, row, age_index.column) else -1

				//CHECKING IF THE VALUE GOT FROM ROW is not a repeated header
				if (st_name != null && st_last_name != null && !fields.contains(st_name.toString())) {
				  val names = Utils.getNamesFromFullName(st_name.toString())
				  val date_dob = Utils.parseDateFromString(st_dob.toString())

				  val student = Student(
					 name = st_name.toString(),
					 last_name = st_last_name.toString(),
					 address = st_address?.toString() ?: "",
					 phone = if(st_phone != null) Utils.getPhoneNumberFromExpString(st_phone.toString()) else "",
					 dob = date_dob ?: Date(),
					 picture = st_pic?.toString() ?: ""
				  )
				  student.age = st_age?.toString()?.toDouble()?.roundToInt() ?: -1

				  students_list.add(student)
				}
			 }
		  }

		}
	 }

	 Log.i("IMPORTSTUDENTVM","STUDENTS DONE: "+ students_list)

	 this.students.postValue(students_list)

  }

  fun assignCoursesToStudentsByAge(): Map<Course,MutableList<Student>> {

	 val map_course_students = mutableMapOf<Course, MutableList<Student>>()
	 val unassigned_course = Course(-1, "Curso No Asignado")

	 students.value?.forEach { student ->
		val student_age =  if (student.age != -1) student.age else Utils.getDiffYears(student.dob, Date())
		val found_course = courses?.find { course ->
		  student_age >= course.min_year_student && student_age <= course.max_year_student
		}

		found_course?.let {
		  if (map_course_students.containsKey(found_course)) {
		  map_course_students[found_course]!!.add(student)
		  } else {
			 map_course_students[found_course] = mutableListOf(student)
		  }
		} ?: run {
		  if (map_course_students.containsKey(unassigned_course)) {
			 map_course_students[unassigned_course]!!.add(student)
		  } else {
			 map_course_students[unassigned_course] = mutableListOf(student)
		  }
		}
	 }

	 return map_course_students
  }

  fun getStudentsSortedByUnassignedFirst(map_course_students: Map<Course,MutableList<Student>>) : List<Student>{
	 val new_students= students.value!!.toMutableList()
	 val unassigned_students= students.value!!.filter { student ->
		map_course_students.keys.find { course -> map_course_students[course]!!.contains(student)}?.course_id == -1
	 }

	 unassigned_students?.forEach { student ->
		new_students.remove(student)
		new_students.add(0,student)
	 }

	 this.unassigned_students_count.value = unassigned_students.size

	 return new_students

  }

  suspend fun importStudents(map_students:Map<Course,List<Student>>){
	 if(map_students.isNotEmpty()){
		map_students.forEach { entry ->
		  val course = entry.key
		  val student_list= entry.value

		  val new_ids= repository.addStudentsRecords(student_list)
		  val added_ids= new_ids.filter { id -> id != (-1).toLong() }
		  this.added_students+= added_ids.size

		  if(course.course_id != -1){
			 val sc_list= added_ids.map {
				  st_id -> StudentCourse(student = st_id.toInt(),course = course.course_id, active = true)
			 }

			 repository.addStudentCourseRowList(sc_list)
		  }

		  /*
		  entry.value.forEach{ student ->
			 Log.i("IMPORTSTUDENTVM","Adding student $student")

			 val new_id= repository.addStudentToDatabase(student)

			 if(new_id >0){
				Log.i("IMPORTSTUDENTVM","Student added $student")
				added_students++

				if(course.course_id != -1) {
				  val student_course = StudentCourse(student = new_id.toInt(), course = course.course_id, active = true)
				  val id = repository.addStudentCourseRow(student_course)
				  Log.i("IMPORTSTUDENTVM","Student added $student to course with id $id")
				}
			 }
		  } */
		}
	 } else{
		val new_ids= repository.addStudentsRecords(this.students.value!!)
		val added_ids= new_ids.filter { id -> id != (-1).toLong() }
		this.added_students+= added_ids.size
		/*
		this.students.value?.forEach { student->
		  Log.i("IMPORTSTUDENTVM","Adding student $student")
		  val new_id= repository.addStudentToDatabase(student)
		  if(new_id >0){
			 Log.i("IMPORTSTUDENTVM","Student added $student")
			 added_students++
		  }
		}
		*/
	 }

	 Log.i("IMPORTVM","FINISHED TO ADD STUDENTS TO DATABASE")
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

  fun getStudentsLD(): LiveData<List<Student>>{
	 return this.students
  }

  private operator fun <E> List<E>.component6(): E {
	 return get(5)
  }

  private operator fun <E> List<E>.component7(): E {
	 return get(6)
  }

  private operator fun <E> List<E>.component8(): E {
	 return get(7)
  }

  private operator fun <E> List<E>.component9(): E {
	 return get(8)
  }
}
