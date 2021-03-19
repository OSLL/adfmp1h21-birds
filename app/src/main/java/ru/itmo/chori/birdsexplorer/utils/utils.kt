package ru.itmo.chori.birdsexplorer.utils

import android.location.Address
import android.location.Geocoder
import androidx.annotation.WorkerThread
import com.google.firebase.firestore.GeoPoint
import java.io.IOException

@WorkerThread
fun humanReadableLocation(geocoder: Geocoder, location: GeoPoint): String? {
    val addresses: List<Address>
    try {
        addresses = geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        )
    } catch (ignored: IOException) {
        return null
    }

    if (addresses != null && addresses.isNotEmpty()) {
        return addresses[0].getAddressLine(0)
    }

    return null
}
