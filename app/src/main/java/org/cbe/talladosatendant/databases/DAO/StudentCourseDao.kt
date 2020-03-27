package org.cbe.talladosatendant.databases.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import org.cbe.talladosatendant.databases.entities.StudentCourse

@Dao
interface StudentCourseDao {

    @Query("SELECT * FROM student_course WHERE course = :course_id AND active = 1")
    fun getStudentCoursesByCourse(course_id:Int): LiveData<List<StudentCourse>>

    @Query("SELECT * FROM student_course WHERE student = :student_id AND active = 1")
    fun getStudentCoursesByStudent(student_id:Int): LiveData<List<StudentCourse>>

    @Query("SELECT * FROM student_course WHERE student = :student_id")
    suspend fun getStudentCourseIgnoringActiveByStudentSync(student_id:Int) : List<StudentCourse>

    @Query("SELECT * FROM student_course WHERE student = :student_id AND active = 1")
    suspend fun getStudentCoursesByStudentSync(student_id:Int): List<StudentCourse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(studentCourse: StudentCourse) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg studentcourse: StudentCourse) : List<Long>

    @Update
    suspend fun update(vararg studentcourse:StudentCourse) : Int
}