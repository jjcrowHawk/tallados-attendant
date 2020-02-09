package org.cbe.talladosatendant.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.pojo.CourseAttendance
import org.cbe.talladosatendant.pojo.AttendanceSectionedModel
import org.cbe.talladosatendant.viewmodels.SharedViewModel


class SectionedRecyclerViewAdapter(val context: Context,val view_model: ViewModel? = null) : RecyclerView.Adapter<SectionedRecyclerViewAdapter.ViewHolder>() {

    private var sectionItems: List<AttendanceSectionedModel> = ArrayList<AttendanceSectionedModel>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sectionLabel : TextView = itemView.findViewById(R.id.section_label);
        val showAllButton : Button = itemView.findViewById(R.id.section_show_all_button);
        val itemRecyclerView : RecyclerView = itemView.findViewById(R.id.section_recycler_view);

        fun bind(item: AttendanceSectionedModel, position: Int){
            sectionLabel.text = item.sectionLabel
            itemRecyclerView.setHasFixedSize(true)
            itemRecyclerView.isNestedScrollingEnabled = false

            val adapter= AttendancesAdapter(context, item.itemArrayList as List<CourseAttendance>,view_model as SharedViewModel)
            itemRecyclerView.adapter = adapter
            val layout_manager=  LinearLayoutManager(context!!)
            itemRecyclerView.layoutManager= layout_manager
            val itemDecor = DividerItemDecoration(context,layout_manager.orientation)
            itemRecyclerView.addItemDecoration(itemDecor)

            showAllButton.setOnClickListener{
                
                if(itemRecyclerView.visibility != View.GONE){

                    itemRecyclerView.visibility= View.GONE
                    showAllButton.text = "Mostrar"
                }
                else{
                    itemRecyclerView.visibility = View.VISIBLE
                    showAllButton.text = "Ocultar"
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.sectioned_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return sectionItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val item= sectionItems[position]
        holder.bind(item,position)
    }

     fun setItems(items: List<AttendanceSectionedModel>){
        sectionItems= items
        notifyDataSetChanged()
    }
}