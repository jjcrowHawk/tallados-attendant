package org.cbe.talladosatendant.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.cbe.talladosatendant.R


/**
 * A simple [Fragment] subclass.
 * Use the [StudentAttendanceFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class StudentAttendanceFragment : Fragment() {


  override fun onCreateView(
	 inflater: LayoutInflater, container: ViewGroup?,
	 savedInstanceState: Bundle?
  ): View? {
	 // Inflate the layout for this fragment
	 val view= inflater.inflate(R.layout.fragment_student_attendance, container, false)

	 return view
  }


  override fun onActivityCreated(savedInstanceState: Bundle?) {
	 super.onActivityCreated(savedInstanceState)


  }


  companion object {
	 /**
	  * Use this factory method to create a new instance of
	  * this fragment using the provided parameters.
	  *
	  * @return A new instance of fragment StudentAttendanceFragment.
	  */
	 @JvmStatic
	 fun newInstance() = StudentAttendanceFragment().apply {}
  }
}
