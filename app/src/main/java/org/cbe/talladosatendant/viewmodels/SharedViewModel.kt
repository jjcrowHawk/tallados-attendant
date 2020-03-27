package org.cbe.talladosatendant.viewmodels

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.cbe.talladosatendant.pojo.CourseAttendance

class SharedViewModel: ViewModel() {

    //Selected attendance
    private val selected_attendance : MutableLiveData<CourseAttendance> = MutableLiveData()

    //Student Data Fragment
    private val student_data: MutableLiveData<Map<String,Any>> = MutableLiveData()


    fun setSelectedCourse(c: CourseAttendance){
        selected_attendance.value= c
    }

    fun getSelectedCourse() : LiveData<CourseAttendance> {
        return selected_attendance
    }

    fun setStudentData(data_map:Map<String,Any>){
        this.student_data.value = data_map
    }

    fun getStudentDataLD() : LiveData<Map<String,Any>>{
        return this.student_data
    }
}