package ru.itmo.chori.birdsexplorer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

typealias Callback = ((LatLng) -> Unit)

class PickLocationFragment : DialogFragment() {
    companion object {
        @JvmStatic
        fun newInstance() = PickLocationFragment()
    }

    var onOk: Callback? = null
    var onCancel: Callback? = null

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
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
        mapFragment.getMapAsync(callback)
    }

    private val mapFragment: SupportMapFragment
        get() = childFragmentManager.findFragmentByTag(
            getString(R.string.fragment_tag_map_pick_location)
        ) as SupportMapFragment
}
