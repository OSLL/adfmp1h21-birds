package ru.itmo.chori.birdsexplorer

import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import kotlinx.android.synthetic.main.fragment_add_bird.*
import kotlinx.android.synthetic.main.fragment_pick_location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.itmo.chori.birdsexplorer.data.BirdModel
import ru.itmo.chori.birdsexplorer.data.BirdViewModel
import ru.itmo.chori.birdsexplorer.data.BirdViewModelFactory
import ru.itmo.chori.birdsexplorer.profile.ProfileNotLoggedIn
import ru.itmo.chori.birdsexplorer.utils.humanReadableLocation
import ru.itmo.chori.birdsexplorer.utils.loadFragment
import java.io.File

class AddBirdFragment(private val bird: BirdModel? = null) : Fragment() {
    private enum class Mode {
        CREATE, EDIT
    }

    private val mode = bird?.let { Mode.EDIT } ?: Mode.CREATE

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: StorageReference
    private lateinit var firestore: FirebaseFirestore

    private lateinit var geocoder: Geocoder
    private lateinit var oldTitle: CharSequence

    private lateinit var pickImageDialog: PickImageDialog

    private lateinit var navigationBar: BottomNavigationView
    private lateinit var progressBar: ProgressBar

    private val birdViewModel: BirdViewModel by viewModels { BirdViewModelFactory(bird) }

    private fun loadProfileFragment() {
        loadFragment(parentFragmentManager, ProfileNotLoggedIn.newInstance())
        navigationBar.selectedItemId = R.id.action_profile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = requireActivity()
        navigationBar = activity.findViewById(R.id.bottom_navigation)

        firebaseAuth = FirebaseAuth.getInstance()
        // Note: token may expire at any time when authentication is required
        if (firebaseAuth.currentUser == null) {
            loadProfileFragment()
            return
        }

        pickImageDialog = PickImageDialog.build(PickSetup())
            .setOnPickResult { result ->
                val path = result.path
                birdViewModel.image.postValue(path)

                Glide.with(requireContext())
                    .load(path)
                    .into(imageAddBird)

                if (textImageAddBirdError.isVisible) {
                    textImageAddBirdError.visibility = View.GONE
                }
            }.setOnPickCancel {
                if (birdViewModel.image.value == null) {
                    textImageAddBirdError.visibility = View.VISIBLE
                }
            }

        geocoder = Geocoder(context)

        birdViewModel.location.observe(this) {
            it?.let { position ->
                birdCurrentLocationText.text = getString(
                    R.string.add_bird_location_not_geo_encoded,
                    position.latitude,
                    position.longitude
                )

                lifecycleScope.launch(context = Dispatchers.IO) {
                    humanReadableLocation(
                        geocoder,
                        GeoPoint(position.latitude, position.longitude)
                    )?.let { text ->
                        withContext(Dispatchers.Main) {
                            birdCurrentLocationText.text = text
                        }
                    }
                }
            }
        }

        progressBar = activity.findViewById(R.id.progressBar)

        storage = FirebaseStorage.getInstance().reference
        firestore = FirebaseFirestore.getInstance()
    }

    private fun showLocationFieldError(show: Boolean) {
        if (show) {
            openMapButtonErrorText.visibility = View.VISIBLE
            openMapButtonHelperText.visibility = View.GONE
        } else {
            openMapButtonErrorText.visibility = View.GONE
            openMapButtonHelperText.visibility = View.VISIBLE
        }
    }

    private fun validate(): Boolean {
        var noError = true

        if (birdViewModel.name.value.isNullOrEmpty()) {
            noError = false
            textLayoutAddBirdName.error = getString(R.string.validation_name_field_is_required)
        }

        if (birdViewModel.image.value.isNullOrEmpty()) {
            noError = false
            textImageAddBirdError.visibility = View.VISIBLE
        }

        if (birdViewModel.location.value == null) {
            noError = false
            showLocationFieldError(true)
        }

        return noError
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_bird, container, false)
        requireActivity().apply {
            oldTitle = title
        }

        return view
    }

    private val birdNameValidator = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (s.isBlank()) {
                textLayoutAddBirdName.error = getString(
                    R.string.validation_name_field_is_required
                )
            } else {
                textLayoutAddBirdName.error = null
            }
        }

        override fun afterTextChanged(s: Editable) {
            birdViewModel.name.postValue(s.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mode == Mode.EDIT) {
            textAddBirdName.setText(birdViewModel.name.value)
            Glide.with(requireContext())
                .load(storage.child(birdViewModel.image.value!!))
                .into(imageAddBird)
        }

        textAddBirdName.addTextChangedListener(birdNameValidator)

        imageAddBird.setOnClickListener {
            pickImageDialog.show(childFragmentManager)
        }

        buttonSelectLocation.setOnClickListener {
            val dialog = PickLocationFragment.newInstance(birdViewModel.location.value)

            dialog.onOk = { position ->
                birdViewModel.location.postValue(GeoPoint(position.latitude, position.longitude))
                showLocationFieldError(false)
            }

            dialog.onCancel = { _ ->
                if (birdViewModel.location.value == null) {
                    showLocationFieldError(true)
                }
            }

            dialog.show(
                childFragmentManager,
                getString(R.string.fragment_tag_map_pick_location_dialog)
            )
        }

        buttonClearAddBird.setOnClickListener {
            birdViewModel.image.postValue(null)
            birdViewModel.location.postValue(null)

            textAddBirdName.error = null
            textAddBirdName.removeTextChangedListener(birdNameValidator)
            textAddBirdName.text = null
            textAddBirdName.addTextChangedListener(birdNameValidator)

            Glide.with(requireContext())
                .load(R.drawable.placeholder_image_add_bird_photo)
                .dontTransform()
                .into(imageAddBird)
            textImageAddBirdError.visibility = View.GONE

            birdCurrentLocationText.text = getString(R.string.add_bird_location_undefined)
            showLocationFieldError(false)

            progressBar.visibility = View.INVISIBLE
        }

        buttonSaveBird.text = getString(
            when (mode) {
                Mode.EDIT -> R.string.label_button_save
                Mode.CREATE -> R.string.label_button_update
            }
        )
        buttonSaveBird.setOnClickListener {
            if (!validate()) {
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            if (mode == Mode.CREATE) {
                createNewBird()
                return@setOnClickListener
            }

            if (birdViewModel.image.value != bird?.image) {
                val file = Uri.fromFile(File(birdViewModel.image.value!!))
                val fileReference = storage.child("birds/${file.lastPathSegment}")

                val user = firebaseAuth.currentUser
                if (user == null) {
                    loadProfileFragment()
                    return@setOnClickListener
                }

                fileReference.putFile(file)
                    .addOnFailureListener {
                        // TODO: Handle failure: e.g. rejection due to unauthenticated
                    }
                    .addOnSuccessListener {
                        updateBird()
                    }

                return@setOnClickListener
            }

            updateBird()
        }
    }

    private fun updateBird() {
        val currentBird = firestore.collection("birds").document(birdViewModel.id!!)
        val data = mutableMapOf<String, Any?>()

        if (bird?.name != birdViewModel.name.value) {
            data["name"] = birdViewModel.name.value
        }

        if (bird?.image != birdViewModel.image.value) {
            val file = Uri.fromFile(File(birdViewModel.image.value!!))
            val fileReference = storage.child("birds/${file.lastPathSegment}")
            data["image"] = fileReference.path
        }

        if (bird?.location != birdViewModel.location.value) {
            data["location"] = birdViewModel.location.value

            val geoLocation = GeoLocation(
                birdViewModel.location.value!!.latitude,
                birdViewModel.location.value!!.longitude
            )
            data["geohash"] = GeoFireUtils.getGeoHashForLocation(geoLocation)
        }

        currentBird.update(data)
            .addOnSuccessListener {
                loadFragment(parentFragmentManager, GalleryFragment.newInstance())
            }.addOnFailureListener {
                // TODO: Handle failure
            }.addOnCompleteListener {
                progressBar.visibility = View.INVISIBLE
            }
    }

    private fun createNewBird() {
        val file = Uri.fromFile(File(birdViewModel.image.value!!))
        val fileReference = storage.child("birds/${file.lastPathSegment}")

        val user = firebaseAuth.currentUser
        if (user == null) {
            loadProfileFragment()
            return
        }

        fileReference.putFile(file)
            .addOnFailureListener {
                // TODO: Handle failure: e.g. rejection due to unauthenticated
            }.addOnSuccessListener {
                val geoLocation = GeoLocation(
                    birdViewModel.location.value!!.latitude,
                    birdViewModel.location.value!!.longitude
                )

                val newBird = BirdModel(
                    id = birdViewModel.id,
                    author = user.uid,
                    name = birdViewModel.name.value,
                    image = fileReference.path,
                    location = birdViewModel.location.value,
                    geohash = GeoFireUtils.getGeoHashForLocation(geoLocation)
                )

                firestore.collection("birds")
                    .add(newBird)
                    .addOnSuccessListener {
                        loadFragment(parentFragmentManager, GalleryFragment.newInstance())
                    }.addOnFailureListener {
                        // TODO: Handle failure
                    }.addOnCompleteListener {
                        progressBar.visibility = View.INVISIBLE
                    }
            }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().apply {
            title = TITLE
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().apply {
            title = oldTitle
        }
    }

    companion object {
        private const val TITLE = "Add bird"

        @JvmStatic
        fun newInstance(bird: BirdModel? = null) = AddBirdFragment(bird)
    }
}
