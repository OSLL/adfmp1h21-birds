package ru.itmo.chori.birdsexplorer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(bottom_navigation) {
            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.action_gallery -> {
                        loadFragment(GalleryFragment.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.action_add_bird -> {
                        loadFragment(AddBirdFragment.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.action_map -> {
                        loadFragment(MapFragment.newInstance())
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.action_profile -> {
                        loadFragment(ProfileFragment.newInstance())
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
