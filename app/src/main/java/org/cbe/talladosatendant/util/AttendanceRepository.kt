package org.cbe.talladosatendant.util

import android.util.Log
import androidx.lifecycle.LiveData
import org.cbe.talladosatendant.databases.AttendanceDatabase
import org.cbe.talladosatendant.databases.entities.*
import org.cbe.talladosatendant.pojo.AttendanceRecordStudent
import org.cbe.talladosatendant.pojo.CourseAttendance
import java.util.*

class AttendanceRepository(private val database: AttendanceDatabase) {

    /**
     * Main data Repository
     */

    val courses : LiveData<List<Course>> = database.courseDao().getCourses()

    fun attendancesFromLastMonth() : LiveData<List<CourseAttendance>>{
        Log.i("REPOSITORY","GETTING LAST ATTENDANCES")
        val nowDate= Date()
        val calendar : Calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH,-1)
        val pasthMonthDate= calendar.time
        return database.attendanceDao().getCourseAttendances(since= pasthMonthDate, to= nowDate)
    }

    fun getBirthDaysOfMonth() : LiveData<List<Student>>{
        Log.i("REPOSITORY","GETTING BIRTHDAYS")
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, (cal[Calendar.DAY_OF_MONTH] - 1) * -1)
        val firstDayDate : Date = cal.time

        val cal2 = Calendar.getInstance()
        cal2.add(
            Calendar.DAY_OF_MONTH,
            cal2.getActualMaximum(Calendar.DAY_OF_MONTH) - cal2[Calendar.DAY_OF_MONTH]
        )
        val lastDayDate : Date = cal2.time

        return database.studentDao().getStudentsOnBirthDayFromMonth()
    }

    /**
     * Take Attendance Repository
     */

    fun getUntakenAttendancesFromCourse(course_id:Int) : LiveData<List<Attendance>>{
        return database.attendanceDao().getUntakenAttendanceFromCourse(course_id)
    }

    fun getStudentsFromCourse(course_id: Int): LiveData<List<Student>>{
        return database.studentDao().getStudentsByCourse(course_id)
    }

    fun getAttendanceStatuses(): LiveData<List<AttendanceStatus>>{
        return database.attendanceStatusDao().getAttendanceStatuses()
    }

    suspend fun instertAttendanceRecords(records: MutableList<AttendanceRecord>) : List<Long>{
        return database.attendanceRecordDao().insertAll(*records.toTypedArray())
    }

    suspend fun updateAttendance(attendance: Attendance) : Int{
        return database.attendanceDao().update(attendance)
    }

    /**
     * Review Attendance
     */

    fun getPastAttendancesFromCourse(fromDate: Date, toDate: Date, course: Int) : LiveData<List<CourseAttendance>>{
        return database.attendanceDao().getCourseAttendancesFromCourse(since=fromDate,to = toDate,course_id = course)
    }

    /**
     * Attendance Report
     */

    fun getAttendanceRecordsFromAttendance(attendance_id: Int): LiveData<List<AttendanceRecordStudent>>{
        return database.attendanceRecordDao().getRecordsFromAttendanceWithStudent(attendance_id)
    }

    /**
     * Attendance Export
     */
    suspend fun getAttendancesFromCourseWithDateRange(course_id: Int, date_from:Date, date_to:Date) : List<Attendance>{
        return database.attendanceDao().getSyncAttendancesFromCourse(date_from,date_to,course_id)
    }

    suspend fun getRecordsFromAttendance(attendance_id: Int) : List<AttendanceRecordStudent>{
        return database.attendanceRecordDao().getSynRecordsFromAttendanceWithStudent(attendance_id)
    }
}