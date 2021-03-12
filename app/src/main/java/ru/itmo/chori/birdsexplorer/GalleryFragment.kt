package ru.itmo.chori.birdsexplorer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_gallery.*
import ru.itmo.chori.birdsexplorer.data.BirdModel

class GalleryFragment : Fragment() {
    private lateinit var firestoreAdapter: FirestoreRecyclerAdapter<BirdModel, BirdsViewHolder>
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        val query = firestore.collection("birds")
        val options = with(FirestoreRecyclerOptions.Builder<BirdModel>()) {
            setQuery(query, BirdModel::class.java)
        }.build()

        firestoreAdapter = object : FirestoreRecyclerAdapter<BirdModel, BirdsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdsViewHolder {
                val view = with(LayoutInflater.from(parent.context)) {
                    inflate(R.layout.gallery_bird_card, parent, false)
                }

                return BirdsViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: BirdsViewHolder,
                position: Int,
                model: BirdModel
            ) {
                holder.birdName.text = model.name
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
