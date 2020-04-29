package com.example.capaproject

import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.example.capaproject.SurveyReaderContract.SurveyEntry
import com.example.capaproject.UserPrefsContract.UserPrefsEntry
import com.example.capaproject.UserHistoryContract.UserHistoryEntry
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

lateinit var mapper : ObjectMapper

private object SurveyReaderContract{
    object SurveyEntry : BaseColumns{
        const val TABLE_NAME = "Survey"
        const val COLUMN_QUESTION = "Question"
        const val COLUMN_ANSWER = "Answer"
    }
}

private object UserHistoryContract{
    object UserHistoryEntry : BaseColumns{
        const val TABLE_NAME = "UserHistory"
        const val COLUMN_DATE_TIME = "DateTime"
        const val COLUMN_STATE = "State"
        const val COLUMN_LATITUDE = "Latitude"
        const val COLUMN_LONGITUDE = "Longitude"
    }
}

private object UserPrefsContract{
    object UserPrefsEntry : BaseColumns{
        const val TABLE_NAME = "UserPrefs"
        const val COLUMN_WIDGET = "Widget"
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
            "${UserPrefsEntry.COLUMN_WIDGET} TEXT)"

private const val USER_HISTORY_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${UserHistoryEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${UserHistoryEntry.COLUMN_DATE_TIME} TEXT," +
            "${UserHistoryEntry.COLUMN_STATE} TEXT," +
            "${UserHistoryEntry.COLUMN_LATITUDE} DOUBLE," +
            "${UserHistoryEntry.COLUMN_LONGITUDE} DOUBLE)"

private const val SURVEY_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SurveyEntry.TABLE_NAME}"
private const val WORK_DELETE_ENTRIES = "DROP TABLE IF EXISTS WorkState"
private const val DEFAULT_DELETE_ENTRIES = "DROP TABLE IF EXISTS DefaultState"
private const val SCHOOL_DELETE_ENTRIES = "DROP TABLE IF EXISTS SchoolState"
private const val USER_PREFS_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${UserPrefsEntry.TABLE_NAME}"
private const val USER_HISTORY_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${UserHistoryEntry.TABLE_NAME}"

class DatabaseHandler(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    init{
        mapper = jacksonObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        mapper.addMixIn(ComponentName::class.java,CNmixin::class.java)
    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SURVEY_CREATE_ENTRIES)
        db.execSQL(USER_PREFS_CREATE_ENTRIES)
        db.execSQL(USER_HISTORY_CREATE_ENTRIES)

        mapper = jacksonObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        mapper.addMixIn(ComponentName::class.java,CNmixin::class.java)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SURVEY_DELETE_ENTRIES)
        db.execSQL(WORK_DELETE_ENTRIES)
        db.execSQL(USER_PREFS_DELETE_ENTRIES)
        db.execSQL(SCHOOL_DELETE_ENTRIES)
        db.execSQL(DEFAULT_DELETE_ENTRIES)
        onCreate(db)
    }
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Database"
    }

    //given 2 widgetHolders, replace all occurrences of Old to New
    fun updatePrefsForAllStates(whOld:widgetHolder, whNew:widgetHolder){
        //iterate through every state
        for(stateName in context.resources.getStringArray(R.array.States)) {
            //First, get HashMap from database.
            val map = getStateData(stateName)
            //placeholder to update, since we can't update hashMap as we iterate through it
            var toUpdate = widgetHolder(whOld.awpi, 0)
            var toUpdateVal = 0.0
            var found = false
            //iterate over every value in map
            for (entry in map) {
                //if class name matches
                if (entry.key.awpi.provider.className == whOld.awpi.provider.className) {
                    toUpdate = entry.key
                    toUpdateVal = entry.value
                    found = true
                }
            }
            if (found) {
                map.remove(toUpdate)
                map[whNew] = toUpdateVal
                //only update if needed
                updateStateData(stateName, map)
            }
        }
    }

    fun updateUserPrefs(prefs: UserPrefApps){
        updateUserPrefsData(prefs)
    }

    //Adds or updates user preferences in database
    private fun updateUserPrefsData(prefs: UserPrefApps){
        val db = this.writableDatabase
        db.execSQL(USER_PREFS_DELETE_ENTRIES)
        db.execSQL(USER_PREFS_CREATE_ENTRIES)

        val jsonString = mapper.writeValueAsString(prefs)

        val values = ContentValues().apply {
            put(UserPrefsEntry.COLUMN_WIDGET, jsonString)
        }
        db.insert(UserPrefsEntry.TABLE_NAME, null, values)

        db.close()
    }

    fun getUserPrefs(): UserPrefApps{
        return getUserPrefsData()
    }

    //Gets user preference info from database and returns as UserPrefApps object
    private fun getUserPrefsData(): UserPrefApps{
        val db = this.readableDatabase
        db.execSQL(USER_PREFS_CREATE_ENTRIES)
        var prefs = UserPrefApps()

        val selectQuery = "SELECT * FROM ${UserPrefsEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            val jsonString = cursor.getString(cursor.getColumnIndex("Widget"))
            prefs = mapper.readValue(jsonString)
            cursor.moveToNext()
        }
        cursor.close()
        db.close()

        return prefs
    }

    //Adds or updates work state info in database
    private fun updateStateData(stateName: String, map: HashMap<widgetHolder, Double>){
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $stateName")
        db.execSQL("CREATE TABLE IF NOT EXISTS $stateName(" +
                "WidgetName TEXT," +
                "WidgetInfo TEXT," +
                "Weight DOUBLE)")

        for(entry in map){
            val mapperString = mapper.writeValueAsString(entry.key)
            val weight = entry.value
            val values = ContentValues().apply{
                put("WidgetInfo", mapperString)
                put("Weight", weight)
            }
            db.insert(stateName, null, values)
        }
        db.close()
    }
    //Updates state info in corresponding table using passed string to check which state
    fun updateDatabaseState(stateName: String, map: HashMap<widgetHolder, Double>){
        updateStateData(stateName, map)
    }

    fun updateDatabaseSurvey(userProfile: UserProfile){
        updateSurveyData(userProfile)
    }

    //Deletes all tables in database
    fun deleteData(){
        val db = this.writableDatabase
        onUpgrade(db, 1, 1)
        db.close()
    }

    //Adds or updates survey info in database
    private fun updateSurveyData(profile: UserProfile){
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
    fun getDatabaseState(stateName: String): HashMap<widgetHolder, Double> {
        return getStateData(stateName)
    }
    //Gets state info from database table based on stateName parameter and returns as HashMap
    private fun getStateData(stateName: String): HashMap<widgetHolder, Double> {
        val db = this.readableDatabase
        db.execSQL("CREATE TABLE IF NOT EXISTS $stateName(" +
                "WidgetName TEXT," +
                "WidgetInfo TEXT," +
                "Weight DOUBLE)")

        val map: HashMap<widgetHolder, Double> = HashMap()

        val selectQuery = "SELECT * FROM $stateName"
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            val jsonString = cursor.getString(cursor.getColumnIndex("WidgetInfo"))
            val weight = cursor.getDouble(cursor.getColumnIndex("Weight"))
            val appWidgetObject : widgetHolder = mapper.readValue(jsonString)
            map[appWidgetObject] = weight
            cursor.moveToNext()
        }
        cursor.close()
        db.close()

        return map
    }

    fun getSurvey(): UserProfile {
        return getSurveyData()
    }

    //Gets survey info from database and returns as HashMap
    private fun getSurveyData(): UserProfile{
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
        db.close()
        return UserProfile(home, work, school, gender, birthday)
    }

    fun clearUserHistory(){
        val db = this.writableDatabase
        db.execSQL(USER_HISTORY_DELETE_ENTRIES)
        db.close()
    }

    fun updateUserHistory(userHistory: UserHistory){
        updateUserHistoryData(userHistory)
    }

    //Function to save user session information into database
    private fun updateUserHistoryData(userHistory: UserHistory){
        val db = this.writableDatabase
        db.execSQL(USER_HISTORY_CREATE_ENTRIES)

        val values = ContentValues().apply {
            put(UserHistoryEntry.COLUMN_DATE_TIME, userHistory.dateTime)
            put(UserHistoryEntry.COLUMN_STATE, userHistory.userState)
            put(UserHistoryEntry.COLUMN_LATITUDE, userHistory.latitude)
            put(UserHistoryEntry.COLUMN_LONGITUDE, userHistory.longitude)
        }
        db.insert(UserHistoryEntry.TABLE_NAME, null, values)

        val selectQuery = "SELECT * FROM ${UserHistoryEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToLast()
        //cursor.moveToFirst()
        while(!cursor.isAfterLast){
            Log.d("test", cursor.getString(cursor.getColumnIndex(UserHistoryEntry.COLUMN_STATE)))
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
    }
}