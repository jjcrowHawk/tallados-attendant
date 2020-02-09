package org.cbe.talladosatendant.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName= "student")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val student_id: Int = 0,
    @ColumnInfo(name = "first_name")
    val name: String ="Jhon",
    val last_name: String = "Doe",
    val address: String = "ADD",
    val phone: String ="1241255",
    val dob: Date =Date(),
    val picture: String = "",
    val active: Boolean= true
)