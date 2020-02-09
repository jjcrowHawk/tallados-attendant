package org.cbe.talladosatendant.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.pojo.CourseAttendance
import org.cbe.talladosatendant.util.Utils
import java.util.*

internal class PastAttendanceMainAdapter internal constructor(context: Context, private val resource: Int, private var itemsList:List<CourseAttendance>?) : ArrayAdapter<PastAttendanceMainAdapter.ItemHolder>(context, resource) {

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
            holder.course_name = convertView!!.findViewById(R.id.group_tv_pami)
            holder.date= convertView!!.findViewById(R.id.date_tv_pami)
            holder.attendees= convertView!!.findViewById(R.id.at_num_tv_pami)
            holder.btn_view_attendance= convertView!!.findViewById(R.id.btn_view_past_attendance)
            convertView.tag= holder
        } else {
            holder= convertView.tag as ItemHolder
        }

        val cal= Calendar.getInstance()
        cal.time= itemsList!![position].attendance!!.date


        holder.date!!.text= "${Utils.getDayNameFromNumber(cal.get(Calendar.DAY_OF_WEEK))}, " +
                "${cal.get(Calendar.DAY_OF_MONTH)} de ${Utils.getMonthNameFromNumber(cal.get(Calendar.MONTH))} " +
                "del ${cal.get(Calendar.YEAR)}"
        holder.course_name!!.text= itemsList!![position].courses!![0].name
        holder.btn_view_attendance!!.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                print("Check past attendance")
            }
        })

        return convertView
    }

    internal fun setItems(attendances: List<CourseAttendance>){
        this.itemsList= attendances
        notifyDataSetChanged()
    }

    internal class ItemHolder {
        var course_name: TextView? = null
        var date: TextView? = null
        var attendees: TextView? = null
        var btn_view_attendance: TextView? = null
    }
}