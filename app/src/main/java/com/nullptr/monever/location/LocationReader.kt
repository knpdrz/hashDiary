package com.nullptr.monever.location

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.util.logging.Level.INFO
import java.util.logging.Logger
import kotlin.properties.Delegates

interface ValueChangedListener {
    fun onValueChanged(newValue: List<LatLng>)
}

class LocationReader(listener: ValueChangedListener, userId: String) {
    private val logger = Logger.getLogger("LocationReader")
    private val database = FirebaseDatabase.getInstance()
    private val locationsRef = database.getReference("locations").child(userId)

    private var locationsList: List<LatLng> by Delegates.observable(
        initialValue = listOf(),
        onChange = { _, _, new ->
            listener.onValueChanged(new)
        })

    init {
        prepareLocationsListener()
    }

    fun saveNewLocationToDb(location: LatLng) {
        val newLocationKey = locationsRef.push().key
        if (newLocationKey != null) {
            locationsRef.child(newLocationKey).setValue(location)
        } else {
            logger.log(INFO, "unable to save location $location to fajabejz")
        }
    }

    private fun prepareLocationsListener() {
        val locationsListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val locationRaw = dataSnapshot.getValue(SimpleLocation::class.java)
                if (locationRaw != null) {
                    locationsList =
                        locationsList + LatLng(locationRaw.latitude, locationRaw.longitude)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val locationRaw = dataSnapshot.getValue(SimpleLocation::class.java)
                if (locationRaw != null) {
                    locationsList =
                        locationsList.filterNot { it.latitude == locationRaw.latitude && it.longitude == locationRaw.longitude }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(databaseError: DatabaseError) {
                logger.log(
                    INFO,
                    "fajabejz: failed to load locations {}",
                    databaseError.toException()
                )

            }
        }
        locationsRef.addChildEventListener(locationsListener)
    }
}

data class SimpleLocation(var latitude: Double = 0.0, var longitude: Double = 0.0)