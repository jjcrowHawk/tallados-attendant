package org.cbe.talladosatendant.databases.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "student_course",
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Student::class,
            parentColumns = arrayOf("student_id"),
            childColumns = arrayOf("student")
        ),
        ForeignKey(
            entity = Course::class,
            parentColumns = arrayOf("course_id"),
            childColumns = arrayOf("course")
        )
    )
)
data class StudentCourse(
    @PrimaryKey(autoGenerate = true)
    val sc_id: Int = 0,
    val active: Boolean=true,
    val student: Int= -1,
    val course: Int= -1,
    val created_date: Date = Date()
)