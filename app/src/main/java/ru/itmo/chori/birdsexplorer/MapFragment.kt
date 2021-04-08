package ru.itmo.chori.birdsexplorer

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.toObject
import com.google.maps.android.ktx.awaitMap
import ru.itmo.chori.birdsexplorer.data.BirdModel
import ru.itmo.chori.birdsexplorer.utils.ParcelableGeoPoint
import ru.itmo.chori.birdsexplorer.utils.loadFragmentOnStack

private const val ARG_LOCATION = "location"

class MapFragment : Fragment(), OnCameraIdleListener, GoogleMap.OnInfoWindowClickListener {
    private var location: GeoPoint? = null

    private lateinit var googleMap: GoogleMap
    private lateinit var firestore: FirebaseFirestore

    // TODO: It's might be better to always show button and request permission on first click
    private val requestGeolocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // FIXME: Doesn't moves camera to user location right after permission given. See https://issuetracker.google.com/issues/73122459
            enableLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            location = it.getParcelable<ParcelableGeoPoint>(ARG_LOCATION)?.unwrap()
        }

        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
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

            googleMap.setOnCameraIdleListener(this@MapFragment)
            googleMap.setOnInfoWindowClickListener(this@MapFragment)

            enableLocation()
            queryMarkersData()
        }

        return view
    }

    companion object {
        const val DEFAULT_ZOOM = 14.0f

        @JvmStatic
        fun newInstance(location: GeoPoint? = null) = MapFragment().apply {
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

    /**
     * Computes radius of a visible map
     * @author https://stackoverflow.com/a/44282119/12411158
     */
    private fun mapVisibleRadius(): Float {
        val visibleRegion = googleMap.projection.visibleRegion
        val farLeft = visibleRegion.farLeft
        val nearRight = visibleRegion.nearRight

        val diagonalDistance = FloatArray(1)
        Location.distanceBetween(
            farLeft.latitude,
            farLeft.longitude,
            nearRight.latitude,
            nearRight.longitude,
            diagonalDistance
        )

        return diagonalDistance[0] / 2
    }

    private fun queryMarkersData() {
        val cameraPosition = googleMap.cameraPosition.target
        val center = GeoLocation(cameraPosition.latitude, cameraPosition.longitude)

        queryMarkersData(center, mapVisibleRadius())
    }

    private fun queryMarkersData(center: GeoLocation, radius: Float) {
        val tasks = GeoFireUtils.getGeoHashQueryBounds(center, radius.toDouble()).map { bound ->
            firestore.collection("birds")
                .orderBy("geohash")
                .startAt(bound.startHash)
                .endAt(bound.endHash)
                .get()
        }

        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                tasks.flatMap { task ->
                    task.result?.let { snapshot ->
                        snapshot.documents.filter { document ->
                            val loc = document.getGeoPoint("location") ?: return@filter false
                            val geoLocation = GeoLocation(loc.latitude, loc.longitude)

                            GeoFireUtils.getDistanceBetween(geoLocation, center) <= radius
                        }
                    } ?: emptyList()
                }.mapNotNull {
                    it.toObject<BirdModel>()
                }.map { bird ->
                    val location = LatLng(bird.location!!.latitude, bird.location.longitude)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(bird.name)
                    )?.apply {
                        tag = bird
                    }
                }
            }
    }

    override fun onCameraIdle() {
        queryMarkersData()
    }

    override fun onInfoWindowClick(marker: Marker) {
        val bird = marker.tag as BirdModel
        loadFragmentOnStack(
            parentFragmentManager,
            BirdFragment.newInstance(bird),
            getString(R.string.fragment_tag_map)
        )
    }
}
