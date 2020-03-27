package org.cbe.talladosatendant.databases.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val course_id: Int = 0,
    val name: String= "Course NN",
    val icon: String= "/",
    val min_year_student: Int= -1,
    val max_year_student: Int= -1,
    val active: Boolean = true
){
    override fun toString(): String {
        return name
    }
}