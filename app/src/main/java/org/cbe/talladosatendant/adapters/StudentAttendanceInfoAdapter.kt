package org.cbe.talladosatendant.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.attendance_student_info_item.view.*
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.pojo.AttendanceRecordStudent

class StudentAttendanceInfoAdapter(val context: Context): RecyclerView.Adapter<StudentAttendanceInfoAdapter.ViewHolder>() {

    private var items : List<AttendanceRecordStudent> = ArrayList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tv_student_name= view.findViewById<TextView>(R.id.tv_student_name_asi)
        val tv_att_state= view.findViewById<TextView>(R.id.tv_attendance_state_asi)
        val iv_student_pic= view.findViewById<ImageView>(R.id.iv_student_image_asi)

        fun bind(item: AttendanceRecordStudent,context: Context){
            tv_student_name.text= "${item.student!![0]!!.name} ${item.student!![0]!!.last_name}"
            when(item.status!![0]!!.status){
                "present" -> {
                    tv_att_state.text= context.getString(R.string.present_state)
                    tv_att_state.setTextColor(ContextCompat.getColor(context,R.color.presentState))
                }
                "absent" -> {
                    tv_att_state.text= context.getString(R.string.absent_state)
                    tv_att_state.setTextColor(ContextCompat.getColor(context,R.color.absentState))
                }
                "sick" -> {
                    tv_att_state.text= context.getString(R.string.sick_state)
                    tv_att_state.setTextColor(ContextCompat.getColor(context,R.color.sickState))
                }
                "unknown" -> {
                    tv_att_state.text= context.getString(R.string.unknown_state)
                    tv_att_state.setTextColor(ContextCompat.getColor(context,R.color.unknownState))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder= ViewHolder(LayoutInflater.from(context!!).inflate(R.layout.attendance_student_info_item,parent,false))
        return holder
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item= items[position]
        holder.bind(item,context)
    }

    fun setItems(new_items: List<AttendanceRecordStudent>){
        val sorted_items= new_items.sortedBy {
            it.student!![0].last_name
        }
        items= sorted_items
        notifyDataSetChanged()
    }
}