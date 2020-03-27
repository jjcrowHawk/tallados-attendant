package org.cbe.talladosatendant.databases.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import org.cbe.talladosatendant.databases.entities.Course

@Dao
interface CourseDao {

    @Query("SELECT * FROM course")
    fun getCourses() : LiveData<List<Course>>

    @Query("SELECT * FROM course WHERE course_id == :course_id")
    suspend fun getCourseSync(course_id:Int) : Course

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg courses: Course)

    @Update
    suspend fun update(vararg courses: Course)
}