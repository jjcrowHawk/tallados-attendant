package org.cbe.talladosatendant.databases.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import org.cbe.talladosatendant.databases.entities.Student
import java.util.*

@Dao
interface StudentDao {

    @Query("SELECT * FROM student WHERE active = 1")
    fun getStudents(): LiveData<List<Student>>

    @Query("SELECT student.* FROM student " +
            "INNER JOIN student_course ON student_course.student = student.student_id " +
            "INNER JOIN course ON course.course_id = student_course.course " +
            "WHERE course.course_id = :course_id ORDER BY student.last_name")
    fun getStudentsByCourse(course_id:Int) : LiveData<List<Student>>

    @Query("SELECT student.* FROM student " +
            "INNER JOIN student_course ON student_course.student = student.student_id " +
            "INNER JOIN course ON course.course_id = student_course.course " +
            "WHERE course.course_id = :course_id ORDER BY student.last_name")
    suspend fun getStudentsByCourseSync(course_id:Int) : List<Student>

    @Query("SELECT * FROM student WHERE student_id = :id AND active= 1")
    fun getStudent(id:Int) : Student

    @Query("SELECT * FROM student WHERE student_id = :id AND active = 1 ")
    suspend fun getStudentSync(id:Int) : Student

    @Query("SELECT * FROM student WHERE dob BETWEEN :from AND :to ORDER BY last_name")
    fun getStudentsOnBirthday(from: Date, to: Date) : LiveData<List<Student>>

    @Query("SELECT * FROM student WHERE strftime('%m',dob) = strftime('%m',date('now')) ORDER BY last_name")
    fun getStudentsOnBirthDayFromMonth() : LiveData<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg students: Student) :List<Long>

    @Update
    suspend fun update(vararg students:Student) : Int

}