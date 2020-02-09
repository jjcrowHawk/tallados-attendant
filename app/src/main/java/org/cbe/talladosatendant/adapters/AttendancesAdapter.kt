package org.cbe.talladosatendant.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.pojo.CourseAttendance
import org.cbe.talladosatendant.ui.AttendanceReportFragment
import org.cbe.talladosatendant.util.Utils
import org.cbe.talladosatendant.viewmodels.SharedViewModel
import java.util.*

class AttendancesAdapter (val context: Context, val itemsList: List<CourseAttendance>, val view_model: SharedViewModel) : RecyclerView.Adapter<AttendancesAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return if (this.itemsList != null) this!!.itemsList!!.size else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendancesAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.passed_attendance_main_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendancesAdapter.ViewHolder, position: Int) {
        val item= itemsList[position]
        holder.bind(item,position)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var course_name: TextView? = itemView.findViewById(R.id.group_tv_pami)
        var date: TextView? = itemView.findViewById(R.id.date_tv_pami)
        //var attendees: TextView? = itemView.findViewById(R.id.at_num_tv_pami)
        var btn_view_attendance: TextView? = itemView.findViewById(R.id.btn_view_past_attendance)

        fun bind(item: CourseAttendance, position: Int){
            val cal = Calendar.getInstance()
            cal.time = itemsList!![position].attendance!!.date

            date!!.text = "${Utils.getDayNameFromNumber(cal.get(Calendar.DAY_OF_WEEK))}, " +
                    "${cal.get(Calendar.DAY_OF_MONTH)} de ${Utils.getMonthNameFromNumber(cal.get(Calendar.MONTH))} " +
                    "del ${cal.get(Calendar.YEAR)}"
            course_name!!.text = itemsList!![position].courses!![0].name
            btn_view_attendance!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    view_model.setSelectedCourse(item)
                    val fragment = AttendanceReportFragment()
                    (context as AppCompatActivity)
                        .supportFragmentManager
                        .beginTransaction()
                        .add(R.id.fl_main_container, fragment)
                        .commit()
                }
            })
        }
    }
}