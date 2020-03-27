package org.cbe.talladosatendant.util


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginTop
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_take_attendance.*
import kotlinx.android.synthetic.main.framgent_student_data.*

import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.databases.entities.StudentCourse
import org.cbe.talladosatendant.viewmodels.SharedViewModel
import java.lang.Exception
import java.util.*

// TODO: Rename parameter arguments, choose names that match

/**
 * A simple [Fragment] subclass.
 *
 */
class StudentDataFragment : Fragment() {

  val added_views= mutableListOf<View>()

  override fun onCreateView(
	 inflater: LayoutInflater, container: ViewGroup?,
	 savedInstanceState: Bundle?
  ): View? {
	 // Inflate the layout for this fragment
	 val view= inflater.inflate(R.layout.framgent_student_data, container, false)

	 return view
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
	 super.onActivityCreated(savedInstanceState)

	 val shared_view_model = activity?.run {
		ViewModelProvider(this).get(SharedViewModel::class.java)
	 } ?: throw Exception("Invalid Activity")

	 shared_view_model.getStudentDataLD().observe(this.viewLifecycleOwner, Observer { data ->
		val student= data.get("data") as Student
		val student_courses = data.get("courses_history") as TreeMap<StudentCourse, Course>
		val sc_keys= student_courses.keys
		val active_courses_map= student_courses.filterKeys { sc -> sc.active }

		tv_name_st_data.text = student.name
		tv_lname_st_data.text= student.last_name
		tv_dob_st_data.text= Utils.getFormatedDate(student.dob)
		tv_age_st_data.text= "${Utils.getDiffYears(student.dob, Date())}"
		tv_address_st_data.text= student.address
		tv_phone_st_data.text= student.phone
		tv_course_st_data.text= active_courses_map.entries.first().value.name

		Log.i("STUDENTDATA","REMOVING VIEWS: $added_views : ${added_views.size}")
		added_views.forEach { view -> parent_layout_student_data.removeView(view) }
		added_views.clear()

		Log.i("STUDENTDATA","HISTORIAL OF ${student.name}: ${sc_keys.size}")

		sc_keys.forEach { sc ->
		  //val tv_date= TextView(context)
		  val tv_course= TextView(context)

		  //tv_date.text= Utils.getFormatedDate(sc.created_date)
		  //tv_date.textSize= 14.0f

		  tv_course.text= "⬤ ${student_courses[sc]!!.name}  ->  ${Utils.getFormatedDate(sc.created_date)}"
		  tv_course.textSize= 16.0f

		  val container= LinearLayout(context)
		  val params =LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
		  params.topMargin= 17
		  params.marginStart = 35
		  container.layoutParams= params
		  container.orientation= LinearLayout.HORIZONTAL

		  //container.addView(tv_date)
		  container.addView(tv_course)

		  parent_layout_student_data.addView(container)
		  added_views.add(container)
		}

	 })
  }

}
