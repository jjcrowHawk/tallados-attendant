package org.cbe.talladosatendant.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.cbe.talladosatendant.util.Utils
import java.util.*

@Entity(tableName= "student")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val student_id: Int = 0,
    @ColumnInfo(name = "first_name")
    val name: String ="",
    val last_name: String = "",
    val address: String = "",
    val phone: String ="",
    val dob: Date =Date(),
    val picture: String = "",
    val active: Boolean= true

){
    @Ignore var age: Int = -1
    val full_name : String
        get() = "$last_name $name"

    override fun toString(): String {
        return "$full_name"
    }


}