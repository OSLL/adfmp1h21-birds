package ru.itmo.chori.birdsexplorer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import kotlinx.android.synthetic.main.activity_main.*
import ru.itmo.chori.birdsexplorer.profile.ProfileNotLoggedIn
import ru.itmo.chori.birdsexplorer.utils.loadFragment

object State {
    var isDemoLogin = false
}

class MainActivity : AppCompatActivity() {
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        analytics = FirebaseAnalytics.getInstance(this)

        with(bottom_navigation) {
            setOnNavigationItemSelectedListener {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStackImmediate(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                }

                when (it.itemId) {
                    R.id.action_gallery -> {
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                            param(
                                FirebaseAnalytics.Param.SCREEN_CLASS,
                                GalleryFragment::class.qualifiedName ?: "Gallery"
                            )
                            param(FirebaseAnalytics.Param.SCREEN_NAME, "gallery")
                        }

                        loadFragment(supportFragmentManager, GalleryFragment.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.action_add_bird -> {
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                            param(
                                FirebaseAnalytics.Param.SCREEN_CLASS,
                                AddBirdFragment::class.qualifiedName ?: "AddBird"
                            )
                            param(FirebaseAnalytics.Param.SCREEN_NAME, "add_bird")
                        }

                        loadFragment(supportFragmentManager, AddBirdFragment.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.action_map -> {
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                            param(
                                FirebaseAnalytics.Param.SCREEN_CLASS,
                                MapFragment::class.qualifiedName ?: "Map"
                            )
                            param(FirebaseAnalytics.Param.SCREEN_NAME, "map")
                        }

                        loadFragment(supportFragmentManager, MapFragment.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.action_profile -> {
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                            param(
                                FirebaseAnalytics.Param.SCREEN_CLASS,
                                ProfileNotLoggedIn::class.qualifiedName ?: "Profile"
                            )
                            param(FirebaseAnalytics.Param.SCREEN_NAME, "profile")
                        }

                        loadFragment(supportFragmentManager, ProfileNotLoggedIn.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }
                }

                false
            }
            selectedItemId = R.id.action_gallery
        }
    }
}
