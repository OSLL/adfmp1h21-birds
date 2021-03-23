package ru.itmo.chori.birdsexplorer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.coroutineScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.ktx.awaitMap
import ru.itmo.chori.birdsexplorer.utils.ParcelableGeoPoint

typealias Callback = ((LatLng) -> Unit)

private const val ARG_LOCATION = "location"

class PickLocationFragment : DialogFragment() {
    private var location: GeoPoint? = null

    companion object {
        private const val DEFAULT_ZOOM = 14.0f

        @JvmStatic
        fun newInstance(location: GeoPoint?) = PickLocationFragment().apply {
            arguments = Bundle().apply {
                location?.let {
                    putParcelable(
                        ARG_LOCATION,
                        ParcelableGeoPoint(location.latitude, location.longitude)
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            location = it.getParcelable<ParcelableGeoPoint>(ARG_LOCATION)?.unwrap()
        }
    }

    // TODO: It's might be better to always show button and request permission on first click
    private val requestGeolocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // FIXME: Doesn't moves camera to user location right after permission given. See https://issuetracker.google.com/issues/73122459
            enableLocation()
        }

        // TODO: Handle reject
    }

    var onOk: Callback? = null
    var onCancel: Callback? = null

    private fun enableLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestGeolocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        googleMap.isMyLocationEnabled = true
    }

    private lateinit var customView: View
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return customView
    }

    @SuppressLint("InflateParams") // see https://developer.android.com/guide/topics/ui/dialogs.html#CustomLayout
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        customView = layoutInflater.inflate(
            R.layout.fragment_pick_location,
            null
        )

        return AlertDialog.Builder(requireContext()).apply {
            setView(customView)
            setPositiveButton(android.R.string.ok) { _, _ ->
                onOk?.invoke(googleMap.cameraPosition.target)
            }
            setNegativeButton(android.R.string.cancel) { _, _ ->
                onCancel?.invoke(googleMap.cameraPosition.target)
            }
        }.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentByTag(
            getString(R.string.fragment_tag_map_pick_location)
        ) as SupportMapFragment
        lifecycle.coroutineScope.launchWhenCreated {
            googleMap = mapFragment.awaitMap()

            location?.let {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        DEFAULT_ZOOM
                    )
                )
            }

            enableLocation()
        }
    }
}
