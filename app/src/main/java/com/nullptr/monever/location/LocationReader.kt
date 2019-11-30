package com.nullptr.monever.location

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import com.google.android.gms.maps.model.LatLng
import java.util.logging.Level
import java.util.logging.Logger

class LocationReader(private val context: Context) {
    private val logger = Logger.getLogger("LocationReader")

    fun readUserLocationsFromDb(): List<LatLng> {
        val userLocations = mutableListOf<LatLng>()
        val dbHelper = LocationReaderDbHelper(context)
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            LocationReaderContract.LocationEntry.COLUMN_NAME_LAT,
            LocationReaderContract.LocationEntry.COLUMN_NAME_LNG
        )
        val cursor =
            db.query(
                LocationReaderContract.LocationEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
            )
        with(cursor) {
            while (moveToNext()) {
                val lat =
                    getDouble(getColumnIndexOrThrow(LocationReaderContract.LocationEntry.COLUMN_NAME_LAT))
                val lng =
                    getDouble(getColumnIndexOrThrow(LocationReaderContract.LocationEntry.COLUMN_NAME_LNG))

                userLocations.add(LatLng(lat, lng))
            }
        }
        return userLocations
    }

    fun saveNewLocationToDb(location: LatLng) {
        val dbHelper = LocationReaderDbHelper(context)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(LocationReaderContract.LocationEntry.COLUMN_NAME_LAT, location.latitude)
            put(LocationReaderContract.LocationEntry.COLUMN_NAME_LNG, location.longitude)
        }
        val newRowId = db?.insert(LocationReaderContract.LocationEntry.TABLE_NAME, null, values)
        logger.log(Level.INFO, "saved location to db with id $newRowId")
    }
}