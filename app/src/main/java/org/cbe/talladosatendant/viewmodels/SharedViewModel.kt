package org.cbe.talladosatendant.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.cbe.talladosatendant.pojo.CourseAttendance

class SharedViewModel: ViewModel() {

    private val selected_attendance : MutableLiveData<CourseAttendance> = MutableLiveData()

    fun setSelectedCourse(c: CourseAttendance){
        selected_attendance.value= c
    }

    fun getSelectedCourse() : LiveData<CourseAttendance> {
        return selected_attendance
    }
}