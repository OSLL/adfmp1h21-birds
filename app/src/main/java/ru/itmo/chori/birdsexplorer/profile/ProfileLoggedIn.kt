package ru.itmo.chori.birdsexplorer.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_profile_logged_in.*
import ru.itmo.chori.birdsexplorer.R

private const val ARG_USER = "user"

class ProfileLoggedIn : Fragment() {
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable(ARG_USER)
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

        profile_welcome.text = resources.getString(R.string.profile_logged_welcome, user?.displayName)
        profile_logout.setOnClickListener {
            context?.let {
                AuthUI.getInstance().signOut(it).addOnCompleteListener {
                    loadFragment(ProfileNotLoggedIn.newInstance())
                }
            }
        }
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