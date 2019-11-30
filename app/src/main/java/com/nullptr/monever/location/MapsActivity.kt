package com.nullptr.monever.location

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.nullptr.monever.R
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

    private fun displayUserLocations(userLocations: List<LatLng>) {
        if(userLocations.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            for (location in userLocations) {
                addMarkerToMap(location)
                boundsBuilder.include(location)
            }
            val bounds = boundsBuilder.build()
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 200)
            gMap.moveCamera(cameraUpdate)
        }
    }

    private fun prepareLocationButton() {
        // changing default location button (square) to fab-like custom button
        val locationButton =
            (mapFragment.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(
                Integer.parseInt("2")
            ) as ImageView

        locationButton.background = getDrawable(R.drawable.round_button)
        locationButton.setImageDrawable(getDrawable(R.drawable.ic_my_location))
        locationButton.setPadding(40)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        gMap.isMyLocationEnabled = true

        prepareLocationButton()

        var userLocations = LocationReader(this).readUserLocationsFromDb()
        displayUserLocations(userLocations)
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
            .setMarkerDrawable(R.drawable.ic_add_location_24dp)
            .setMarkerImageImageColor(R.color.colorPrimary)
            .setFabColor(R.color.colorAccent)
            .setPrimaryTextColor(R.color.colorAccent)
            .setSecondaryTextColor(R.color.colorAccentLight)
            .setMapType(MapType.NORMAL)
            .onlyCoordinates(true)
            .hideMarkerShadow(false)
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
        val location = LatLng(addressData.latitude, addressData.longitude)
        LocationReader(this).saveNewLocationToDb(location)
        addMarkerToMap(location)
        val update = CameraUpdateFactory.newLatLngZoom(
            location,
            DEFAULT_ZOOM
        )
        gMap.moveCamera(update)
        UserLocationsService(this).prepareGeofences(listOf(location))
    }

    private fun addMarkerToMap(location: LatLng){
        gMap.addMarker(MarkerOptions().position(location).icon(bitmapDescriptorFromVector(this,
            R.drawable.ic_place_pink_24dp
        )))
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}
