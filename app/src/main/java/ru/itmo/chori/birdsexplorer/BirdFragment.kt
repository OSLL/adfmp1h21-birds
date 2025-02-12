package ru.itmo.chori.birdsexplorer

import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.ktx.awaitMap
import kotlinx.android.synthetic.main.fragment_bird.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.itmo.chori.birdsexplorer.data.BirdModel
import ru.itmo.chori.birdsexplorer.dialog.ErrorDialogFragment
import ru.itmo.chori.birdsexplorer.utils.humanReadableLocation
import ru.itmo.chori.birdsexplorer.utils.loadFragmentOnStack

internal const val ARG_BIRD = "bird"

class BirdFragment : Fragment() {
    private lateinit var geocoder: Geocoder

    private var progressBar: ProgressBar? = null
    private lateinit var bird: BirdModel
    private lateinit var oldTitle: CharSequence
    private lateinit var storage: StorageReference

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bird = it.getParcelable(ARG_BIRD)!!
        }

        setHasOptionsMenu(true)

        storage = Firebase.storage.reference
        firestore = FirebaseFirestore.getInstance()

        progressBar = activity?.findViewById(R.id.progressBar)

        geocoder = Geocoder(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bird, container, false)
        requireActivity().apply {
            oldTitle = title
        }

        lifecycleScope.launch(context = Dispatchers.IO) {
            humanReadableLocation(
                geocoder,
                GeoPoint(bird.location!!.latitude, bird.location!!.longitude)
            )?.let { text ->
                withContext(Dispatchers.Main) {
                    textBirdSeenLocation.text = text
                }
            }
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        Firebase.auth.currentUser?.let { user ->
            if (user.uid == bird.author) {
                inflater.inflate(R.menu.action_bar_bird_menu, menu)
                return
            }
        }

        if (State.isDemoLogin) {
            inflater.inflate(R.menu.action_bar_bird_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_edit_bird -> {
            loadFragmentOnStack(
                parentFragmentManager,
                AddBirdFragment.newInstance(bird),
                getString(R.string.fragment_tag_gallery)
            )
            true
        }
        R.id.action_remove_bird -> {
            if (State.isDemoLogin) {
                ErrorDialogFragment(getString(R.string.edit_declined)).show(
                    childFragmentManager,
                    getString(R.string.error_edit_bird_demo)
                )
            } else {

                progressBar?.visibility = View.VISIBLE

                firestore.collection("birds").document(bird.id!!).delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            getString(R.string.notification_bird_was_removed, bird.name),
                            Toast.LENGTH_SHORT
                        ).show() // It doesn't require too much user attention
                    }.addOnFailureListener {
                        ErrorDialogFragment(
                            it.message ?: getString(R.string.unknown_error)
                        ).show(childFragmentManager, getString(R.string.error_remove_bird))
                    }.addOnCompleteListener {
                        progressBar?.visibility = View.INVISIBLE
                    }

                childFragmentManager.popBackStack()
            }

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireActivity())
            .load(storage.child(bird.image!!))
            .placeholder(R.drawable.placeholder_image)
            .into(birdPagePhoto)
        birdPagePhoto.contentDescription = getString(
            R.string.bird_photo_description, bird.name
        )

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        lifecycle.coroutineScope.launchWhenCreated {
            val googleMap = mapFragment.awaitMap()
            val location = LatLng(bird.location!!.latitude, bird.location!!.longitude)

            googleMap.addMarker(MarkerOptions().position(location))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM))

            googleMap.setOnMarkerClickListener {
                loadFragmentOnStack(
                    parentFragmentManager,
                    MapFragment.newInstance(GeoPoint(it.position.latitude, it.position.longitude)),
                    getString(R.string.fragment_tag_map_near_bird)
                )

                true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().apply {
            title = bird.name
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().apply {
            title = oldTitle
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
}
