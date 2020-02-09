package org.cbe.talladosatendant.databases.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import org.cbe.talladosatendant.databases.entities.Course

@Dao
interface CourseDao {

    @Query("SELECT * FROM course")
    fun getCourses() : LiveData<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg courses: Course)

    @Update
    suspend fun update(vararg courses: Course)
}