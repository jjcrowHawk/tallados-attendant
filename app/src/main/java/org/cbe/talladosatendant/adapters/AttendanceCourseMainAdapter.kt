package org.cbe.talladosatendant.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Course

internal class AttendanceCourseMainAdapter internal constructor(context: Context, private val resource: Int, private var itemsList:List<Course>?) : ArrayAdapter<AttendanceCourseMainAdapter.ItemHolder>(context, resource) {

    override fun getCount(): Int {
        return if(this.itemsList != null) this!!.itemsList!!.size else 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView


        var holder: ItemHolder

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(resource,null)
            convertView!!.setClickable(false)
            convertView!!.setFocusable(false)
            holder = ItemHolder()
            holder.course_name = convertView!!.findViewById(R.id.tv_course_asigned_attendance)
            holder.course_image = convertView!!.findViewById(R.id.iv_course_assigned_main)
            holder.button_take_attendance= convertView!!.findViewById(R.id.btn_course_assigned_main)
            convertView.tag= holder
        } else {
            holder= convertView.tag as ItemHolder
        }

        holder.course_name!!.text = this.itemsList!![position].name
        holder.button_take_attendance!!.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {

            }
        })

        return convertView
    }

    internal fun setCourseList(courses: List<Course>){
        this.itemsList= courses
        notifyDataSetChanged()

    }

    internal class ItemHolder{
        var course_name: TextView? = null
        var course_image: ImageView? = null
        var button_take_attendance: Button? = null
    }
}