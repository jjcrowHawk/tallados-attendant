package org.cbe.talladosatendant.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.pojo.CourseAttendance
import org.cbe.talladosatendant.pojo.AttendanceSectionedModel
import org.cbe.talladosatendant.util.AttendanceRepository
import org.cbe.talladosatendant.util.Utils
import java.util.*

class ReviewAttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository
    private lateinit var attendances: LiveData<List<CourseAttendance>>
    var attendances_sectioned_list: MediatorLiveData<List<AttendanceSectionedModel>> = MediatorLiveData()


    val courses: LiveData<List<Course>>

    init {
        val database= AttendanceDatabase.getDatabase(application,viewModelScope)
        repository= AttendanceRepository(database)
        courses= repository.courses

    }

    fun getAttendances(course: Int){
        val calendar= Calendar.getInstance()

        val year= calendar.get(Calendar.YEAR)

        val cal_date_start= Calendar.getInstance()
        cal_date_start.set(year,Calendar.JANUARY,1)
        val cal_date_end= Calendar.getInstance()
        cal_date_end.set(year,Calendar.DECEMBER,31)

        attendances= repository.getPastAttendancesFromCourse(cal_date_start.time,cal_date_end.time,course)

        attendances_sectioned_list.addSource(attendances, Observer {
            val list_sec_att= it.groupBy {
                val cal= Calendar.getInstance()
                cal.time = it.attendance?.date
                Utils.getMonthNameFromNumber(cal.get(Calendar.MONTH))
            }.map {
                    entry -> AttendanceSectionedModel(entry.key,entry.value)
            }
            attendances_sectioned_list.postValue(list_sec_att.reversed())
        })

        /*attendances_sectioned_list  = Transformations.map(attendances){
            it.groupBy {
                val cal= Calendar.getInstance()
                cal.time = it.date
                Utils.getMonthNameFromNumber(cal.get(Calendar.MONTH))
            }.map {
                    entry -> AttendanceSectionedModel(entry.key,entry.value)
            }
        }*/
    }
}

