package ru.itmo.chori.birdsexplorer

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.itmo.chori.birdsexplorer.data.BirdModel
import ru.itmo.chori.birdsexplorer.utils.humanReadableLocation
import ru.itmo.chori.birdsexplorer.utils.loadFragmentOnStack
import java.text.DateFormat

class GalleryFragment : Fragment() {
    private lateinit var firestoreAdapter: FirestoreRecyclerAdapter<BirdModel, BirdsViewHolder>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var geocoder: Geocoder
    private lateinit var storage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        geocoder = Geocoder(context)
        storage = Firebase.storage.reference

        val query = firestore.collection("birds")
        val options = with(FirestoreRecyclerOptions.Builder<BirdModel>()) {
            setQuery(query, BirdModel::class.java)
        }.build()

        // TODO: For paging – https://github.com/firebase/FirebaseUI-Android/tree/master/firestore#using-the-firestorepagingadapter
        firestoreAdapter = object : FirestoreRecyclerAdapter<BirdModel, BirdsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdsViewHolder {
                val view = with(LayoutInflater.from(parent.context)) {
                    inflate(R.layout.gallery_bird_card, parent, false)
                }

                return BirdsViewHolder(view)
            }

            private val dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT)
            override fun onBindViewHolder(
                holder: BirdsViewHolder,
                position: Int,
                model: BirdModel
            ) {
                holder.birdName.text = model.name
                holder.birdSeenTime.text = dateFormatter.format(model.seen_at!!.toDate())

                holder.birdLocation.text = getString(
                    R.string.gallery_card_bird_location,
                    model.location!!.latitude,
                    model.location.longitude
                )
                lifecycleScope.launch(context = Dispatchers.IO) {
                    humanReadableLocation(geocoder, model.location)?.let { text ->
                        withContext(Dispatchers.Main) {
                            holder.birdLocation.text = text
                        }
                    }
                }

                Glide.with(requireActivity())
                    .load(storage.child(model.image!!))
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.birdImage)
                holder.birdImage.contentDescription = getString(
                    R.string.bird_photo_description, model.name
                )

                holder.itemView.setOnClickListener {
                    loadFragmentOnStack(
                        parentFragmentManager,
                        BirdFragment.newInstance(model),
                        getString(R.string.fragment_tag_gallery)
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(birdsList) {
            setHasFixedSize(true)

            layoutManager = LinearLayoutManager(context)
            adapter = firestoreAdapter
        }
    }

    override fun onStop() {
        super.onStop()
        firestoreAdapter.stopListening()
    }

    override fun onStart() {
        super.onStart()
        firestoreAdapter.startListening()
    }

    companion object {
        @JvmStatic
        fun newInstance() = GalleryFragment()
    }
}

private class BirdsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val birdName: TextView = itemView.findViewById(R.id.birdName)
    val birdSeenTime: TextView = itemView.findViewById(R.id.birdSeenTime)
    val birdLocation: TextView = itemView.findViewById(R.id.birdLocation)
    val birdImage: ImageView = itemView.findViewById(R.id.birdPhoto)
}
