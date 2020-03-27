package org.cbe.talladosatendant.pojo

import androidx.room.Embedded
import androidx.room.Relation
import org.cbe.talladosatendant.databases.entities.AttendanceRecord
import org.cbe.talladosatendant.databases.entities.AttendanceStatus
import org.cbe.talladosatendant.databases.entities.Student

class AttendanceRecordWithStatus {
    @Embedded
    var record: AttendanceRecord? = null
    @Relation(parentColumn = "status", entityColumn = "id", entity = AttendanceStatus::class)
    var status: List<AttendanceStatus>? = null

    override fun toString(): String {
        return "AttendanceRecordStudent(record=$record, status=$status)"
    }
}