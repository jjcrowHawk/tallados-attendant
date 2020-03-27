package org.cbe.talladosatendant.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider

import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.util.Utils
import org.cbe.talladosatendant.viewmodels.EditStudentViewModel
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext
import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import org.cbe.talladosatendant.databases.entities.Student
import java.io.FileInputStream

class EditStudentFragment : Fragment(), CoroutineScope {

  companion object {
	 fun newInstance() = EditStudentFragment()
  }

  val IMAGE_FILE_REQUEST= 1080
  val date_regex= "\\d{2}/\\d{2}/\\d{4}"
  val phone_regex= "\\+?\\d+"

  private val job = Job()
  override val coroutineContext: CoroutineContext
	 get() = job + Dispatchers.Default

  lateinit var et_search: AutoCompleteTextView
  lateinit var btn_search: ImageButton
  lateinit var container_layout: ConstraintLayout
  lateinit var et_name: EditText
  lateinit var et_lname: EditText
  lateinit var et_address: EditText
  lateinit var et_phone: EditText
  lateinit var et_dob: EditText
  lateinit var tv_age: TextView
  lateinit var spinner_courses: Spinner
  lateinit var iv_picture: ImageView
  lateinit var btn_add_pic: Button
  lateinit var btn_update_student: Button
  lateinit var btn_delete_student: Button
  lateinit var btn_clear_search: ImageButton
  var waiting_dialog : DialogInterface? = null

  private lateinit var viewModel: EditStudentViewModel

  override fun onCreateView(
	 inflater: LayoutInflater, container: ViewGroup?,
	 savedInstanceState: Bundle?
  ): View? {
	 val view= inflater.inflate(R.layout.fragment_edit_student, container, false)

	 et_search= view.findViewById(R.id.et_student_search_edit)
	 btn_search= view.findViewById(R.id.btn_search_student_edit)
	 container_layout= view.findViewById(R.id.cl_container)
	 et_name= view.findViewById(R.id.et_names_edit)
	 et_lname= view.findViewById(R.id.et_last_name_edit)
	 et_address= view.findViewById(R.id.et_address_edit)
	 et_phone= view.findViewById(R.id.et_phone_edit)
	 et_dob= view.findViewById(R.id.et_dob_edit)
	 tv_age= view.findViewById(R.id.tv_age_edit)
	 spinner_courses= view.findViewById(R.id.course_spinner_edit)
	 iv_picture= view.findViewById(R.id.iv_student_edit)
	 btn_add_pic= view.findViewById(R.id.btn_picture_edit)
	 btn_update_student= view.findViewById(R.id.btn_student_edit)
	 btn_delete_student= view.findViewById(R.id.btn_student_delete)
	 btn_clear_search= view.findViewById(R.id.btn_clear_search)

	 return view
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
	 super.onActivityCreated(savedInstanceState)
	 viewModel = ViewModelProvider(this).get(EditStudentViewModel::class.java)

	 /*Observers */

	 viewModel.students.observe(this.viewLifecycleOwner, Observer { students ->
		et_search.setAdapter(ArrayAdapter<Student>(context!!,android.R.layout.simple_dropdown_item_1line,students))
		et_search.setOnItemClickListener { parent, view, position, id ->
		  viewModel.selected_student= et_search.adapter.getItem(position) as Student
		}
	 })

	 viewModel._courses.observe(this.viewLifecycleOwner, Observer { courses ->
		Log.i("EDITSTUDENT","THIS COURSES: ${courses}")
	 })

	 viewModel.courses.observe(this.viewLifecycleOwner, Observer { courses ->
		spinner_courses.adapter= ArrayAdapter<Course>(context!!,android.R.layout.simple_spinner_dropdown_item,courses)
	 })

	 viewModel.showAgeExceedError.observe(this.viewLifecycleOwner, Observer { value ->
		if(value){
		  tv_age.text= "${tv_age.text} - la edad del niño no se adapta a ningún curso"
		}
	 })

	 viewModel.getShowWaitingDialog().observe(this.viewLifecycleOwner, Observer { value ->
		if(value){
		  this.waiting_dialog= Utils.showWaitingDialog(context!!,"Actualizando Niño...")
		}
		else{
		  this.waiting_dialog?.dismiss()
		}
	 })

	 viewModel.getShowSuccessDialog().observe(this.viewLifecycleOwner, Observer { value ->
		if(value){
		  Utils.showSuccessDialog(context!!,"Actualizado Exitosamente","Se ha actulizado la información del/la niño/niña!"){
				dialogInterface, _ ->
			 clearFields()
			 dialogInterface.dismiss()
		  }
		}
	 })

	 viewModel.getShowErrorDialog().observe(this.viewLifecycleOwner, Observer { value ->
		if(value){
		  Utils.showErrorDialog(context!!,"Error","Hubo un problema al actualizar, intente nuevamente"){
				dialogInterface, _ -> dialogInterface.dismiss()
		  }
		}
	 })

	 viewModel.getShowSuccessDeleteDialog().observe(this.viewLifecycleOwner, Observer { value ->
		if(value){
		  Utils.showSuccessDialog(context!!,"Elminado Exitosamente","Se ha eliminado el/la niño/niña!"){
				dialogInterface, _ ->
			 clearFields()
			 dialogInterface.dismiss()
		  }
		}
	 })

	 viewModel.getShowErrorDeleteDialog().observe(this.viewLifecycleOwner, Observer { value ->
		if(value){
		  Utils.showErrorDialog(context!!,"Error","Hubo un problema al eliminar, intente nuevamente"){
				dialogInterface, _ -> dialogInterface.dismiss()
		  }
		}
	 })



	 /* Listeners */

	 et_search.addTextChangedListener{ _ ->
		if(btn_clear_search.visibility == View.GONE) {
		  btn_clear_search.visibility = View.VISIBLE
		}
	 }

	 btn_clear_search.setOnClickListener {
		et_search.text.clear()
		btn_clear_search.visibility= View.GONE
	 }

	 btn_search.setOnClickListener{
		if(viewModel.selected_student != null){
		  et_name.setText(viewModel.selected_student!!.name)
		  et_lname.setText(viewModel.selected_student!!.last_name)
		  et_address.setText(viewModel.selected_student!!.address)
		  et_phone.setText(viewModel.selected_student!!.phone)
		  et_dob.setText(Utils.getFormatedDate(viewModel.selected_student!!.dob))
		  launch {
			 viewModel.setCourseOnDataSource(viewModel.selected_student!!.student_id)
			 async(Dispatchers.Main) {
				if (!viewModel.selected_student!!.picture.equals("")) {
				  val file = FileInputStream(File(viewModel.selected_student!!.picture))
				  val bitmap = Utils.getBitmapFromStream(file)
				  iv_picture.setImageBitmap(bitmap)
				} else{
				  iv_picture.setImageDrawable(context!!.getDrawable(R.drawable.user_pic_placeholder))
				}
				container_layout.visibility= View.VISIBLE
			 }
		  }
		} else{
		  et_search.error = "Ingrese y seleccione un nombre válido"
		}
	 }

	 btn_add_pic.setOnClickListener {
		val intent= Intent(Intent.ACTION_GET_CONTENT)
		intent.setType("image/*")
		startActivityForResult(intent,IMAGE_FILE_REQUEST)
	 }

	 et_dob.addTextChangedListener {editable ->

		Log.i("EDITSTUDENT","New Text: ${editable.toString()}")
		Log.i("EDITSTUDENT","MATCHES: ${Pattern.matches(date_regex,editable.toString())}")
		if(Pattern.matches(date_regex,editable.toString())){
		  val date = Utils.parseDateFromString(editable.toString())
		  date?.let {
			 val years= Utils.getDiffYears(date, Date())
			 tv_age.text= "$years años"
		  }
		}

	 }

	 btn_update_student.setOnClickListener {
		viewModel.is_valid_form= et_name.validate("No puede dejar este campo vacío"){ !it.isNullOrBlank() }
		viewModel.is_valid_form= et_lname.validate("No puede dejar este campo vacío"){ !it.isNullOrBlank()}
		viewModel.is_valid_form= et_address.validate("No puede dejar este campo vacío"){ !it.isNullOrBlank()}
		viewModel.is_valid_form= et_phone.validate("Ingrese un número válido"){ Pattern.matches(phone_regex,it) }
		viewModel.is_valid_form= et_dob.validate("Ingrese una fecha valida en este formato: dd/mm/aaaa"){
		  Pattern.matches(date_regex,it)
		}

		if(viewModel.is_valid_form){
		  Log.i("EDITSTUDENT","FORM IS VALID!")
		  viewModel.st_name= et_name.text.toString()
		  viewModel.st_lname= et_lname.text.toString()
		  viewModel.st_address= et_address.text.toString()
		  viewModel.st_phone= et_phone.text.toString()
		  viewModel.st_dob= et_dob.text.toString()
		  viewModel.selected_course= spinner_courses.selectedItem as Course

		  viewModel.setShowWaitingDialog(true)

		  this.launch {
			 val updated= viewModel.updateStudent()
			 if(updated){
				viewModel.student_pic_uri?.let { uri ->
				  val filename= "${viewModel.st_lname}_${viewModel.st_name}.jpg".replace(" ","_")
				  val path = savePictureToStorage(uri, filename)
				  Log.i("EDITSTUDENT", "image_path: $path")
				  path?.let {
					 viewModel.addPictureToStudent(viewModel.selected_student!!.student_id, path)
				  }
				}
				launch(Dispatchers.Main){
				  viewModel.setShowWaitingDialog(false)
				  viewModel.setShowSuccessDialog(true)
				}
			 }
			 else{
				launch(Dispatchers.Main){
				  viewModel.setShowWaitingDialog(false)
				  viewModel.setShowErrorDialog(true)
				}
			 }
		  }

		}
	 }

	 btn_delete_student.setOnClickListener {
		Utils.showConfirmDialog(context!!,"Confirmar Accion",
		  "Está seguro que desea eliminar al niño/niña? Esta acción es irreversible","Eliminar"){
		  dialogInterface, _ ->
		  dialogInterface.dismiss()

		  viewModel.setShowWaitingDialog(true)

		  this.launch {
			 val deleted= viewModel.deleteSelectedStudent()
			 if(deleted){
				launch(Dispatchers.Main){
				  viewModel.setShowWaitingDialog(false)
				  viewModel.setShowSuccessDeleteDialog(true)
				}
			 }
			 else{
				launch(Dispatchers.Main){
				  viewModel.setShowWaitingDialog(false)
				  viewModel.setShowErrorDeleteDialog(true)
				}
			 }
		  }

		}

	 }

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	 super.onActivityResult(requestCode, resultCode, data)

	 if (requestCode == IMAGE_FILE_REQUEST) {
		if (resultCode == Activity.RESULT_OK) {
		  data?.let {
			 val uri= it.data
			 val mime= context!!.contentResolver.getType(uri!!)
			 if(mime!!.startsWith("image")) {
				val stream = context!!.contentResolver.openInputStream(it.data!!)
				val bitmap = Utils.getBitmapFromStream(stream!!) //Utils.getBitmapFromUri(uri!!,context!!)
				iv_picture.setImageBitmap(bitmap)
				viewModel.student_pic_uri= uri
			 }
			 else{
				Utils.showErrorDialog(context!!,"Error al cargar archivo","Por favor elija un archivo de imagen correcto (jpg,png,jpeg..)"){
					 dialog,_ ->	dialog.dismiss()
				}
			 }
		  }
		}
	 }
  }

  private fun savePictureToStorage(uri: Uri, picture_name:String) : String?{
	 val wrapper= ContextWrapper(context!!)

	 val file_dir= wrapper.getDir("students_pictures", Context.MODE_PRIVATE)
	 val image_file= File(file_dir,picture_name)

	 var fos: FileOutputStream?= null
	 try {
		fos= FileOutputStream(image_file)

		val bitmap= Utils.getBitmapFromUri(uri,context!!).compress(Bitmap.CompressFormat.JPEG,100,fos)

		return image_file.absolutePath

	 }catch (ex: Exception){
		ex.printStackTrace()
	 } finally {
		fos?.close()
	 }
	 return null
  }

  private fun clearFields(){
	 iv_picture.setImageDrawable(context!!.getDrawable(R.drawable.user_pic_placeholder))
	 et_name.text.clear()
	 et_lname.text.clear()
	 et_address.text.clear()
	 et_phone.text.clear()
	 et_dob.text.clear()
	 tv_age.text = ""
	 viewModel.student_pic_uri= null
	 container_layout.visibility= View.GONE
  }

  /* EditText Extension */

  fun EditText.validate(error_message: String, validator: (String) -> Boolean) : Boolean {
	 val validation = validator(this.text.toString())
	 this.error = if (validation) null else error_message
	 return validation
  }

}
