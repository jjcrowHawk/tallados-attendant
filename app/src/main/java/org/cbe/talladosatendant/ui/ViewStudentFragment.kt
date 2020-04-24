package org.cbe.talladosatendant.ui

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TabHost
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*

import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.util.StudentDataFragment
import org.cbe.talladosatendant.viewmodels.SharedViewModel
import org.cbe.talladosatendant.viewmodels.ViewStudentViewModel
import kotlin.coroutines.CoroutineContext

class ViewStudentFragment : Fragment(), CoroutineScope {

  companion object {
	 fun newInstance() = ViewStudentFragment()
  }

  private lateinit var viewModel: ViewStudentViewModel
  private lateinit var shared_view_model: SharedViewModel

  private val job = Job()
  override val coroutineContext: CoroutineContext
	 get() = job + Dispatchers.Default

  lateinit var et_search: AutoCompleteTextView
  lateinit var btn_search: ImageButton
  lateinit var btn_clear_search: ImageButton
  lateinit var tab_layout: TabLayout
  lateinit var vpager: ViewPager
  lateinit var tab_data: TabItem
  lateinit var tab_attendance: TabItem


  override fun onCreateView(
	 inflater: LayoutInflater, container: ViewGroup?,
	 savedInstanceState: Bundle?
  ): View? {
	 val view = inflater.inflate(R.layout.fragment_view_student, container, false)

	 et_search= view.findViewById(R.id.et_student_search_view)
	 btn_search= view.findViewById(R.id.btn_search_student_view)
	 btn_clear_search= view.findViewById(R.id.btn_clear_search_view)
	 tab_layout= view.findViewById(R.id.tab_layout_student_info)
	 vpager= view.findViewById(R.id.vp_student_info)
	 //tab_data= view.findViewById(R.id.tab_student_data)
	 //tab_attendance= view.findViewById(R.id.tab_student_attendance)


	 return view
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
	 super.onActivityCreated(savedInstanceState)
	 viewModel = ViewModelProvider(this).get(ViewStudentViewModel::class.java)
	 shared_view_model =  activity?.run {
		ViewModelProvider(this).get(SharedViewModel::class.java)
	 } ?: throw Exception("Invalid Activity")

	 // OBSERVERS

	 viewModel.students.observe(this.viewLifecycleOwner, Observer { students ->
		et_search.setAdapter(ArrayAdapter<Student>(context!!,android.R.layout.simple_dropdown_item_1line,students))
		et_search.setOnItemClickListener { parent, view, position, id ->
		  viewModel.selected_student= et_search.adapter.getItem(position) as Student
		}
	 })

	 // LISTENERS

	 et_search.addTextChangedListener{
		if(btn_clear_search.visibility == View.GONE) {
		  btn_clear_search.visibility = View.VISIBLE
		}
	 }

	 btn_clear_search.setOnClickListener {
		et_search.text.clear()
		btn_clear_search.visibility= View.GONE
	 }

	 btn_search.setOnClickListener {
		if(viewModel.selected_student != null){
		  launch {

			 val data_request = async {
				viewModel.getStudentCourseInformation()
			 }
			 val attendance_request= async{
				viewModel.getStudentAttendances(context!!)
			 }

			 awaitAll(data_request,attendance_request)

			 val student_data= mapOf<String,Any>(
				"data" to viewModel.selected_student!!,
				"courses_history" to viewModel.map_student_courses)


			 launch(Dispatchers.Main) {
				shared_view_model.setStudentData(student_data)
				shared_view_model
				tab_layout.visibility = View.VISIBLE
				vpager.visibility = View.VISIBLE
			 }

		  }
		}
	 }

	 vpager.adapter= ViewPagerAdapter(context!!,activity!!.supportFragmentManager,2)

	 vpager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))

	 tab_layout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
		override fun onTabReselected(p0: TabLayout.Tab?) {

		}

		override fun onTabUnselected(p0: TabLayout.Tab?) {
		}

		override fun onTabSelected(p0: TabLayout.Tab?) {
		  Log.i("VIEWSTUDENT","tab selected ${p0!!.text}")
		  vpager.currentItem = p0.position
		}
	 })
	 //tab_layout.setupWithViewPager(vpager)
  }

  class ViewPagerAdapter(private val context: Context, manager: FragmentManager, private var totalTabs: Int) : FragmentPagerAdapter(manager,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
	 // this is for fragment tabs
	 override fun getItem(position: Int): Fragment {
		Log.i("VIEWSTUDENT","SETTING FRAGMENT ON VP")
		return when (position) {
		  0 -> {
			 StudentDataFragment()
		  }
		  else -> {
			 StudentAttendanceFragment()
		  }
		}
	 }

	 // this counts total number of tabs
	 override fun getCount(): Int {
		return totalTabs
	 }

	 // this is for fragment tabs

  }

}
