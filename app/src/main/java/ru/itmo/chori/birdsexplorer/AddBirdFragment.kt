package ru.itmo.chori.birdsexplorer

import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.firestore.GeoPoint
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
import ru.itmo.chori.birdsexplorer.utils.humanReadableLocation

class AddBirdFragment(bird: BirdModel? = null) : Fragment() {
    private lateinit var geocoder: Geocoder
    private lateinit var oldTitle: CharSequence

    private lateinit var pickImageDialog: PickImageDialog

    private val birdViewModel: BirdViewModel by viewModels { BirdViewModelFactory(bird) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        textAddBirdName.addTextChangedListener(birdNameValidator)

        imageAddBird.setOnClickListener {
            pickImageDialog.show(childFragmentManager)
        }

        buttonSelectLocation.setOnClickListener {
            val dialog = PickLocationFragment.newInstance()

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

            birdCurrentLocationText.text = null
            showLocationFieldError(false)
        }

        buttonSaveBird.setOnClickListener {
            if (!validate()) {
                return@setOnClickListener
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
