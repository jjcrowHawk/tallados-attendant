package org.cbe.talladosatendant.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.AttendanceStatus
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.pojo.AttendanceRecordStudent

class StudentAttendanceEditAdapter(private val context: Context) : RecyclerView.Adapter<StudentAttendanceEditAdapter.ViewHolder>() {

  private var students_records_list: List<AttendanceRecordStudent> = ArrayList()
  private var map_students_attendance: MutableMap<Student,String> = HashMap<Student,String>()


  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
	 val layoutInflater= LayoutInflater.from(parent.context)
	 //vholder= ViewHolder(layoutInflater.inflate(R.layout.attendance_take_item,parent,false))
	 return ViewHolder(layoutInflater.inflate(R.layout.attendance_take_item,parent,false))
  }

  override fun getItemCount(): Int {
	 return students_records_list.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
	 val item = students_records_list[position]
	 if (position == students_records_list.lastIndex){
		holder.itemView.findViewById<LinearLayout>(R.id.item_take_att_layout_parent).setPadding(0,0,0,150)
	 } else{
		holder.itemView.findViewById<LinearLayout>(R.id.item_take_att_layout_parent).setPadding(0,0,0,0)
	 }
	 holder.bind(item,position)
  }

  fun setItems(students_records: List<AttendanceRecordStudent>){
	 students_records_list= students_records
	 map_students_attendance.clear()
	 students_records_list.forEach { st_record->
		val status= st_record.status?.get(0)?.status ?: ""
		map_students_attendance[st_record.student!![0]] = status
	 }
  }

  fun getStudentAttendancesStatuses(): MutableMap<Student,String>{
	 return map_students_attendance
  }



  fun clearItems(){
	 (students_records_list as ArrayList<String>?)?.clear()
	 map_students_attendance.clear()
  }

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
	 //TODO set STUDENT IMAGE
	 val tv_student_name : TextView = view.findViewById(R.id.ati_tv_student_name)
	 val iv_student_picture: ImageView = view.findViewById(R.id.ati_iv_student_image)
	 val rg_statuses: RadioGroup = view.findViewById(R.id.ati_rg_statuses)
	 val rb_present: RadioButton = view.findViewById(R.id.ati_radio_present)
	 val rb_absent: RadioButton = view.findViewById(R.id.ati_radio_absent)
	 val rb_sick: RadioButton = view.findViewById(R.id.ati_radio_sick)
	 val rb_unkown: RadioButton = view.findViewById(R.id.ati_radio_unknown)

	 fun bind(student_record: AttendanceRecordStudent, item_pos: Int){
		val student= students_records_list[item_pos].student!![0]
		tv_student_name.text = "${student.name} ${student.last_name}"
		rg_statuses.clearCheck()

		if(map_students_attendance.containsKey(student)){
		  when(map_students_attendance[student]){
			 "present" -> rb_present.isChecked= true
			 "absent" -> rb_absent.isChecked= true
			 "sick" -> rb_sick.isChecked= true
			 "unknown" -> rb_unkown.isChecked= true
			 "" -> rg_statuses.clearCheck()
		  }
		}

		rg_statuses.setOnCheckedChangeListener {
			 radioGroup, id ->
		  Log.i("RADIOCHECK","selected $id")
		  val checkedRadio = radioGroup.findViewById(id) as RadioButton?
		  checkedRadio?.let {
			 val isChecked: Boolean = checkedRadio?.isChecked!!
			 if (isChecked) {
				map_students_attendance[student] = checkedRadio.contentDescription.toString()
				Log.i("RADIOCHECK", "key ${map_students_attendance[student]}")
			 }
		  }
		}
	 }
  }
}