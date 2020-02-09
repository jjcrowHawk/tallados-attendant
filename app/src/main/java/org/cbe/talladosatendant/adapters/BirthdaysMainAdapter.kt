package org.cbe.talladosatendant.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.util.Utils
import java.util.*

internal class BirthdaysMainAdapter internal constructor(context: Context, private val resource: Int, private var itemsList:List<Student>?) : ArrayAdapter<BirthdaysMainAdapter.ItemHolder>(context, resource) {

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
            holder.student_name = convertView!!.findViewById(R.id.name_tv_bmi)
            holder.student_date= convertView!!.findViewById(R.id.date_tv_bmi)
            holder.student_age= convertView!!.findViewById(R.id.age_tv_bmi)
            holder.btn_view_birth_info= convertView!!.findViewById(R.id.btn_view_birthday_student)
            convertView.tag= holder
        } else {
            holder= convertView.tag as ItemHolder
        }

        val cal= Calendar.getInstance()
        cal.time= itemsList!![position].dob

        holder.student_name!!.text= itemsList!![position].name + " " + itemsList!![position].last_name
        holder.student_date!!.text= "${cal.get(Calendar.DAY_OF_MONTH)} de ${Utils.getMonthNameFromNumber(cal.get(Calendar.MONTH))} " +
                "de ${cal.get(Calendar.YEAR)}"
        holder.student_age!!.text= Utils.getDiffYears(itemsList!![position].dob,Date()).toString() + " años"
        holder.btn_view_birth_info!!.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Log.d("Birth:","Check birth")
            }
        })

        return convertView
    }

    internal fun setItems(students: List<Student>){
        this.itemsList = students
        notifyDataSetChanged()
    }

    internal class ItemHolder {
        var student_name: TextView? = null
        var student_date: TextView? = null
        var student_age: TextView? = null
        var student_picture: ImageView? = null
        var btn_view_birth_info: TextView? = null
    }
}