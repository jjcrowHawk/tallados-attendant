package org.cbe.talladosatendant.databases

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.cbe.talladosatendant.databases.DAO.*
import org.cbe.talladosatendant.databases.entities.*
import java.util.*

@Database(
    entities = arrayOf(
        Student::class,
        Course::class,
        Attendance::class,
        AttendanceRecord::class,
        StudentCourse::class,
        AttendanceStatus::class),
    version = 1,
    exportSchema = false)
@TypeConverters(Converters::class)
public abstract class AttendanceDatabase: RoomDatabase() {

    abstract  fun studentDao() : StudentDao
    abstract fun courseDao() : CourseDao
    abstract fun attendanceDao() : AttendanceDao
    abstract fun attendanceRecordDao() : AttendanceRecordDao
    abstract fun studentCourseDao() : StudentCourseDao
    abstract fun attendanceStatusDao() : AttendanceStatusDao

    companion object{

        @Volatile
        private var INSTANCE: AttendanceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AttendanceDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AttendanceDatabase::class.java,
                    "attendance_database"
                )
                    .allowMainThreadQueries()
                    .addCallback(AttendanceDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    private class AttendanceDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase){
            super.onOpen(db)
            INSTANCE?.let {
                database ->
                scope.launch {
                    generateAttendance(database.attendanceDao())
                    Log.i("DATABASE","RECORDS: ${database.attendanceRecordDao().getRecordsFromCourse(2)}")
                }

            }
        }

        suspend fun populateDatabase(attendanceDatabase: AttendanceDatabase) {
            // Delete all content here.
            Log.i("DATABASE","REGISTERING DATA!")

            attendanceDatabase.courseDao().insertAll(
                Course(course_id = 1, name = "Niños de 3 - 4 años"),
                Course(course_id=2, name = "Niños de 5 - 6 años"),
                Course(course_id=3, name = "Niños de 7 - 8 años"),
                Course(course_id=4, name ="Niños de 9 - 11 años")
            )

            attendanceDatabase.studentDao().insertAll(
                Student(student_id=1 ,name="Juan Pueblo", last_name = "Pueblo", dob= GregorianCalendar(2015,Calendar.JANUARY,5).time),
                Student(student_id=2 ,name="Jeremias", last_name= "Contreras Herrera",dob= GregorianCalendar(2009,Calendar.JULY,12).time),
                Student(student_id=3 ,name="Mercedes", last_name= "Contreras Herrera",dob= GregorianCalendar(2016,Calendar.OCTOBER,12).time),
                Student(student_id=4 ,name="Valentino", last_name= "Quintero Oria",dob= GregorianCalendar(2014,Calendar.JANUARY,24).time),
                Student(student_id=5 ,name="Litsy", last_name= "Paredes Sandoya",dob= GregorianCalendar(2014,Calendar.JANUARY,25).time),
                Student(student_id=6 ,name="Scarleth ", last_name= "Quintanilla Maldonado",dob= GregorianCalendar(2013,Calendar.FEBRUARY,10).time),
                Student(student_id=7 ,name="Sharick", last_name= "Revelo",dob= GregorianCalendar(2012,Calendar.MARCH,11).time),
                Student(student_id=8 ,name="Juan Pepe", last_name = "Coello", dob= GregorianCalendar(2014,Calendar.MARCH,5).time),
                Student(student_id=9 ,name="Jostin", last_name = "Rodriguez", dob= GregorianCalendar(2014,Calendar.SEPTEMBER,10).time),
                Student(student_id=10 ,name="Lisbeth Maya", last_name = "Saona", dob= GregorianCalendar(2014,Calendar.JULY,25).time),
                Student(student_id=11 ,name="Israel", last_name = "Elizalde", dob= GregorianCalendar(2014,Calendar.JUNE,27).time)
            )

            attendanceDatabase.studentCourseDao().insertAll(
                StudentCourse(sc_id = 1, course = 2, student = 1),
                StudentCourse(sc_id = 2, course = 4, student = 2),
                StudentCourse(sc_id = 3, course = 1, student = 3),
                StudentCourse(sc_id = 4, course = 2, student = 4),
                StudentCourse(sc_id = 5, course = 2, student = 5),
                StudentCourse(sc_id = 6, course = 3, student = 6),
                StudentCourse(sc_id = 7, course = 4, student = 7),
                StudentCourse(sc_id = 8, course = 2, student = 8),
                StudentCourse(sc_id = 9, course = 2, student = 9),
                StudentCourse(sc_id = 10, course = 2, student = 10),
                StudentCourse(sc_id = 11, course = 2, student = 11)
            )

            attendanceDatabase.attendanceDao().insertAll(
                Attendance(attendance_id = 1, date = GregorianCalendar(2020,Calendar.JANUARY,11).time, course= 2),
                Attendance(attendance_id = 2, date = GregorianCalendar(2020,Calendar.JANUARY,18).time, course= 2),
                Attendance(attendance_id = 3, date = GregorianCalendar(2020,Calendar.JANUARY,25).time, course= 2),
                Attendance(attendance_id = 4, date = GregorianCalendar(2020,Calendar.JANUARY,18).time, course= 3),
                Attendance(attendance_id = 5, date = GregorianCalendar(2020,Calendar.JANUARY,25).time, course= 3),
                Attendance(attendance_id = 6, date = GregorianCalendar(2020,Calendar.JANUARY,25).time, course= 4),
                Attendance(attendance_id = 7, date = GregorianCalendar(2020,Calendar.FEBRUARY,1).time, course= 2)
            )

            attendanceDatabase.attendanceStatusDao().insertAll(
                AttendanceStatus(id=1 , status = "present"),
                AttendanceStatus(id=2 , status = "absent"),
                AttendanceStatus(id=3 , status = "sick"),
                AttendanceStatus(id=4 , status = "unknown")
            )

            Log.i("DATABASE","REGISTERED DATA!")

        }

        suspend fun generateAttendance(attendanceDao: AttendanceDao){
            val nowDate= Calendar.getInstance()
            if(nowDate.get(Calendar.DAY_OF_WEEK) == 7){
                attendanceDao.insert(Attendance(date = nowDate.time, course = 1))
                attendanceDao.insert(Attendance(date = nowDate.time, course = 2))
                attendanceDao.insert(Attendance(date = nowDate.time, course = 3))
                attendanceDao.insert(Attendance(date = nowDate.time, course = 4))
            }
        }
    }
}