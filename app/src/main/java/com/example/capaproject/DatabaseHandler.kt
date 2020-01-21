package com.example.capaproject

import android.content.ComponentName
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import androidx.lifecycle.Transformations.map
import com.example.capaproject.SurveyReaderContract.SurveyEntry
import com.example.capaproject.SurveyReaderContract.SurveyEntry.COLUMN_QUESTION
import com.example.capaproject.WorkReaderContract.WorkEntry

/*
val DATABASE_NAME = "Database"
val SURVEY_TABLE_NAME = "Survey"
val WORK_TABLE_NAME = "User State"
val SURVEY_COL_QUESTION = "Question"
val SURVEY_COL_ANSWER = "Answer"
val WORK_COL_WIDGET = "Widget"
val WORK_COL_WEIGHT = "Weight"
*/

object SurveyReaderContract{
    object SurveyEntry : BaseColumns{
        const val TABLE_NAME = "Survey"
        const val COLUMN_QUESTION = "Question"
        const val COLUMN_ANSWER = "Answer"
    }
}
/*
object StateReaderContract{
    object StateEntry : BaseColumns{
        const val TABLE_NAME = ""
        const val COLUMN_WIDGET = "Widget"
        const val COLUMN_WEIGHT = "Weight"
    }
}
*/
object WorkReaderContract{
    object WorkEntry : BaseColumns{
        const val TABLE_NAME = "Work"
        const val COLUMN_PACKAGE = "Package"
        const val COLUMN_CLASS = "Class"
        const val COLUMN_WEIGHT = "Weight"
    }
}

private const val SURVEY_CREATE_ENTRIES =
    "CREATE TABLE ${SurveyEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${SurveyEntry.COLUMN_QUESTION} TEXT," +
            "${SurveyEntry.COLUMN_ANSWER} TEXT)"

private const val WORK_CREATE_ENTRIES =
    "CREATE TABLE ${WorkEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${WorkEntry.COLUMN_PACKAGE} TEXT," +
            "${WorkEntry.COLUMN_CLASS} TEXT," +
            "${WorkEntry.COLUMN_WEIGHT} DOUBLE)"

private const val SURVEY_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SurveyEntry.TABLE_NAME}"

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SURVEY_CREATE_ENTRIES)
        db.execSQL(WORK_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SURVEY_DELETE_ENTRIES)
        onCreate(db)
    }
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Database"
    }

    fun addWorkState(map: HashMap<ComponentName, Double>){
        val db = this.writableDatabase

        for(entry in map){
            val pkg = entry.key.packageName
            val cls = entry.key.className
            val weight = entry.value

            val values = ContentValues().apply{
                put(WorkEntry.COLUMN_PACKAGE, pkg)
                put(WorkEntry.COLUMN_CLASS, cls)
                put(WorkEntry.COLUMN_WEIGHT, weight)
            }
            db.insert(WorkEntry.TABLE_NAME, null, values)
        }
        db.close()
    }

    //val dbHelper = DatabaseHandler(context)
    fun addSurveyInfo(question: String, answer: String){
        val db = this.writableDatabase
        var selectQuery = "SELECT * FROM ${SurveyEntry.TABLE_NAME}"

        val values = ContentValues().apply {
            put(SurveyEntry.COLUMN_QUESTION, question)
            put(SurveyEntry.COLUMN_ANSWER, answer)
        }
        db.insert(SurveyEntry.TABLE_NAME, null, values)
        db.close()
    }

    fun deleteSurveyInfo(){
        val db = this.writableDatabase
        onUpgrade(db, 1, 1)
    }

    fun updateSurveyInfo(question: String, answer: String){
        val db = this.writableDatabase
        val values = ContentValues().apply {
            //put(COLUMN_QUESTION, question)
            put(SurveyEntry.COLUMN_ANSWER, answer)
        }
        db.update(SurveyEntry.TABLE_NAME, values,
            "$COLUMN_QUESTION=?", arrayOf(question))
        db.close()
    }

    fun getSurveyInfo(): Cursor?{
        val db = this.readableDatabase
        //val arrayList = ArrayList<String>()
        //var test = ""

        //val projection = arrayOf(BaseColumns._ID, SurveyReaderContract.SurveyEntry.COLUMN_QUESTION, SurveyReaderContract.SurveyEntry.COLUMN_ANSWER)

        //val selection = "${SurveyReaderContract.SurveyEntry.COLUMN_QUESTION} = ?"
        //val selectionArgs = arrayOf("Address")

        /*val cursor = db.query(
            SurveyReaderContract.SurveyEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )*/
        val selectQuery = "SELECT * FROM ${SurveyEntry.TABLE_NAME}"
        return db.rawQuery(selectQuery, null)
        /*if(cursor.moveToFirst()) {
            do {
                test =
                    cursor.getString(cursor.getColumnIndex(SurveyReaderContract.SurveyEntry.COLUMN_QUESTION))
                arrayList.add(test)
            } while (cursor.moveToNext())
            //test = cursor.getString(1).toString()
            Log.d("test", arrayList.toString())
        }
        cursor.close()*/
    }
}

//val dbHelper = DatabaseHandler
//val db = dbHelper.get
