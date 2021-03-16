package ru.itmo.chori.birdsexplorer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class MapFragment : Fragment(), OnMapReadyCallback, OnRequestPermissionsResultCallback {
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = MapFragment()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != RequestCode.PERMISSION_GEOLOCATION.ordinal) {
            return
        }

        val expected = intArrayOf(PackageManager.PERMISSION_GRANTED)
        if (grantResults.isEmpty() || !grantResults.contentEquals(expected)) {
            // TODO: Handle reject
            return
        }

        // FIXME: Doesn't show your location right after enabling access
        enableLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        enableLocation()
    }

    private fun enableLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                RequestCode.PERMISSION_GEOLOCATION.ordinal
            )

            return
        }

        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.isMyLocationEnabled = true
    }
}
