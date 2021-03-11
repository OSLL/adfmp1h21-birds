package ru.itmo.chori.birdsexplorer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import kotlinx.android.synthetic.main.activity_main.*
import ru.itmo.chori.birdsexplorer.profile.ProfileNotLoggedIn

class MainActivity : AppCompatActivity() {
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        analytics = FirebaseAnalytics.getInstance(this)

        with(bottom_navigation) {
            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.action_gallery -> {
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                            param(FirebaseAnalytics.Param.SCREEN_CLASS, GalleryFragment::class.qualifiedName ?: "Gallery")
                            param(FirebaseAnalytics.Param.SCREEN_NAME, "gallery")
                        }

                        loadFragment(GalleryFragment.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.action_add_bird -> {
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                            param(FirebaseAnalytics.Param.SCREEN_CLASS, AddBirdFragment::class.qualifiedName ?: "AddBird")
                            param(FirebaseAnalytics.Param.SCREEN_NAME, "add_bird")
                        }

                        loadFragment(AddBirdFragment.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.action_map -> {
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                            param(FirebaseAnalytics.Param.SCREEN_CLASS, MapFragment::class.qualifiedName ?: "Map")
                            param(FirebaseAnalytics.Param.SCREEN_NAME, "map")
                        }

                        loadFragment(MapFragment.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.action_profile -> {
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                            param(FirebaseAnalytics.Param.SCREEN_CLASS, ProfileNotLoggedIn::class.qualifiedName ?: "Profile")
                            param(FirebaseAnalytics.Param.SCREEN_NAME, "profile")
                        }

                        loadFragment(ProfileNotLoggedIn.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }
                }

                false
            }
            selectedItemId = R.id.action_gallery
        }
    }

    private fun loadFragment(fragment: Fragment) {
        with(supportFragmentManager.beginTransaction()) {
            replace(app_content.id, fragment)
            commit()
        }
    }
}
