package org.cbe.talladosatendant.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.pojo.AttendanceRecordStudent
import org.cbe.talladosatendant.util.AttendanceRepository

class AttendanceReportViewModel(application: Application,val attendance_id: Int) : AndroidViewModel(application) {

    private val repository: AttendanceRepository
    lateinit var records: LiveData<List<AttendanceRecordStudent>>

    var stat_total= 0
    var stat_presents= 0
    var stat_absents= 0
    var stat_sicks= 0
    var stat_unknown= 0

    private val showAttendanceGraph: MutableLiveData<Boolean> = MutableLiveData(false)
    private val showAttendanceComment: MutableLiveData<Boolean> = MutableLiveData(false)

    // THIS COULD BE ACHIEVED BETTER WITH FACTORY VIEWMODEL
    init {
        val database = AttendanceDatabase.getDatabase(application,viewModelScope)
        repository= AttendanceRepository(database)
        records= repository.getAttendanceRecordsFromAttendance(attendance_id)
    }

    fun getRecordsStats(){
        val records_students= records.value
        records_students?.let {
            val status_att_map= records_students.groupBy {
                it.status!![0].status
            }
            this.stat_presents= status_att_map["present"]?.size ?: 0
            this.stat_absents= status_att_map["absent"]?.size ?: 0
            this.stat_sicks= status_att_map["sick"]?.size ?: 0
            this.stat_unknown= status_att_map["unknown"]?.size ?: 0
            this.stat_total= records_students.size
        } ?: run{
            Log.i("ATRPRTVM", "NO records found yet!")
        }
    }


    fun setShowAttendanceGraph(value: Boolean){ showAttendanceGraph.value= value }

    fun getShowAttendanceGraph(): LiveData<Boolean>{return showAttendanceGraph}

    fun setShowAttendanceComment(value: Boolean){ showAttendanceComment.value= value}

    fun getShowAttendanceComment(): LiveData<Boolean>{return showAttendanceComment}

}
