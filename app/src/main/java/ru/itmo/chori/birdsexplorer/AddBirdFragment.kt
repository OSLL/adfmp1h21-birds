package ru.itmo.chori.birdsexplorer

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.firestore.GeoPoint
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import kotlinx.android.synthetic.main.fragment_add_bird.*
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
                Glide.with(requireContext())
                    .load(result.uri)
                    .into(imageAddBird)
            }.setOnPickCancel {
                // TODO: Handle refuse
            }

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
            pickImageDialog.show(childFragmentManager)
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

            dialog.show(
                childFragmentManager,
                getString(R.string.fragment_tag_map_pick_location_dialog)
            )
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
