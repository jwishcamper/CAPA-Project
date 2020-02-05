package com.example.capaproject

import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.example.capaproject.SurveyReaderContract.SurveyEntry
import com.example.capaproject.WorkReaderContract.WorkEntry
import com.example.capaproject.UserPrefsContract.UserPrefsEntry

//import com.example.capaproject.StateReaderContract.StateEntry


const val WORK_TABLE_NAME = "atWork"

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

object UserPrefsContract{
    object UserPrefsEntry : BaseColumns{
        const val TABLE_NAME = "UserPrefs"
        const val COLUMN_WIDGET = "Widget"
        const val COLUMN_PACKAGE = "Package"
        const val COLUMN_CLASS = "Class"
    }
}

private const val SURVEY_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${SurveyEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${SurveyEntry.COLUMN_QUESTION} TEXT," +
            "${SurveyEntry.COLUMN_ANSWER} TEXT)"

private const val USER_PREFS_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${UserPrefsEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${UserPrefsEntry.COLUMN_WIDGET} TEXT," +
            "${UserPrefsEntry.COLUMN_PACKAGE} TEXT," +
            "${UserPrefsEntry.COLUMN_CLASS} TEXT)"

private const val WORK_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${WorkEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${WorkEntry.COLUMN_PACKAGE} TEXT," +
            "${WorkEntry.COLUMN_CLASS} TEXT," +
            "${WorkEntry.COLUMN_WEIGHT} DOUBLE)"

private const val SURVEY_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SurveyEntry.TABLE_NAME}"
private const val WORK_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${WorkEntry.TABLE_NAME}"
private const val USER_PREFS_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${UserPrefsEntry.TABLE_NAME}"

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SURVEY_CREATE_ENTRIES)
        db.execSQL(WORK_CREATE_ENTRIES)
        db.execSQL(USER_PREFS_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SURVEY_DELETE_ENTRIES)
        db.execSQL(WORK_DELETE_ENTRIES)
        db.execSQL(USER_PREFS_DELETE_ENTRIES)
        onCreate(db)
    }
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Database"
    }

    fun updateUserPrefs(prefs: UserPrefApps){
        updateUserPrefsInfo(prefs)
    }

    //Adds or updates user preferences in database
    private fun updateUserPrefsInfo(prefs: UserPrefApps){
        val db = this.writableDatabase
        db.execSQL(USER_PREFS_CREATE_ENTRIES)

        val clock = prefs.getAttr("Clock")
        val music = prefs.getAttr("Music")

        val clockPKG = clock.packageName
        val clockCLS = clock.className

        val musicPKG = music.packageName
        val musicCLS = music.className

        var values = ContentValues().apply {
            put(UserPrefsEntry.COLUMN_WIDGET, "Clock")
            put(UserPrefsEntry.COLUMN_PACKAGE, clockPKG)
            put(UserPrefsEntry.COLUMN_CLASS, clockCLS)
        }
        db.replace(UserPrefsEntry.TABLE_NAME, null, values)

        values.clear()

        values.put(UserPrefsEntry.COLUMN_WIDGET, "Music")
        values.put(UserPrefsEntry.COLUMN_PACKAGE, musicPKG)
        values.put(UserPrefsEntry.COLUMN_CLASS, musicCLS)

        db.replace(UserPrefsEntry.TABLE_NAME, null, values)

        db.close()
    }

    fun getUserPrefs(): UserPrefApps{
        return getUserPrefsInfo()
    }

    //Gets user preference info from database and returns as UserPrefApps object
    private fun getUserPrefsInfo(): UserPrefApps{
        val db = this.readableDatabase
        db.execSQL(USER_PREFS_CREATE_ENTRIES)
        val prefs = UserPrefApps()

        val selectQuery = "SELECT * FROM ${UserPrefsEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            var pkg = cursor.getString(cursor.getColumnIndex("Package"))
            val cls = cursor.getString(cursor.getColumnIndex("Class"))
            val compName = ComponentName(
                pkg,
                cls
            )
            when {
                cursor.getString(cursor.getColumnIndex("Widget")) == "Clock" -> prefs.clock = compName
                cursor.getString(cursor.getColumnIndex("Widget")) == "Music" -> prefs.music = compName
                /*else -> {
                    prefs.clock = ComponentName("", "")
                    prefs.music = ComponentName("", "")
                }*/
            }

            cursor.moveToNext()
        }
        cursor.close()

        return prefs
    }

    //Adds or updates work state info in database
    private fun updateWorkInfo(map: HashMap<ComponentName, Double>){
        val db = this.writableDatabase
        db.execSQL(WORK_DELETE_ENTRIES)
        db.execSQL(WORK_CREATE_ENTRIES)

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

    //Updates state info in corresponding table using passed string to check which state
    fun updateDatabaseState(stateName: String, map: HashMap<ComponentName, Double>){
        when(stateName){
            WORK_TABLE_NAME -> updateWorkInfo(map)
        }
    }

    fun updateDatabaseSurvey(userProfile: UserProfile){
        updateSurveyInfo(userProfile)
    }

    //Deletes all tables in database
    fun deleteInfo(){
        val db = this.writableDatabase
        onUpgrade(db, 1, 1)
    }

    //Adds or updates survey info in database
    private fun updateSurveyInfo(profile: UserProfile){
        val db = this.writableDatabase
        db.execSQL(SURVEY_CREATE_ENTRIES)

        for(entry in profile.getFieldNames()){
            val answer = profile.getField(entry)
            val values = ContentValues().apply{
                put(SurveyEntry.COLUMN_QUESTION, entry)
                put(SurveyEntry.COLUMN_ANSWER, answer)
            }
            db.replace(SurveyEntry.TABLE_NAME, null, values)
        }
        db.close()
    }

    //Uses passed string to get info from corresponding state table
    fun getStateInfo(stateName: String): HashMap<ComponentName, Double>? {
        return when (stateName) {
            "atWork" -> getWorkInfo()
            else -> return null
        }
    }

    //Gets work state info from database and returns as HashMap
    private fun getWorkInfo(): HashMap<ComponentName, Double> {
        val db = this.readableDatabase
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

    fun getSurvey(): UserProfile {
        return getSurveyInfo()
    }

    //Gets survey info from database and returns as HashMap
    private fun getSurveyInfo(): UserProfile{
        val db = this.readableDatabase
        db.execSQL(SURVEY_CREATE_ENTRIES)

        var home = ""
        var work = ""
        var school = ""
        var gender = ""
        var birthday = ""

        val selectQuery = "SELECT * FROM ${SurveyEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            when(cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_QUESTION))){
                "Home" -> home = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
                "Work" -> work = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
                "School" -> school = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
                "Gender" -> gender = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
                "BirthDay" -> birthday = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
            }
            cursor.moveToNext()
        }
        cursor.close()
        return UserProfile(home, work, school, gender, birthday)
    }
}
