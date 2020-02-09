package org.cbe.talladosatendant.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Student
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class StudentAttendanceTakingAdapter(val context: Context) : RecyclerView.Adapter<StudentAttendanceTakingAdapter.ViewHolder>() {

    private var students_list: List<Student> = ArrayList<Student>()
    private var map_students_attendance: MutableMap<Student,String> = HashMap<Student,String>()
    //private lateinit var vholder: ViewHolder


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        //vholder= ViewHolder(layoutInflater.inflate(R.layout.attendance_take_item,parent,false))
        return ViewHolder(layoutInflater.inflate(R.layout.attendance_take_item,parent,false))
    }

    override fun getItemCount(): Int {
        return students_list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = students_list[position]
        if (position == students_list.lastIndex){
            holder.itemView.findViewById<LinearLayout>(R.id.item_take_att_layout_parent).setPadding(0,0,0,150)
        } else{
            holder.itemView.findViewById<LinearLayout>(R.id.item_take_att_layout_parent).setPadding(0,0,0,0)
        }
        holder.bind(item,position)
    }

    fun setItems(students: List<Student>){
        students_list= students
        map_students_attendance.clear()
        students_list.forEach { student -> map_students_attendance[student] = "absent"}
    }

    fun getStudentAttendancesStatuses(): MutableMap<Student,String>{
        return map_students_attendance
    }



    fun clearItems(){
        (students_list as ArrayList<String>?)?.clear()
        map_students_attendance.clear()
        //vholder?.let { it.rg_statuses.clearCheck() }
    }

   inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tv_student_name : TextView = view.findViewById(R.id.ati_tv_student_name)
        val iv_student_picture: ImageView = view.findViewById(R.id.ati_iv_student_image)
        val rg_statuses: RadioGroup = view.findViewById(R.id.ati_rg_statuses)
        val rb_present: RadioButton = view.findViewById(R.id.ati_radio_present)
        val rb_absent: RadioButton = view.findViewById(R.id.ati_radio_absent)
        val rb_sick: RadioButton = view.findViewById(R.id.ati_radio_sick)
        val rb_unkown: RadioButton = view.findViewById(R.id.ati_radio_unknown)

        fun bind(student: Student, item_pos: Int){
            tv_student_name.text = "${student.name} ${student.last_name}"
            rg_statuses.clearCheck()
            rg_statuses.setOnCheckedChangeListener {
                    radioGroup, id ->
                        Log.i("RADIOCHECK","selected $id")
                        val checkedRadio = radioGroup.findViewById(id) as RadioButton?
                        checkedRadio?.let {
                            val isChecked: Boolean = checkedRadio?.isChecked!!
                            if (isChecked) {
                                map_students_attendance[students_list[item_pos]] =
                                    checkedRadio.contentDescription.toString()
                                Log.i("RADIOCHECK", "key ${map_students_attendance[students_list[item_pos]]}")
                            }
                        }
            }
        }
    }
}