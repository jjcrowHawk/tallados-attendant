package org.cbe.talladosatendant.ui


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ListView
import androidx.lifecycle.ViewModelProvider
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.adapters.AttendanceCourseMainAdapter
import org.cbe.talladosatendant.adapters.BirthdaysMainAdapter
import org.cbe.talladosatendant.adapters.PastAttendanceMainAdapter
import org.cbe.talladosatendant.util.Utils
import org.cbe.talladosatendant.viewmodels.MainViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var mainViewModel: MainViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view= inflater.inflate(R.layout.fragment_main, container, false)

        val assigned_courses_grid = view.findViewById<GridView>(R.id.courses_grid)
        val past_attendances_lview= view.findViewById<ListView>(R.id.passed_assitances_listview)
        val birthdays_lview= view.findViewById<ListView>(R.id.birthdays_listview)

       Log.i("Main","===> INITING VIEWMODEL FROM FRAGMENT")
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        Log.i("Main","===> VIEWMODEL INITED? FROM FRAGMENT")


        //val assigned_courses= Utils.getSampleCourses()
        //val past_attendances= arrayListOf<Attendance>(Attendance())
        //val birthdays= arrayListOf<Student>(Student(), Student())

        val ac_grid_adapter= AttendanceCourseMainAdapter(activity!!.applicationContext,
            R.layout.course_main_item, mainViewModel.assignedCourses.value)
        assigned_courses_grid.adapter = ac_grid_adapter

        val past_attendances_adapter = PastAttendanceMainAdapter(activity!!.applicationContext,
            R.layout.passed_attendance_main_item, mainViewModel.pastMonthAttendances.value)
        past_attendances_lview.adapter = past_attendances_adapter

        val birthdays_adapter= BirthdaysMainAdapter(activity!!.applicationContext,
            R.layout.birthday_main_item, mainViewModel.currentMonthBirthdayStudents.value)
        birthdays_lview.adapter = birthdays_adapter

        mainViewModel.assignedCourses.observe(this.viewLifecycleOwner, androidx.lifecycle.Observer {
                courses ->
            run {
                Log.i("", "this courses: ${courses}")
                courses?.let {
                    ac_grid_adapter.setCourseList(courses)
                    Utils.setGridViewHeightBasedOnChildren(assigned_courses_grid,2)
                }
            }
        })

        mainViewModel.pastMonthAttendances.observe(this.viewLifecycleOwner, androidx.lifecycle.Observer {
            attendances ->
                Log.i("MAIN","this attendances: ${attendances}")
                attendances?.let {
                    past_attendances_adapter.setItems(attendances)
                    Utils.setListViewFullHeight(past_attendances_lview)
                }
        })

        mainViewModel.currentMonthBirthdayStudents.observe(this.viewLifecycleOwner, androidx.lifecycle.Observer{
            students ->
                Log.i("MAIN","this b_students: ${students}")
                students?.let {
                    birthdays_adapter.setItems(students)
                    Utils.setListViewFullHeight(birthdays_lview)
                }
        })

        //Utils.setGridViewHeightBasedOnChildren(assigned_courses_grid,2)
        //Utils.setListViewFullHeight(past_attendances_lview)
        //Utils.setListViewFullHeight(birthdays_lview)

        return view
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
