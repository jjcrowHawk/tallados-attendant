package org.cbe.talladosatendant.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.BarEntry
import kotlinx.android.synthetic.main.fragment_attendance_report.*
import kotlinx.coroutines.*

import org.cbe.talladosatendant.R
import org.cbe.talladosatendant.adapters.StudentAttendanceInfoAdapter
import org.cbe.talladosatendant.util.Utils
import org.cbe.talladosatendant.viewmodels.AttendanceReportViewModel
import org.cbe.talladosatendant.viewmodels.BaseViewModelFactory
import org.cbe.talladosatendant.viewmodels.SharedViewModel
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class AttendanceReportFragment : Fragment(), CoroutineScope {

    companion object {
        fun newInstance() = AttendanceReportFragment()
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var viewModel: AttendanceReportViewModel
    private lateinit var shared_view_model: SharedViewModel

    private lateinit var tv_title: TextView
    private lateinit var tv_presents: TextView
    private lateinit var tv_absents: TextView
    private lateinit var tv_sicks: TextView
    private lateinit var tv_unknowns: TextView
    private lateinit var btn_hide_stats: TextView
    private lateinit var btn_show_graphic: TextView
    private lateinit var btn_show_comment: TextView
    private lateinit var rv_students_atts: RecyclerView
    private lateinit var layout_stats: LinearLayout

    private var is_stats_hidden: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view= inflater.inflate(R.layout.fragment_attendance_report, container, false)

        tv_title= view.findViewById(R.id.tv_title_att_report)
        tv_presents= view.findViewById(R.id.tv_present_att_report)
        tv_absents= view.findViewById(R.id.tv_absent_att_report)
        tv_sicks= view.findViewById(R.id.tv_sick_att_report)
        tv_unknowns= view.findViewById(R.id.tv_unknown_att_report)
        btn_hide_stats= view.findViewById(R.id.btn_hide_stats_att_report)
        btn_show_graphic= view.findViewById(R.id.btn_show_graphic_att_report)
        btn_show_comment= view.findViewById(R.id.btn_show_comment_att_report)
        rv_students_atts= view.findViewById(R.id.rv_students_attendances_att_report)
        layout_stats= view.findViewById(R.id.layout_stats)

        rv_students_atts.layoutManager= LinearLayoutManager(context!!)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)



        shared_view_model = activity?.run {
            ViewModelProvider(this).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        val course_att = shared_view_model.getSelectedCourse().value

        viewModel = ViewModelProvider(this,BaseViewModelFactory{ AttendanceReportViewModel(activity!!.application,course_att!!.attendance!!.attendance_id)})
            .get(AttendanceReportViewModel::class.java)

        rv_students_atts.adapter= StudentAttendanceInfoAdapter(context!!)

        //OBSERVERS

        /*shared_view_model.getSelectedCourse().observe(this.viewLifecycleOwner, Observer { course_att ->
            Log.i("ATTREPORT","course_Selected: ${course_att}")
            tv_title.text= "Asistencia del ${Utils.getFormattedLatinDate(course_att.attendance!!.date)} - ${course_att.courses!![0]!!.name}"
            viewModel = ViewModelProvider(this,BaseViewModelFactory{ AttendanceReportViewModel(activity!!.application,course_att.attendance!!.attendance_id)})
                .get(AttendanceReportViewModel::class.java)
        })*/

        tv_title.text= "Asistencia del ${Utils.getFormattedLatinDate(course_att!!.attendance!!.date)} - ${course_att.courses!![0]!!.name}"

        viewModel?.records?.observe(this.viewLifecycleOwner, Observer {
            Log.i("ATTREPORT", "records: $it")
            launch {
                val deferred= async(Dispatchers.Default){
                    viewModel.getRecordsStats()
                }

                deferred.await()

                tv_presents.text= viewModel.stat_presents.toString()
                tv_absents.text= viewModel.stat_absents.toString()
                tv_sicks.text= viewModel.stat_sicks.toString()
                tv_unknowns.text= viewModel.stat_unknown.toString()

                (rv_students_atts.adapter as StudentAttendanceInfoAdapter).setItems(it)
            }
        })

        viewModel.getShowAttendanceGraph().observe(this.viewLifecycleOwner, Observer {value ->
            if(value){
                val present_percentage= if(viewModel.stat_presents != 0)  (viewModel.stat_presents.toFloat() / viewModel.stat_total.toFloat()) * 100 else 0f
                val absent_percentage= if(viewModel.stat_absents != 0) (viewModel.stat_absents.toFloat() / viewModel.stat_total.toFloat()) * 100 else 0f
                val sick_percentage= if(viewModel.stat_sicks != 0) (viewModel.stat_sicks.toFloat() / viewModel.stat_total.toFloat()) * 100 else 0f
                val unk_percentage= if(viewModel.stat_unknown != 0) (viewModel.stat_unknown.toFloat() / viewModel.stat_total.toFloat()) * 100 else 0f

                val data: List<Float> = listOf(present_percentage,absent_percentage,sick_percentage,unk_percentage)
                val labels: List<String> = listOf(
                    getString(R.string.present_state),
                    getString(R.string.absent_state),
                    getString(R.string.sick_state),
                    getString(R.string.unknown_state)
                )

                val colors: List<Int> = listOf(
                    ContextCompat.getColor(context!!,R.color.presentState),
                    ContextCompat.getColor(context!!,R.color.absentState),
                    ContextCompat.getColor(context!!,R.color.sickState),
                    ContextCompat.getColor(context!!,R.color.unknownState)
                )

                Utils.showBarChartDialog(context!!,"Gráfico de Asistencias en porcentajes",data,labels,colors){
                    dialog,_ ->
                        dialog.dismiss()
                        viewModel.setShowAttendanceGraph(false)
                }
            }
        })

        viewModel.getShowAttendanceComment().observe(this.viewLifecycleOwner, Observer {value ->
            if(value){
                Utils.showSuccessDialog(context!!,"Comentarios Sobre la Asistencia",course_att.attendance!!.observations){
                    dialog,_ ->
                        dialog.dismiss()
                        viewModel.setShowAttendanceComment(false)
                }
            }
        })



        //EVENTS

        btn_hide_stats.setOnClickListener {
            if(!is_stats_hidden){
                layout_stats.visibility = View.GONE
                btn_show_graphic.visibility= View.GONE
                btn_show_comment.visibility= View.GONE
                btn_hide_stats.text = getString(R.string.show_button)
                is_stats_hidden= true
            }
            else{
                layout_stats.visibility = View.VISIBLE
                btn_show_graphic.visibility= View.VISIBLE
                btn_show_comment.visibility= View.VISIBLE
                btn_hide_stats.text = getString(R.string.hide_button)
                is_stats_hidden= false
            }
        }

        btn_show_graphic.setOnClickListener {
            viewModel.setShowAttendanceGraph(true)
        }

        btn_show_comment.setOnClickListener {
            viewModel.setShowAttendanceComment(true)
        }

    }

}
