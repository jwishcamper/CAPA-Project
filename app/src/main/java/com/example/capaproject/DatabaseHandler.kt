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



private const val SURVEY_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${SurveyEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${SurveyEntry.COLUMN_QUESTION} TEXT," +
            "${SurveyEntry.COLUMN_ANSWER} TEXT)"

private const val WORK_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS $WORK_TABLE_NAME (" +
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

    //save state to database given a string statename and a hashmap
    fun addState(stateName: String, map: HashMap<ComponentName, Double>){
        val db = this.writableDatabase
        db.execSQL(WORK_CREATE_ENTRIES)
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
    fun addSurveyInfo(map: HashMap<String, String>){
        val db = this.writableDatabase
        db.execSQL(SURVEY_CREATE_ENTRIES)
        for(entry in map){
            val question = entry.key
            val answer = entry.value

            val values = ContentValues().apply{
                put(SurveyEntry.COLUMN_QUESTION, question)
                put(SurveyEntry.COLUMN_ANSWER, answer)
            }
            db.insert(SurveyEntry.TABLE_NAME, null, values)
        }
        db.close()
    }


    fun deleteInfo(){
        val db = this.writableDatabase
        onUpgrade(db, 1, 1)
    }

    fun updateSurveyInfo(map: HashMap<String, String>){
        val db = this.writableDatabase
        db.execSQL(SURVEY_DELETE_ENTRIES)
        db.execSQL(SURVEY_CREATE_ENTRIES)
        addSurveyInfo(map)

        db.close()
    }

    /*fun getState(stateName: String): HashMap<ComponentName, Double>{
        var map: HashMap<ComponentName, Double> = HashMap()
        if(stateName == "Work") {
            map = getWorkState()
        }
        return map
    }*/
    //load state from database
    fun getState(stateName: String): HashMap<ComponentName, Double>{
        val db = this.writableDatabase
        db.execSQL(WORK_CREATE_ENTRIES)
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

    fun getSurveyInfo(): HashMap<String, String>{
        val db = this.readableDatabase
        db.execSQL(SURVEY_CREATE_ENTRIES)
        val map: HashMap<String, String> = HashMap()
        val selectQuery = "SELECT * FROM ${SurveyEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor!!.moveToFirst()
        while(!cursor.isAfterLast){
            val question = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_QUESTION))
            val answer = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
            map[question] = answer
            cursor.moveToNext()
        }
        cursor.close()
        return map
    }
}