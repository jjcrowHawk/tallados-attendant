package org.cbe.talladosatendant.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.util.Utils
import org.cbe.talladosatendant.viewmodels.ImportStudentListViewModel
import java.util.*
import java.util.function.Predicate
import android.widget.AdapterView.OnItemSelectedListener as OnItemSelectedListener


class StudentImportAdapter(private val context: Context,private var students:List<Student>,private val courses:List<Course>,private val viewmodel: ImportStudentListViewModel) : RecyclerView.Adapter<StudentImportAdapter.ViewHolder>() {

  private var map_course_students: Map<Course,MutableList<Student>> = mutableMapOf()
  private var isCourseSelectorVisible: Boolean= false

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

	 val layout_parent: LinearLayout= view.findViewById(R.id.item_import_student_layout_parent)
	 val iv_student : ImageView= view.findViewById(R.id.iv_student_image_sii)
	 val tv_student_name: TextView= view.findViewById(R.id.tv_student_name_sii)
	 val tv_student_age: TextView= view.findViewById(R.id.tv_student_age_sii)
	 val spinner_course: Spinner = view.findViewById(R.id.spinner_course_selected_sii)
	 var spinner_lock= true
	 //var previous_spinner_selection : Course? = null

	 fun bind(item:Student,position:Int){
		//TODO MATCH IMAGE WITH STUDENT

		spinner_lock= true

		val student_age = if(item.age != -1) item.age else Utils.getDiffYears(item.dob, Date())
		tv_student_name.text= item.full_name
		tv_student_age.text= "${student_age} años"
		if(isCourseSelectorVisible){
		  showCoursesWithSelectedCourseByStudentAge(item)
		  spinner_course.visibility= View.VISIBLE
		}
		else{
		  layout_parent.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
		  spinner_course.visibility= View.GONE
		}

		spinner_course.onItemSelectedListener= object : OnItemSelectedListener{
		  override fun onNothingSelected(parent: AdapterView<*>?) {
			 TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		  }

		  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
			 Log.i("STIMPORTADAPTER", "SELECTED IN POS: $position")
			 val course = parent?.getItemAtPosition(position) as Course

			 if(spinner_lock){
				spinner_lock = false
				//previous_spinner_selection= course
			 }
			 else{
				if (course.course_id != -1) {
				  layout_parent.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
				} else {
				  layout_parent.setBackgroundColor(ContextCompat.getColor(context, R.color.absentState))
				}

				// Searching on map to delete records of student
				map_course_students.forEach { entry ->
				  val index = entry.value.indexOf(item)
				  if (index != -1) {
					 entry.value.removeAt(index)
					 if (entry.key.course_id == -1 && viewmodel.unassigned_students_count.value!! > 0) {
						viewmodel.unassigned_students_count.value = viewmodel.unassigned_students_count.value!! - 1
					 }
				  }
				}

				map_course_students[course]?.add(item)
				Log.i("STIMPORTADAPTER", "Student $item added to $course with list: ${map_course_students[course]}")
				//previous_spinner_selection= course
			 }
		  }


		}
	 }

	 private fun showCoursesWithSelectedCourseByStudentAge(student:Student){

		if(map_course_students.isEmpty()){
		  return
		}

		val found_course = map_course_students.keys.find { course ->  map_course_students[course]?.contains(student) ?: false }
		val new_courses= courses.toMutableList()
		//val student_age= if(student.age != -1) student.age else Utils.getDiffYears(student.dob, Date())
		//

		/*val found_course= new_courses.find {course ->
		  student_age >= course.min_year_student && student_age <= course.max_year_student
		}*/

		found_course?.let {
		  if(found_course.course_id != -1) {
			 new_courses.remove(found_course)
			 new_courses.add(0, found_course)
			 this.layout_parent.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary))
		  }
		  else{
			 val unassigned_course= Course(-1,"Curso No Asignado")
			 new_courses.add(0,unassigned_course)
			 this.layout_parent.setBackgroundColor(ContextCompat.getColor(context,R.color.absentState))
		  }
		} /*?: run {
		  val unassigned_course= Course(-1,"Curso No Asignado")
		  new_courses.add(0,unassigned_course)
		  if(map_course_students.containsKey(unassigned_course)){
			 map_course_students[unassigned_course]?.add(student)
		  } else {
			 map_course_students[unassigned_course]= mutableListOf(student)
		  }
		}*/

		spinner_course.adapter= ArrayAdapter<Course>(context,R.layout.spinner_select,new_courses)
		(spinner_course.adapter as ArrayAdapter<*>).setDropDownViewResource(R.layout.spinner_dropdown)

	 }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
	 val layoutInflater= LayoutInflater.from(context)
	 return ViewHolder(layoutInflater.inflate(R.layout.student_import_item,parent,false))
  }

  override fun getItemCount(): Int {
	 return students?.size ?: 0
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
	 val item = students[position]
	 holder.bind(item,position)
  }

  fun getCourseStudentsMap(): Map<Course,List<Student>>{
	 return this.map_course_students
  }

  fun setCourseStudentsMap(map: Map<Course,MutableList<Student>>){
	 this.map_course_students= map
	 this.notifyDataSetChanged()
  }

  fun setItems(items:List<Student>){
	 this.students= items
	 this.notifyDataSetChanged()
  }

  fun clearItems(){
	 this.students= emptyList()
	 this.map_course_students= emptyMap()
	 this.notifyDataSetChanged()
  }

  fun changeCourseSpinnerItemsVisibility(value:Boolean){
	 this.isCourseSelectorVisible= value
	 this.notifyDataSetChanged()
  }

}