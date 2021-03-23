package ru.itmo.chori.birdsexplorer.utils

import android.location.Address
import android.location.Geocoder
import androidx.annotation.WorkerThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.firestore.GeoPoint
import ru.itmo.chori.birdsexplorer.R
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

fun loadFragment(fragmentManager: FragmentManager, fragment: Fragment) {
    with(fragmentManager.beginTransaction()) {
        replace(R.id.app_content, fragment)
        commit()
    }
}

fun loadFragmentOnStack(fragmentManager: FragmentManager, fragment: Fragment, tag: String) {
    with(fragmentManager.beginTransaction()) {
        replace(R.id.app_content, fragment, tag)
        addToBackStack(tag)

        commit()
    }
}
