package ru.itmo.chori.birdsexplorer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_bird.*
import ru.itmo.chori.birdsexplorer.data.BirdModel

private const val ARG_BIRD = "bird"

class BirdFragment : Fragment(), OnMapReadyCallback {
    private lateinit var bird: BirdModel
    private lateinit var oldTitle: CharSequence
    private lateinit var storage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bird = it.getParcelable(ARG_BIRD)!!
        }

        storage = Firebase.storage.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bird, container, false)
        activity?.let {
            oldTitle = it.title
        }

        // TODO: If user id == author id => show edit and delete icons

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireActivity())
            .load(storage.child(bird.image))
            .placeholder(R.drawable.placeholder_image)
            .into(birdPagePhoto)
        birdPagePhoto.contentDescription = getString(
            R.string.bird_photo_description, bird.name
        )

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            it.title = bird.name
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.let {
            it.title = oldTitle
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param bird Bird to show it's detailed info.
         * @return A new instance of fragment BirdFragment.
         */
        @JvmStatic
        fun newInstance(bird: BirdModel) =
            BirdFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_BIRD, bird)
                }
            }

        private const val DEFAULT_ZOOM = 14.0f
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(bird.location.latitude, bird.location.longitude)

        googleMap.addMarker(MarkerOptions().position(location))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM))
    }
}