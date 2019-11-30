package com.nullptr.monever.location

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.nullptr.monever.location.LocationReaderContract.LocationEntry.COLUMN_NAME_LAT
import com.nullptr.monever.location.LocationReaderContract.LocationEntry.COLUMN_NAME_LNG
import com.nullptr.monever.location.LocationReaderContract.LocationEntry.TABLE_NAME

class LocationReaderDbHelper(context: Context) :
    SQLiteOpenHelper(context,
        DATABASE_NAME, null,
        DATABASE_VERSION
    ){
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_LOCATIONS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL(SQL_DELETE_LOCATIONS)
        onCreate(db)
    }

    companion object{
        const val DATABASE_NAME = "locations_db"
        const val DATABASE_VERSION = 1
    }
}

object LocationReaderContract{
    object LocationEntry : BaseColumns {
        const val TABLE_NAME = "location"
        const val COLUMN_NAME_LAT = "lat"
        const val COLUMN_NAME_LNG = "lng"
    }
}

const val SQL_CREATE_LOCATIONS =
    "CREATE TABLE $TABLE_NAME (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
            "$COLUMN_NAME_LAT DOUBLE, " +
            "$COLUMN_NAME_LNG DOUBLE)"

const val SQL_DELETE_LOCATIONS = "DROP TABLE IF EXISTS $TABLE_NAME"