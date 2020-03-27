package org.cbe.talladosatendant.databases.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_status")
data class AttendanceStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Int= 0,
    val status : String="present",
    val icon: String="",
    val active: Boolean=true
)