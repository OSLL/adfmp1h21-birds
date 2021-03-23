package ru.itmo.chori.birdsexplorer.profile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile_not_logged_in.*
import ru.itmo.chori.birdsexplorer.R


class ProfileNotLoggedIn : Fragment() {
    private val requestAuth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // FIXME: Handle error
//        val response = IdpResponse.fromResultIntent(it.data)

        if (it.resultCode == Activity.RESULT_OK) {
            loadLoggedInUserFragment()
        }
    }

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

                requestAuth.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build()
                )

                return@setOnClickListener
            }

            loadLoggedInUserFragment()
        }
    }

    private fun loadLoggedInUserFragment() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            loadFragment(ProfileLoggedIn.newInstance(user))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        with(parentFragmentManager.beginTransaction()) {
            replace(R.id.app_content, fragment)
            commit()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileNotLoggedIn()
    }
}
