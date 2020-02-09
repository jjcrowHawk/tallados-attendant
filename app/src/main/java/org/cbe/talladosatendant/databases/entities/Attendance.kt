package org.cbe.talladosatendant.databases.entities

import androidx.room.*
import java.util.*

@Entity(
    tableName = "attendance",
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Course::class,
            parentColumns = arrayOf("course_id"),
            childColumns = arrayOf("course"))
    )
)
data class Attendance (
    @PrimaryKey(autoGenerate = true)
    val attendance_id: Int = -1,
    val name: String = "attendance",
    val description: String = "Attendance of today",
    val date: Date = Date(),
    val observations: String = "",
    val course: Int = -1,
    val taken: Boolean=false,
    val active: Boolean=true
)

