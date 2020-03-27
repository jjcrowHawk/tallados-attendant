package org.cbe.talladosatendant.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.adapters.StudentImportAdapter
import org.cbe.talladosatendant.util.SheetsReaderPOI
import org.cbe.talladosatendant.util.Utils
import org.cbe.talladosatendant.viewmodels.ImportStudentListViewModel
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

class ImportStudentListFragment : Fragment(), CoroutineScope {

  companion object {
	 fun newInstance() = ImportStudentListFragment()
  }

  private val job = Job()
  override val coroutineContext: CoroutineContext
	 get() = job + Dispatchers.Default

  private val FILE_REQUEST= 2040

  lateinit var et_file : EditText
  lateinit var btn_file_select: Button
  lateinit var container_students_number: LinearLayout
  lateinit var container_students: LinearLayout
  lateinit var tv_students_found: TextView
  lateinit var check_assign_courses: CheckBox
  lateinit var container_students_unassigned: LinearLayout
  lateinit var tv_unassigned_students: TextView
  lateinit var lview_students: RecyclerView
  lateinit var btn_import: Button

  var waiting_dialog: DialogInterface?= null

  private lateinit var viewModel: ImportStudentListViewModel

  override fun onCreateView(
	 inflater: LayoutInflater, container: ViewGroup?,
	 savedInstanceState: Bundle?
  ): View? {
	 val view= inflater.inflate(R.layout.fragment_import_student_list, container, false)

	 et_file= view.findViewById(R.id.et_file_import)
	 btn_file_select= view.findViewById(R.id.btn_get_file_import)
	 container_students_number= view.findViewById(R.id.linlayout_students_found)
	 container_students= view.findViewById(R.id.linlayout_students_container_import)
	 tv_students_found= view.findViewById(R.id.tv_students_found_import)
	 check_assign_courses= view.findViewById(R.id.check_assign_course_import)
	 container_students_unassigned= view.findViewById(R.id.linlayout_students_unassigned)
	 tv_unassigned_students= view.findViewById(R.id.tv_students_unassigned_import)
	 lview_students= view.findViewById(R.id.rv_students_found_import)
	 btn_import= view.findViewById(R.id.btn_import_students)

	 return view
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
	 super.onActivityCreated(savedInstanceState)
	 viewModel = ViewModelProvider(this).get(ImportStudentListViewModel::class.java)

	 /* OBSERVERS */

	 viewModel.getShowWaitingDialog().observe(this.viewLifecycleOwner, Observer {value ->
		if(value){
		  waiting_dialog= Utils.showWaitingDialog(context!!)
		}
	 })

	 viewModel.getShowSuccessDialog().observe(this.viewLifecycleOwner, Observer { value ->
		if(value){
		  Utils.showSuccessDialog(context!!,context!!.getString(R.string.importedStudentsDialogTitle),
			 "Se han agregado ${viewModel.added_students} niños y niñas"){dialogInterface, _ ->

			 dialogInterface.dismiss()

			 (lview_students.adapter as StudentImportAdapter).clearItems()
			 et_file.text.clear()
			 container_students.visibility= View.GONE
			 container_students_number.visibility= View.GONE
			 btn_import.visibility= View.GONE

		  }
		}
	 })

	 viewModel._courses.observe(this.viewLifecycleOwner, Observer {  })

	 viewModel.getStudentsLD().observe(this.viewLifecycleOwner, Observer {students ->
		Log.i("IMPORTSTUDENT","Students: $students")
		lview_students.layoutManager= LinearLayoutManager(context!!)
		lview_students.adapter= StudentImportAdapter(context!!,students,viewModel.courses!!,this.viewModel)
		container_students.visibility= View.VISIBLE
		container_students_number.visibility= View.VISIBLE
		btn_import.visibility= View.VISIBLE
		tv_students_found.text= "${students.size}"
	 })

	 viewModel.unassigned_students_count.observe(this.viewLifecycleOwner,Observer{ count ->
		tv_unassigned_students.text = "$count"
	 })

	 /* LISTENERS */

	 btn_file_select.setOnClickListener {
		val intent= Intent(Intent.ACTION_GET_CONTENT)
		intent.setType("*/*")
		startActivityForResult(intent,FILE_REQUEST)
	 }

	 check_assign_courses.setOnClickListener{ view ->
		val value= (view as CheckBox).isChecked
		if(value){

		  val map_course_students= viewModel.assignCoursesToStudentsByAge()
		  val students_ordered= viewModel.getStudentsSortedByUnassignedFirst(map_course_students)

		  (lview_students.adapter as StudentImportAdapter).setItems(students_ordered)
		  (lview_students.adapter as StudentImportAdapter).setCourseStudentsMap(map_course_students)
		  (lview_students.adapter as StudentImportAdapter).changeCourseSpinnerItemsVisibility(true)
		  container_students_unassigned.visibility = View.VISIBLE
		}
		else{
		  (lview_students.adapter as StudentImportAdapter).changeCourseSpinnerItemsVisibility(false)
		  (lview_students.adapter as StudentImportAdapter).clearItems()
		  (lview_students.adapter as StudentImportAdapter).setItems(viewModel.getStudentsLD().value!!)
		  container_students_unassigned.visibility = View.GONE
		}
	 }

	 btn_import.setOnClickListener {
		if(!check_assign_courses.isChecked || viewModel.unassigned_students_count.value!! > 0){
		  Utils.showConfirmDialog(context!!,context!!.getString(R.string.unassignedStudentDialogTitle),
			 context!!.getString(R.string.unassignedStudentsMessage),"Importar"){ dialogInterface, _ ->

			 dialogInterface.dismiss()

			 waiting_dialog= Utils.showWaitingDialog(context!!,"Importando a la base de datos...")
			 async {
				val students_map= (lview_students.adapter as StudentImportAdapter).getCourseStudentsMap()
				viewModel.importStudents(students_map)
				launch(Dispatchers.Main) {
				  waiting_dialog?.dismiss()
				  viewModel.setShowSuccessDialog(true)
				}
			 }


		  }
		}
		else{
		  waiting_dialog= Utils.showWaitingDialog(context!!,"Importando a la base de datos...")
		  async {
			 val students_map= (lview_students.adapter as StudentImportAdapter).getCourseStudentsMap()
			 viewModel.importStudents(students_map)
			 launch(Dispatchers.Main) {
				waiting_dialog?.dismiss()
				viewModel.setShowSuccessDialog(true)
			 }
		  }
		}
	 }

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	 super.onActivityResult(requestCode, resultCode, data)

	 if (requestCode == FILE_REQUEST) {
		if (resultCode == Activity.RESULT_OK) {
		  data?.let {
			 val uri= it.data
			 val mime= context!!.contentResolver.getType(uri!!)
			 Log.i("IMPORTSTUDENT","MIME TYPE: $mime")
			 et_file.setText(uri.path)
			 if(Pattern.matches("(.)*(vnd.openxmlformats-officedocument.spreadsheetml.sheet|vnd.ms-excel|csv)(.)*",mime!!)) {
				Log.i("IMPORTSTUDENT","Document FOUND")
				val path= Utils.createTempFileWithContent("students",".xlsx",context!!.contentResolver.openInputStream(it.data!!)!!)
				val sheetsReader= SheetsReaderPOI(path)
				val fields= context!!.resources.getStringArray(R.array.sheet_fields).toList()

				val sheets= sheetsReader.getSheetsTitles()
				val checkedChoices= sheets.map { item -> true}.toBooleanArray()
				Log.i("IMPORTSTUDENT","SHEETS FOUND: $sheets with checkeds: $checkedChoices")
				Utils.showChoicesDialog(context!!,"Seleccione las hojas que desea importar del archivo", sheets.toTypedArray(),checkedChoices){ dialogInterface, _ ->
				  dialogInterface.dismiss()

				  waiting_dialog = Utils.showWaitingDialog(context!!,"Leyendo archivo...")

				  val selected_sheets= mutableListOf<Int>()
				  sheets.forEachIndexed { index, s ->
					 if(checkedChoices[index]) {
						selected_sheets.add(index)
					 }
				  }
				  Log.i("IMPORTSTUDENT","selecteds: ${selected_sheets}")
				  async{
					 viewModel.getStudentDataFromSheet(sheetsReader,selected_sheets,fields)
					 launch(Dispatchers.Main) {
						waiting_dialog?.dismiss()
					 }
				  }
				}


			 }
			 else{
				Utils.showErrorDialog(context!!,"Error al cargar archivo","Por favor elija un archivo correcto y que esté en un formato adecuado para la aplicación"){
					 dialog,_ ->	dialog.dismiss()
				}
			 }
		  }
		}
	 }
  }

}
