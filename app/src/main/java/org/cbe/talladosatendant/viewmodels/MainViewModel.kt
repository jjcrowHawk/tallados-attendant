package org.cbe.talladosatendant.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.Course
import org.cbe.talladosatendant.pojo.CourseAttendance
import org.cbe.talladosatendant.databases.entities.Student
import org.cbe.talladosatendant.util.AttendanceRepository

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    val assignedCourses : LiveData<List<Course>>
    val pastMonthAttendances: LiveData<List<CourseAttendance>>
    val currentMonthBirthdayStudents: LiveData<List<Student>>


    init {
        Log.i("MainViewModel","===> INITING DB")
        val database= AttendanceDatabase.getDatabase(application,viewModelScope)
        Log.i("MainViewModel","===> INITED DB?")
        repository= AttendanceRepository(database)
        assignedCourses= repository.courses
        Log.i("MAINVIEWMODEL","courses: ${assignedCourses.value}")
        pastMonthAttendances= repository.attendancesFromLastMonth()
        currentMonthBirthdayStudents= repository.getBirthDaysOfMonth()
    }
}