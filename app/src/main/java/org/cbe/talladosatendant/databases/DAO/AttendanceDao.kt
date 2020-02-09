package org.cbe.talladosatendant.databases.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import org.cbe.talladosatendant.databases.entities.Attendance
import org.cbe.talladosatendant.pojo.CourseAttendance
import java.util.*

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance")
    fun getAttendance() : LiveData<Attendance>

    @Query("SELECT * FROM attendance WHERE taken = 0")
    fun getUntakenAttendance() : LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE course = :course_id AND taken = 0 ORDER BY date DESC")
    fun getUntakenAttendanceFromCourse(course_id:Int) : LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE (date BETWEEN :since AND :to) AND course = :course_id ORDER BY date ASC")
    fun getAttendancesFromCourse(since: Date, to:Date,course_id:Int): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE (date BETWEEN :since AND :to) AND course = :course_id ORDER BY date ASC")
    suspend fun getSyncAttendancesFromCourse(since: Date, to:Date,course_id:Int): List<Attendance>

    @Query("SELECT * FROM attendance WHERE date BETWEEN :since AND :to")
    fun getPastAttendances(since: Date, to:Date): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date BETWEEN :since AND :to")
    fun getCourseAttendances(since: Date, to:Date): LiveData<List<CourseAttendance>>

    @Query("SELECT * FROM attendance WHERE date BETWEEN :since AND :to AND course = :course_id")
    fun getCourseAttendancesFromCourse(since: Date, to:Date,course_id: Int): LiveData<List<CourseAttendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attendance: Attendance) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg attendances: Attendance) : List<Long>

    @Update
    suspend fun update(vararg attendances:Attendance) : Int
}