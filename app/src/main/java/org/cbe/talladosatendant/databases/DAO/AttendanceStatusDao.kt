package org.cbe.talladosatendant.databases.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import org.cbe.talladosatendant.databases.entities.AttendanceStatus

@Dao
interface AttendanceStatusDao {

    @Query("SELECT * FROM attendance_status")
    fun getAttendanceStatuses(): LiveData<List<AttendanceStatus>>

    @Query("SELECT * FROM attendance_status WHERE id = :status_id")
    fun getAttendanceStatusByID(status_id:Int): LiveData<AttendanceStatus>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attendance_status: AttendanceStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg attendance_statuses: AttendanceStatus)

    @Update
    suspend fun update(vararg attendance_statuses: AttendanceStatus)
}