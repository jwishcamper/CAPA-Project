package com.example.capaproject

import android.content.ComponentName
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import androidx.lifecycle.Transformations.map
import com.example.capaproject.SurveyReaderContract.SurveyEntry
import com.example.capaproject.SurveyReaderContract.SurveyEntry.COLUMN_QUESTION
//import com.example.capaproject.WorkReaderContract.WorkEntry
import com.example.capaproject.StateReaderContract.StateEntry

/*
val DATABASE_NAME = "Database"
val SURVEY_TABLE_NAME = "Survey"
val WORK_TABLE_NAME = "User State"
val SURVEY_COL_QUESTION = "Question"
val SURVEY_COL_ANSWER = "Answer"
val WORK_COL_WIDGET = "Widget"
val WORK_COL_WEIGHT = "Weight"
*/
const val WORK_TABLE_NAME = "atWork"

object SurveyReaderContract{
    object SurveyEntry : BaseColumns{
        const val TABLE_NAME = "Survey"
        const val COLUMN_QUESTION = "Question"
        const val COLUMN_ANSWER = "Answer"
    }
}

object StateReaderContract{
    object StateEntry : BaseColumns{
        //const val TABLE_NAME = ""
        const val COLUMN_PACKAGE = "Package"
        const val COLUMN_CLASS = "Class"
        const val COLUMN_WEIGHT = "Weight"
    }
}

/*object WorkReaderContract{
    object WorkEntry : BaseColumns{
        const val TABLE_NAME = "Work"
        //const val COLUMN_PACKAGE = "Package"
        //const val COLUMN_CLASS = "Class"
        //const val COLUMN_WEIGHT = "Weight"
    }
}*/

private const val SURVEY_CREATE_ENTRIES =
    "CREATE TABLE ${SurveyEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${SurveyEntry.COLUMN_QUESTION} TEXT," +
            "${SurveyEntry.COLUMN_ANSWER} TEXT)"

private const val WORK_CREATE_ENTRIES =
    "CREATE TABLE $WORK_TABLE_NAME (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${StateEntry.COLUMN_PACKAGE} TEXT," +
            "${StateEntry.COLUMN_CLASS} TEXT," +
            "${StateEntry.COLUMN_WEIGHT} DOUBLE)"

private const val SURVEY_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SurveyEntry.TABLE_NAME}"
private const val WORK_DELETE_ENTRIES = "DROP TABLE IF EXISTS $WORK_TABLE_NAME"

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SURVEY_CREATE_ENTRIES)
        db.execSQL(WORK_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SURVEY_DELETE_ENTRIES)
        db.execSQL(WORK_DELETE_ENTRIES)
        onCreate(db)
    }
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Database"
    }

    fun addState(stateName: String, map: HashMap<ComponentName, Double>){
        val db = this.writableDatabase

        for(entry in map){
            val pkg = entry.key.packageName
            val cls = entry.key.className
            val weight = entry.value

            val values = ContentValues().apply{
                put(StateEntry.COLUMN_PACKAGE, pkg)
                put(StateEntry.COLUMN_CLASS, cls)
                put(StateEntry.COLUMN_WEIGHT, weight)
            }
            db.insert(stateName, null, values)
        }
        db.close()
    }

    /*fun addState(stateName: String, map: HashMap<ComponentName, Double>){
        if(stateName == "Work"){
            addWorkState(map)
        }
    }*/

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

    fun deleteInfo(){
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

    /*fun getState(stateName: String): HashMap<ComponentName, Double>{
        var map: HashMap<ComponentName, Double> = HashMap()
        if(stateName == "Work") {
            map = getWorkState()
        }
        return map
    }*/

    fun getState(stateName: String): HashMap<ComponentName, Double>{
        val db = this.writableDatabase
        val map: HashMap<ComponentName, Double> = HashMap()
        val selectQuery = "SELECT * FROM $stateName"
        val cursor = db.rawQuery(selectQuery, null)
        cursor!!.moveToFirst()
        while(!cursor.isAfterLast){
            val pkg = cursor.getString(cursor.getColumnIndex("Package"))
            val cls = cursor.getString(cursor.getColumnIndex("Class"))
            val weight = cursor.getDouble(cursor.getColumnIndex("Weight"))
            val compName = ComponentName(
                pkg,
                cls
            )
            map[compName] = weight
            cursor.moveToNext()
        }
        cursor.close()
        return map
    }

    fun getSurveyInfo(): Cursor?{
        val db = this.readableDatabase
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
