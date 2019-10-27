package com.nullptr.monever

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.sucho.placepicker.AddressData
import com.sucho.placepicker.Constants.ADDRESS_INTENT
import com.sucho.placepicker.Constants.DEFAULT_ZOOM
import com.sucho.placepicker.Constants.PLACE_PICKER_REQUEST
import com.sucho.placepicker.MapType
import com.sucho.placepicker.PlacePicker
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.logging.Level
import java.util.logging.Logger


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val logger = Logger.getLogger("MapsActivity")
    private lateinit var gMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private var userLocations = mutableListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        addLocationButton.setOnClickListener {
            openPlacePicker()
        }
    }

    private fun displayUserLocations() {
        val boundsBuilder = LatLngBounds.builder()
        for(location in userLocations){
            gMap.addMarker(MarkerOptions().position(location))
            boundsBuilder.include(location)
        }
        val bounds = boundsBuilder.build()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50)
        gMap.moveCamera(cameraUpdate)
    }

    private fun prepareLocationButton() {
        // changing default location button (square at the top right corner) to fab-like custom button
        val locationButton =
            (mapFragment.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(
                Integer.parseInt("2")
            ) as ImageView
        val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParams.setMargins(0, 0, 30, 210)

        locationButton.background = getDrawable(R.drawable.round_button)
        locationButton.setImageDrawable(getDrawable(R.drawable.ic_my_location))
        locationButton.setPadding(40)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        gMap.isMyLocationEnabled = true

        prepareLocationButton()

        readUserLocationsFromDb()
        displayUserLocations()
    }

    private fun openPlacePicker() {
        val intent = PlacePicker.IntentBuilder()
            .setLatLong(
                52.2005,
                20.9551
            )
            .showLatLong(true)
            .setMapZoom(12.0f)
            .hideMarkerShadow(true)
            .setMarkerDrawable(R.drawable.ic_map_marker)
            .setMarkerImageImageColor(R.color.colorPrimary)
            .setFabColor(R.color.colorAccent)
            .setPrimaryTextColor(R.color.colorAccent)
            .setSecondaryTextColor(R.color.colorAccentLight)
            .setMapType(MapType.NORMAL)
            .onlyCoordinates(true)
            .build(this)
        startActivityForResult(intent, PLACE_PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val addressData = data?.getParcelableExtra<AddressData>(ADDRESS_INTENT)
                logger.log(Level.INFO, "selected location $addressData")
                addressData?.let { createNewUserLocation(addressData)}
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun createNewUserLocation(addressData: AddressData){
        val newLocation = LatLng(addressData.latitude, addressData.longitude)
        userLocations.add(newLocation)
        saveNewLocationToDb(newLocation)
        gMap.addMarker(MarkerOptions().position(newLocation))

        val update = CameraUpdateFactory.newLatLngZoom(
            newLocation,
            DEFAULT_ZOOM
        )
        gMap.moveCamera(update)
    }

    private fun readUserLocationsFromDb() {
        val dbHelper = LocationReaderDbHelper(applicationContext)
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
    }

    private fun saveNewLocationToDb(location: LatLng) {
        val dbHelper = LocationReaderDbHelper(applicationContext)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(LocationReaderContract.LocationEntry.COLUMN_NAME_LAT, location.latitude)
            put(LocationReaderContract.LocationEntry.COLUMN_NAME_LNG, location.longitude)
        }
        val newRowId = db?.insert(LocationReaderContract.LocationEntry.TABLE_NAME, null, values)
        logger.log(Level.INFO, "saved location to db with id $newRowId")
    }
}
