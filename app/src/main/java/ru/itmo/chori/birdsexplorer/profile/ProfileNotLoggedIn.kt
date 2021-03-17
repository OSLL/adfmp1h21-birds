package ru.itmo.chori.birdsexplorer.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile_not_logged_in.*
import ru.itmo.chori.birdsexplorer.R
import ru.itmo.chori.birdsexplorer.RequestCode


class ProfileNotLoggedIn : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLoggedInUserFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_not_logged_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_log_in.setOnClickListener {
            if (Firebase.auth.currentUser == null) {
                val providers = listOf(
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.GitHubBuilder().build()
                )

                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    RequestCode.SIGN_IN.ordinal
                )

                return@setOnClickListener
            }

            loadLoggedInUserFragment()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode.SIGN_IN.ordinal) {
//            FIXME: Handle error
//            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                loadLoggedInUserFragment()
            }
        }
    }

    private fun loadLoggedInUserFragment() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            loadFragment(ProfileLoggedIn.newInstance(user))
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
        @JvmStatic
        fun newInstance() = ProfileNotLoggedIn()
    }
}
