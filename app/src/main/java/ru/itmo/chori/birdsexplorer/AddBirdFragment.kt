package ru.itmo.chori.birdsexplorer

import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_add_bird.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import ru.itmo.chori.birdsexplorer.utils.humanReadableLocation

class AddBirdFragment : Fragment() {
    private lateinit var geocoder: Geocoder
    private lateinit var oldTitle: CharSequence
    private lateinit var easyImage: EasyImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        easyImage = EasyImage.Builder(requireContext()).build()
        geocoder = Geocoder(context)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageAddBird.setOnClickListener {
            easyImage.openChooser(this)
        }

        buttonSelectLocation.setOnClickListener {
            val dialog = PickLocationFragment.newInstance()

            dialog.onOk = { position ->
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

            dialog.show(requireFragmentManager(), "")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        easyImage.handleActivityResult(
            requestCode,
            resultCode,
            data,
            requireActivity(),
            object : DefaultCallback() {
                override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                    if (imageFiles.isEmpty()) {
                        return
                    }

                    val image = imageFiles[0]
                    Glide.with(requireContext())
                        .load(image.file)
                        .into(imageAddBird)
                }

                override fun onCanceled(source: MediaSource) {
                    super.onCanceled(source)
                    // TODO: Handle cancel
                }

                override fun onImagePickerError(error: Throwable, source: MediaSource) {
                    super.onImagePickerError(error, source)
                    // TODO: Handle error
                }
            })
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
        fun newInstance() = AddBirdFragment()
    }
}