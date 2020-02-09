package org.cbe.talladosatendant.pojo

import androidx.room.Embedded
import androidx.room.Relation
import org.cbe.talladosatendant.databases.entities.Attendance
import org.cbe.talladosatendant.databases.entities.Course

class CourseAttendance{
    @Embedded
    var attendance: Attendance? = null
    @Relation(parentColumn = "course", entityColumn = "course_id",     entity = Course::class)
    var courses: List<Course>? = null
}