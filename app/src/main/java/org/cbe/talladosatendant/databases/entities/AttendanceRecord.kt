package org.cbe.talladosatendant.databases.entities

import androidx.room.*

@Entity(
    tableName = "attendance_record",
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Student::class,
            parentColumns = arrayOf("student_id"),
            childColumns = arrayOf("student")
        ),
        ForeignKey(
            entity = Attendance::class,
            parentColumns = arrayOf("attendance_id"),
            childColumns = arrayOf("attendance")
        ),
        ForeignKey(
            entity = AttendanceStatus::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("status")
        )
    )
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val record_id: Int= 0,
    val observations: String="",
    val student: Int,
    val attendance: Int,
    val status: Int
)

