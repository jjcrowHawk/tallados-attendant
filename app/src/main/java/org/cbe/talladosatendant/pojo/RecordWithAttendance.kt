package org.cbe.talladosatendant.pojo

import androidx.room.Embedded
import androidx.room.Relation
import org.cbe.talladosatendant.databases.entities.Attendance
import org.cbe.talladosatendant.databases.entities.AttendanceRecord
import org.cbe.talladosatendant.databases.entities.AttendanceStatus

class RecordWithAttendance {
  @Embedded
  var record: AttendanceRecord? = null
  @Relation(parentColumn = "status", entityColumn = "id", entity = AttendanceStatus::class)
  var status: List<AttendanceStatus>? = null
  @Relation(parentColumn = "attendance", entityColumn = "attendance_id", entity = Attendance::class)
  var attendance: List<AttendanceStatus>? = null

  override fun toString(): String {
    return "RecordWIthAttendance(record=$record, status=$status, attendance=$attendance)"
  }
}