package com.nullptr.monever

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.nullptr.monever.LogReaderContract.LogEntry.COLUMN_NAME_CREATION_DATE
import com.nullptr.monever.LogReaderContract.LogEntry.COLUMN_NAME_HAPPY_RATING
import com.nullptr.monever.LogReaderContract.LogEntry.COLUMN_NAME_TEXT
import com.nullptr.monever.LogReaderContract.LogEntry.TABLE_NAME

class LogReaderDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_LOGS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL(SQL_DELETE_LOGS)
        onCreate(db)
    }

    companion object{
        const val DATABASE_NAME = "logs_db"
        const val DATABASE_VERSION = 1
    }
}

object LogReaderContract{
    object LogEntry : BaseColumns {
        const val TABLE_NAME = "log"
        const val COLUMN_NAME_CREATION_DATE = "creation_date"
        const val COLUMN_NAME_HAPPY_RATING = "happy_rating"
        const val COLUMN_NAME_TEXT = "text"
    }
}

const val SQL_CREATE_LOGS =
    "CREATE TABLE $TABLE_NAME (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
            "$COLUMN_NAME_CREATION_DATE INTEGER, " +
            "$COLUMN_NAME_HAPPY_RATING INTEGER, " +
            "$COLUMN_NAME_TEXT TEXT) "

const val SQL_DELETE_LOGS = "DROP TABLE IF EXISTS $TABLE_NAME"