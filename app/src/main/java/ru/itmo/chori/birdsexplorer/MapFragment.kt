package ru.itmo.chori.birdsexplorer

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.fragment.app.Fragment
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import ru.itmo.chori.birdsexplorer.data.BirdModel

class MapFragment : Fragment(), OnMapReadyCallback, OnCameraIdleListener,
    GoogleMap.OnInfoWindowClickListener, OnRequestPermissionsResultCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
    }

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
        googleMap.setOnCameraIdleListener(this)
        googleMap.setOnInfoWindowClickListener(this)
        this.googleMap = googleMap

        enableLocation()
        queryMarkersData()
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
                    val location = LatLng(bird.location.latitude, bird.location.longitude)
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

    private fun loadFragmentOnStack(fragment: Fragment) {
        fragmentManager?.let {
            with(it.beginTransaction()) {
                val tag = FragmentTags.MAP.toString()

                replace(R.id.app_content, fragment, tag)
                addToBackStack(tag)

                commit()
            }
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        val bird = marker.tag as BirdModel
        loadFragmentOnStack(BirdFragment.newInstance(bird))
    }
}
