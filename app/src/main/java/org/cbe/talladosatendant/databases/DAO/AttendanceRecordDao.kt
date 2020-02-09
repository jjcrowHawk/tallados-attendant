package org.cbe.talladosatendant.databases.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import org.cbe.talladosatendant.databases.entities.AttendanceRecord
import org.cbe.talladosatendant.pojo.AttendanceRecordStudent
import java.util.*

@Dao
interface AttendanceRecordDao {

    @Query("SELECT * FROM attendance_record " +
            "INNER JOIN attendance ON attendance.attendance_id = attendance_record.attendance " +
            "WHERE attendance.course = :course_id")
    fun getRecordsFromCourse(course_id:Int) : List<AttendanceRecord>

    @Query("SELECT * FROM attendance_record WHERE student = :student_id")
    fun getRecordsFromStudent(student_id:Int): LiveData<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_record INNER JOIN attendance ON attendance.attendance_id = attendance_record.attendance  WHERE attendance_record.student = :student_id AND attendance.date BETWEEN :since AND :to")
    fun getRecordsFromStudentSinceDate(student_id:Int,since: Date, to:Date) : LiveData<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_record WHERE attendance = :attendance_id")
    fun getRecordsFromAttendance(attendance_id:Int): LiveData<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_record WHERE attendance = :attendance_id")
    fun getRecordsFromAttendanceWithStudent(attendance_id: Int) : LiveData<List<AttendanceRecordStudent>>

    @Query("SELECT * FROM attendance_record WHERE attendance = :attendance_id")
    suspend fun getSynRecordsFromAttendanceWithStudent(attendance_id: Int) : List<AttendanceRecordStudent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecord) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg records: AttendanceRecord) : List<Long>

    @Update
    suspend fun update(vararg records:AttendanceRecord) : Int
}