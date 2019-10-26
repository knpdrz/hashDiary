package com.nullptr.monever

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.sucho.placepicker.AddressData
import com.sucho.placepicker.Constants.ADDRESS_INTENT
import com.sucho.placepicker.Constants.PLACE_PICKER_REQUEST
import com.sucho.placepicker.MapType
import com.sucho.placepicker.PlacePicker
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

        prepareLocationButton()
    }

    private fun prepareLocationButton() {
        // changing default location button (square at the top right corner) to fab-like custom button
        val locationButton =
            (mapFragment.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(
                Integer.parseInt("2")
            ) as ImageView
        val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 100, 100)
        locationButton.background = getDrawable(R.drawable.round_button)
        locationButton.setImageDrawable(getDrawable(R.drawable.ic_my_location))
        locationButton.setPadding(50)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        gMap.isMyLocationEnabled = true
        gMap.setOnMapClickListener { openPlacePicker() }
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
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
