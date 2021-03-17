package ru.itmo.chori.birdsexplorer.profile

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_profile_logged_in.*
import ru.itmo.chori.birdsexplorer.BirdFragment
import ru.itmo.chori.birdsexplorer.FragmentTags
import ru.itmo.chori.birdsexplorer.R
import ru.itmo.chori.birdsexplorer.data.BirdModel

private const val ARG_USER = "user"

class ProfileLoggedIn : Fragment() {
    private lateinit var user: FirebaseUser
    private lateinit var firestoreAdapter: FirestoreRecyclerAdapter<BirdModel, BirdsViewHolder>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable(ARG_USER)!!
        }

        setHasOptionsMenu(true)

        firestore = FirebaseFirestore.getInstance()
        storage = Firebase.storage.reference

        val query = firestore.collection("birds")
            .whereEqualTo("author", user.uid)
        val options = with(FirestoreRecyclerOptions.Builder<BirdModel>()) {
            setQuery(query, BirdModel::class.java)
        }.build()

        firestoreAdapter = object : FirestoreRecyclerAdapter<BirdModel, BirdsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdsViewHolder {
                val view = with(LayoutInflater.from(parent.context)) {
                    inflate(R.layout.profile_bird_card, parent, false)
                }

                return BirdsViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: BirdsViewHolder,
                position: Int,
                model: BirdModel
            ) {
                holder.birdName.text = model.name
                Glide.with(requireActivity())
                    .load(storage.child(model.image))
                    .into(holder.birdImage)

                holder.itemView.setOnClickListener {
                    loadFragmentOnStack(BirdFragment.newInstance(model))
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()

                hideListIfNoData(itemCount)
            }
        }
    }

    private fun loadFragmentOnStack(fragment: Fragment) {
        fragmentManager?.let {
            with(it.beginTransaction()) {
                val tag = FragmentTags.GALLERY.toString()

                replace(R.id.app_content, fragment, tag)
                addToBackStack(tag)

                commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.action_bar_profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.action_logout -> {
            AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                loadFragment(ProfileNotLoggedIn.newInstance())
            }

            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun hideListIfNoData(dataCount: Int) {
        if (dataCount == 0) {
            groupNoBirds.visibility = View.VISIBLE
            groupBirdsList.visibility = View.GONE
        } else {
            groupNoBirds.visibility = View.GONE
            groupBirdsList.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_logged_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val escapedUsername = TextUtils.htmlEncode(user.displayName)
        val profileWelcomeText = getString(R.string.profile_logged_welcome, escapedUsername)
        profile_welcome.text = HtmlCompat.fromHtml(
            profileWelcomeText,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        userBirdList.apply {
            setHasFixedSize(true)

            layoutManager = GridLayoutManager(context, 2)
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

    private fun loadFragment(fragment: Fragment) {
        fragmentManager?.let {
            with(it.beginTransaction()) {
                replace(R.id.app_content, fragment)
                commit()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param user Authenticated user.
         * @return A new instance of fragment ProfileLoggedIn.
         */
        @JvmStatic
        fun newInstance(user: FirebaseUser) =
            ProfileLoggedIn().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_USER, user)
                }
            }
    }
}

private class BirdsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val birdName: TextView = itemView.findViewById(R.id.birdNameInProfile)
    val birdImage: ImageView = itemView.findViewById(R.id.birdPhotoInProfile)
}
