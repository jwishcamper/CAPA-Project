package com.example.capaproject

import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.example.capaproject.SurveyReaderContract.SurveyEntry
import com.example.capaproject.WorkReaderContract.WorkEntry

object SurveyReaderContract{
    object SurveyEntry : BaseColumns{
        const val TABLE_NAME = "Survey"
        const val COLUMN_QUESTION = "Question"
        const val COLUMN_ANSWER = "Answer"
    }
}

/*object StateReaderContract{
    object StateEntry : BaseColumns{
        //const val TABLE_NAME = ""
        const val COLUMN_PACKAGE = "Package"
        const val COLUMN_CLASS = "Class"
        const val COLUMN_WEIGHT = "Weight"
    }
}*/

object WorkReaderContract{
    object WorkEntry : BaseColumns{
        const val TABLE_NAME = "Work"
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
    "CREATE TABLE IF NOT EXISTS ${WorkEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${WorkEntry.COLUMN_PACKAGE} TEXT," +
            "${WorkEntry.COLUMN_CLASS} TEXT," +
            "${WorkEntry.COLUMN_WEIGHT} DOUBLE)"

private const val SURVEY_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SurveyEntry.TABLE_NAME}"
private const val WORK_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${WorkEntry.TABLE_NAME}"

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

    private fun updateWorkInfo(map: HashMap<ComponentName, Double>){
        val db = this.writableDatabase
        db.execSQL(WORK_DELETE_ENTRIES)
        db.execSQL(WORK_CREATE_ENTRIES)

        for(entry in map){
            val pkg = entry.key.packageName
            val cls = entry.key.className
            val weight = entry.value
            //val selectQuery = "SELECT * FROM ${WorkEntry.TABLE_NAME}"

            val values = ContentValues().apply{
                put(WorkEntry.COLUMN_PACKAGE, pkg)
                put(WorkEntry.COLUMN_CLASS, cls)
                put(WorkEntry.COLUMN_WEIGHT, weight)
            }
            db.insert(WorkEntry.TABLE_NAME, null, values)

            /*val cursor = db.rawQuery(selectQuery, null)
            var exists = false
            var needsDeletion = false
            cursor.moveToFirst()
            while(!cursor.isAfterLast) {
                //db.replace(WorkEntry.TABLE_NAME, null, values)
                if(cursor.getColumnName(cursor.getColumnIndex(WorkEntry.COLUMN_CLASS)) == cls){
                    //db.update(WorkEntry.TABLE_NAME, values, null, null)
                    //cursor.moveToNext()
                    exists = true
                    needsDeletion = false
                    break
                }else if(cursor.getColumnName(cursor.getColumnIndex(WorkEntry.COLUMN_CLASS)) != cls){
                    needsDeletion = true

                }
                cursor.moveToNext()
            }
            if(exists){
                db.update(WorkEntry.TABLE_NAME, values, "${WorkEntry.COLUMN_CLASS}=$cls", null)
            }else if(needsDeletion){
                db.delete(WorkEntry.TABLE_NAME, null, null)
            }*/
        }
        db.close()
    }

    fun updateState(stateName: String, map: HashMap<ComponentName, Double>){
        when(stateName){
            "atWork" -> updateWorkInfo(map)
        }
    }

    /*fun addState(stateName: String, map: HashMap<ComponentName, Double>){
        if(stateName == "Work"){
            addWorkState(map)
        }
    }*/

    fun deleteInfo(){
        val db = this.writableDatabase
        onUpgrade(db, 1, 1)
    }

    fun updateSurveyInfo(map: HashMap<String, String>){
        val db = this.writableDatabase
        db.execSQL(SURVEY_CREATE_ENTRIES)

        for(entry in map){
            val question = entry.key
            val answer = entry.value

            val values = ContentValues().apply{
                put(SurveyEntry.COLUMN_QUESTION, question)
                put(SurveyEntry.COLUMN_ANSWER, answer)
            }
            db.replace(SurveyEntry.TABLE_NAME, null, values)
        }
        db.close()
    }

    fun getStateInfo(stateName: String): HashMap<ComponentName, Double>? {
        return when (stateName) {
            "atWork" -> getWorkInfo()
            else -> return null
        }
    }

    private fun getWorkInfo(): HashMap<ComponentName, Double> {
        val db = this.writableDatabase
        db.execSQL(WORK_CREATE_ENTRIES)

        val map: HashMap<ComponentName, Double> = HashMap()
        val selectQuery = "SELECT * FROM ${WorkEntry.TABLE_NAME}"
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

