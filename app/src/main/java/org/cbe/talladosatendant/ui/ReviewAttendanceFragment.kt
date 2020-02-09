package org.cbe.talladosatendant.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.adapters.SectionedRecyclerViewAdapter
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.viewmodels.ReviewAttendanceViewModel
import org.cbe.talladosatendant.viewmodels.SharedViewModel

class ReviewAttendanceFragment : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        fun newInstance() = ReviewAttendanceFragment()
    }

    lateinit var course_spinner : Spinner
    lateinit var attendances_recycler: RecyclerView

    private lateinit var viewModel: ReviewAttendanceViewModel
    private lateinit var shared_view_model: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_review_attendance, container, false)

        course_spinner = view.findViewById(R.id.course_spinner_review)
        attendances_recycler = view.findViewById(R.id.rv_attendances_review)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ReviewAttendanceViewModel::class.java)
        shared_view_model =  activity?.run {
            ViewModelProvider(this).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        attendances_recycler.layoutManager = LinearLayoutManager(context!!)
        attendances_recycler.adapter = SectionedRecyclerViewAdapter(context!!,shared_view_model)

        viewModel.courses.observe(this.viewLifecycleOwner, Observer {
                courses ->
            Log.i("REVIEWATTENDANCE","THIS courses: ${viewModel.courses.value!!}")
            course_spinner.adapter = ArrayAdapter<Course>(activity!!.applicationContext,
                android.R.layout.simple_spinner_dropdown_item,
                viewModel.courses.value!!)
            course_spinner.onItemSelectedListener= this
        })


        viewModel.attendances_sectioned_list.observe(this.viewLifecycleOwner, Observer { sections_list ->
            Log.i("ATTREVIEW","sections: $sections_list")
            sections_list?.let {
                (attendances_recycler.adapter as SectionedRecyclerViewAdapter).setItems(sections_list)

            }
        })
    }

    /**
     * Spinners Listeners
     */

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val selected_course= viewModel.courses.value!![p2]
        viewModel.getAttendances(selected_course.course_id)
    }

}
