package org.cbe.talladosatendant.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.adapters.StudentAttendanceEditAdapter
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.util.Utils
import org.cbe.talladosatendant.viewmodels.EditAttendanceViewModel

class EditAttendanceFragment : Fragment(), AdapterView.OnItemSelectedListener {

  lateinit var course_spinner : Spinner
  lateinit var date_spinner: Spinner
  lateinit var students_recycler: RecyclerView
  lateinit var fab_save_attendance: FloatingActionButton
  var waitingDialog : Dialog? = null

  companion object {
		fun newInstance() = EditAttendanceFragment()
  }

  private lateinit var viewModel: EditAttendanceViewModel

  @SuppressLint("RestrictedApi")
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view : View = inflater.inflate(R.layout.fragment_edit_attendance, container, false)

		course_spinner = view.findViewById(R.id.course_spinner_edit)
		date_spinner = view.findViewById(R.id.date_spinner_edit)
		students_recycler= view.findViewById(R.id.rv_attendance_edit)
		fab_save_attendance= view.findViewById(R.id.fab_save_edit_attendance)

		fab_save_attendance.hide()


		return view
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		val layoutManager= LinearLayoutManager(this.context)
		students_recycler.layoutManager= layoutManager
		students_recycler.adapter= StudentAttendanceEditAdapter(this.context!!)

		viewModel = ViewModelProvider(this).get(EditAttendanceViewModel::class.java)


		/* ViewModel Observers */

		viewModel.courses.observe(this.viewLifecycleOwner, Observer {
				  courses ->
			 Log.i("EDITATT","THIS courses: ${viewModel.courses.value!!}")
			 course_spinner.adapter = ArrayAdapter<Course>(activity!!.applicationContext,
				  android.R.layout.simple_spinner_dropdown_item, viewModel.courses.value!!)
			 course_spinner.onItemSelectedListener= this
		})

		viewModel.attendances_statuses.observe(this.viewLifecycleOwner, Observer {
				  statuses -> viewModel.setAttendanceStatuses(statuses)
		})

		viewModel.attendances.observe(this.viewLifecycleOwner, Observer { attendances ->
		  viewModel.attendances_dates!!.clear()
		  viewModel.attendances.value!!.forEach { attendance ->

			 viewModel.attendances_dates!!.add(Utils.getFormattedLatinDate(attendance.date))
		  }
		  date_spinner.adapter = ArrayAdapter<String>(activity!!.applicationContext, android.R.layout.simple_spinner_dropdown_item,
			 viewModel.attendances_dates!!)
		  date_spinner.onItemSelectedListener = this
		})

	 	viewModel.students_records.observe(this.viewLifecycleOwner, Observer {records ->
		  (students_recycler.adapter as StudentAttendanceEditAdapter?)!!.setItems(records)
		  students_recycler.adapter!!.notifyDataSetChanged()
		  if (!records.isEmpty()) {
			 fab_save_attendance.show()
		  } else {
			 fab_save_attendance.hide()
		  }
		})

		viewModel.show_saving_dialog.observe(this.viewLifecycleOwner, Observer {
				  value ->
			 if(value){
				  waitingDialog= Utils.showWaitingDialog(context!!,"Guardando Asistencia...")
			 } else {
				  waitingDialog?.dismiss()
			 }
		})

		viewModel.show_success_dialog.observe(this.viewLifecycleOwner, Observer {
				  value ->
			 if(value){
				  Utils.showSuccessDialog(context= context!!,
						title= "Guardado Exitoso!",
						text= "Se ha modificado la asistencia exitosamente"
				  ){ dialog, which ->
						dialog.dismiss()
						viewModel.show_success_dialog.value = false
						//activity?.supportFragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
				  }
			 }
		})

	 	//TODO PRESENT ERROR DIALOG

		viewModel.show_comment_dialog.observe(this.viewLifecycleOwner, Observer {
				  value ->
			 if(value){
				  Utils.showEditDialog(context= context!!,
						title= "Comentarios sobre la asistencia",
						message = "Si no tiene comentarios, deje en blanco y pulse guardar",
					 	default_placeholder = viewModel.selected_attendance.observations,
						positive_btn_text = "Guardar"
				  ){ dialog, _ ->

						val at_comment= (dialog as androidx.appcompat.app.AlertDialog).findViewById<EditText>(R.id.et_dialog)?.text.toString()
						dialog.dismiss()
						viewModel.show_comment_dialog.value = false
						(students_recycler.adapter as StudentAttendanceEditAdapter?)?.getStudentAttendancesStatuses()?.let {
								  st_attendances_map ->
							 val attendance = viewModel.selected_attendance
							 viewModel.saveStudentsAttendanceToDatabase(st_attendances_map,attendance,at_comment)
						} ?: run{
							 Log.i("AttendanceMap","No data available")
						}

				  }
			 }
		})


	 /* Listeners */

		fab_save_attendance.setOnClickListener {
			 Log.i("TAKEATTENDANCE","Saving...")
			 viewModel.show_comment_dialog.value = true
		}

		students_recycler.addOnScrollListener(object: RecyclerView.OnScrollListener(){
			 override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				  super.onScrolled(recyclerView, dx, dy)
			 }

			 override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
				  super.onScrollStateChanged(recyclerView, newState)
				  if(newState != RecyclerView.SCROLL_STATE_IDLE  && fab_save_attendance.visibility == View.VISIBLE){
						fab_save_attendance.hide()
				  }
				  else if(newState == RecyclerView.SCROLL_STATE_IDLE && fab_save_attendance.visibility != View.VISIBLE){
						fab_save_attendance.show()
				  }
			 }
		})
  }

  override fun onNothingSelected(p0: AdapterView<*>?) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
		if(p0!!.id == R.id.course_spinner_edit) {
		  fab_save_attendance.hide()
		  Log.i("Spinner Course", "Selected: ${viewModel.courses.value!![p2]}")
			 /*if(students_recycler.adapter?.itemCount != 0){
				  for(i in 0..students_recycler.adapter?.itemCount!!){
						(students_recycler.findViewHolderForAdapterPosition(i) as StudentAttendanceTakingAdapter.ViewHolder)
							 .rg_statuses.clearCheck()
				  }
			 }*/
		  (students_recycler?.adapter as StudentAttendanceEditAdapter?)?.clearItems()

		  viewModel.populateAttendanceDates(viewModel.courses.value!![p2].course_id)

		}
		else if(p0!!.id == R.id.date_spinner_edit){

			 if(viewModel.attendances_dates!!.size > p2) {
				Log.i("Spinner Date", "Selected: ${viewModel.attendances_dates!![p2]}")
				viewModel.selected_attendance= viewModel.attendances.value!![p2]
				viewModel.getStudentsRecordsFromCourse()
			 }

		}

  }

}
