package org.cbe.talladosatendant.databases.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import org.cbe.talladosatendant.databases.entities.StudentCourse

@Dao
interface StudentCourseDao {

    @Query("SELECT * FROM student_course WHERE course = :course_id")
    fun getStudentCoursesByCourse(course_id:Int): LiveData<List<StudentCourse>>

    @Query("SELECT * FROM student_course WHERE student = :student_id")
    fun getStudentCoursesByStudent(student_id:Int): LiveData<List<StudentCourse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(studentCourse: StudentCourse)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg studentcourses: StudentCourse)

    @Update
    suspend fun update(vararg studentcourses:StudentCourse)
}