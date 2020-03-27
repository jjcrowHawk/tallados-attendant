package org.cbe.talladosatendant.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toFile
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*

import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.util.Utils
import org.cbe.talladosatendant.viewmodels.AddStudentViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception
import java.util.Date
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext


class AddStudentFragment() : Fragment(), CoroutineScope {

  companion object {
	 fun newInstance() = AddStudentFragment()
  }

  val IMAGE_FILE_REQUEST= 1080
  val date_regex= "\\d{2}/\\d{2}/\\d{4}"
  val phone_regex= "\\+?\\d+"

  private val job = Job()
  override val coroutineContext: CoroutineContext
	 get() = job + Dispatchers.Default

  lateinit var et_name: EditText
  lateinit var et_lname: EditText
  lateinit var et_address: EditText
  lateinit var et_phone: EditText
  lateinit var et_dob: EditText
  lateinit var tv_age: TextView
  lateinit var spinner_courses: Spinner
  lateinit var iv_picture: ImageView
  lateinit var btn_add_pic: Button
  lateinit var btn_add_student: Button
  var waiting_dialog : DialogInterface? = null

  private lateinit var viewModel: AddStudentViewModel


  override fun onCreateView(
	 inflater: LayoutInflater, container: ViewGroup?,
	 savedInstanceState: Bundle?
  ): View? {
	 val view= inflater.inflate(R.layout.fragment_add_student, container, false)

	 et_name= view.findViewById(R.id.et_names_add)
	 et_lname= view.findViewById(R.id.et_last_name_add)
	 et_address= view.findViewById(R.id.et_address_add)
	 et_phone= view.findViewById(R.id.et_phone_add)
	 et_dob= view.findViewById(R.id.et_dob_add)
	 tv_age= view.findViewById(R.id.tv_age_add)
	 spinner_courses= view.findViewById(R.id.course_spinner_add)
	 iv_picture= view.findViewById(R.id.iv_student_add)
	 btn_add_pic= view.findViewById(R.id.btn_picture_add)
	 btn_add_student= view.findViewById(R.id.btn_student_add)

	 //spinner_courses.isEnabled = false
	 //spinner_courses.isClickable= false

	 return view
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
	 super.onActivityCreated(savedInstanceState)
	 viewModel = ViewModelProvider(this).get(AddStudentViewModel::class.java)

	 /*Observers */
	 viewModel._courses.observe(this.viewLifecycleOwner, Observer { courses ->
		Log.i("ADDSTUDENT","THIS COURSES: ${courses}")
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
		  this.waiting_dialog= Utils.showWaitingDialog(context!!,"Agregando Niño...")
		}
		else{
		  this.waiting_dialog?.dismiss()
		}
	 })

	 viewModel.getShowSuccessDialog().observe(this.viewLifecycleOwner, Observer { value ->
		if(value){
		  Utils.showSuccessDialog(context!!,"Niño Agregado Exitosamente","Se ha agregado al/la niño/niña de manera exitosa!"){
			 dialogInterface, _ ->
			 	clearFields()
			 	dialogInterface.dismiss()
		  }
		}
	 })

	 viewModel.getShowErrorDialog().observe(this.viewLifecycleOwner, Observer { value ->
		if(value){
		  Utils.showErrorDialog(context!!,"Error","Hubo un problema al guardar, intente nuevamente"){
				dialogInterface, _ -> dialogInterface.dismiss()
		  }
		}
	 })

	 /* Listeners */

	 btn_add_pic.setOnClickListener {
		val intent= Intent(Intent.ACTION_GET_CONTENT)
		intent.setType("image/*")
		startActivityForResult(intent,IMAGE_FILE_REQUEST)
	 }

	 et_dob.addTextChangedListener {editable ->

		Log.i("ADDSTUDENT","New Text: ${editable.toString()}")
		Log.i("ADDSTUDENT","MATCHES: ${Pattern.matches(date_regex,editable.toString())}")
		if(Pattern.matches(date_regex,editable.toString())){
			val date = Utils.parseDateFromString(editable.toString())
		  date?.let {
			 val years= Utils.getDiffYears(date, Date())
			 tv_age.text= "$years años"
			 if(viewModel.reorderCoursesWithStudentAge(years)) {
				spinner_courses.isEnabled = true
				spinner_courses.isClickable = true
			 }
		  }
		}

	 }

	 btn_add_student.setOnClickListener {
		viewModel.is_valid_form= et_name.validate("No puede dejar este campo vacío"){ !it.isNullOrBlank() }
		viewModel.is_valid_form= et_lname.validate("No puede dejar este campo vacío"){ !it.isNullOrBlank()}
		viewModel.is_valid_form= et_address.validate("No puede dejar este campo vacío"){ !it.isNullOrBlank()}
		viewModel.is_valid_form= et_phone.validate("Ingrese un número válido"){ Pattern.matches(phone_regex,it) }
		viewModel.is_valid_form= et_dob.validate("Ingrese una fecha valida en este formato: dd/mm/aaaa"){
		  Pattern.matches(date_regex,it)
		}

		if(viewModel.is_valid_form){
		  Log.i("ADDSTUDENT","FORM IS VALID!")
		  viewModel.st_name= et_name.text.toString()
		  viewModel.st_lname= et_lname.text.toString()
		  viewModel.st_address= et_address.text.toString()
		  viewModel.st_phone= et_phone.text.toString()
		  viewModel.st_dob= et_dob.text.toString()
		  viewModel.selected_course= spinner_courses.selectedItem as Course

		  viewModel.setShowWaitingDialog(true)

		  this.launch {
			 val new_id= viewModel.addStudent()
			 if(new_id >0){
				viewModel.student_pic_uri?.let { uri ->
				  val filename= "${viewModel.st_lname}_${viewModel.st_name}.jpg".replace(" ","_")
				  val path = savePictureToStorage(uri, filename)
				  Log.i("ADDSTUDENT", "image_path: $path")
				  path?.let {
					 viewModel.addPictureToStudent(new_id.toInt(), path)
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

  private fun savePictureToStorage(uri: Uri,picture_name:String) : String?{
	 val wrapper= ContextWrapper(context!!)

	 val file_dir= wrapper.getDir("students_pictures", Context.MODE_PRIVATE)
	 val image_file= File(file_dir,picture_name)

	 var fos: FileOutputStream?= null
	 try {
		fos= FileOutputStream(image_file)

		val bitmap= Utils.getBitmapFromUri(uri,context!!).compress(Bitmap.CompressFormat.JPEG,100,fos)

		return image_file.absolutePath

	 }catch (ex:Exception){
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
  }

  /* EditText Extension */

  fun EditText.validate(error_message: String, validator: (String) -> Boolean) : Boolean {
	 val validation = validator(this.text.toString())
	 this.error = if (validation) null else error_message
	 return validation
  }


}
