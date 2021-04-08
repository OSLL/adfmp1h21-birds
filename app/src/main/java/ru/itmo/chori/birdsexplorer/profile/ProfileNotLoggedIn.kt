package ru.itmo.chori.birdsexplorer.profile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile_not_logged_in.*
import ru.itmo.chori.birdsexplorer.R
import ru.itmo.chori.birdsexplorer.dialog.ErrorDialogFragment
import ru.itmo.chori.birdsexplorer.utils.loadFragment


class ProfileNotLoggedIn : Fragment() {
    private val requestAuth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val response = IdpResponse.fromResultIntent(it.data)

        if (it.resultCode == Activity.RESULT_OK) {
            loadLoggedInUserFragment()
            return@registerForActivityResult
        }

        val errorMessage = response?.let { resp ->
            resp.error?.message ?: getString(R.string.unknown_error)
        } ?: getString(R.string.login_declined)

        ErrorDialogFragment(
            errorMessage
        ).show(childFragmentManager, getString(R.string.error_not_logged_in))
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
            loadFragment(parentFragmentManager, ProfileLoggedIn.newInstance(user))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileNotLoggedIn()
    }
}
