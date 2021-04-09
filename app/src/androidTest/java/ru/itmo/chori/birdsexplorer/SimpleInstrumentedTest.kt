package ru.itmo.chori.birdsexplorer

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import ru.itmo.chori.birdsexplorer.data.BirdModel
import ru.itmo.chori.birdsexplorer.profile.ARG_USER
import ru.itmo.chori.birdsexplorer.profile.ProfileLoggedIn
import ru.itmo.chori.birdsexplorer.profile.ProfileNotLoggedIn
import java.util.*

@LargeTest
@RunWith(AndroidJUnit4::class)
class SimpleInstrumentedTest {
    @Test
    fun whenOpenProfile_seeWelcomeMessage() {
        val user = mock(FirebaseUser::class.java)
        given(user.displayName).willReturn("Admin")
        given(user.uid).willReturn(UUID.randomUUID().toString())

        val args = bundleOf(ARG_USER to user)

        launchFragmentInContainer<ProfileLoggedIn>(args)
        onView(withSubstring("Welcome back, Admin")).check(matches(isDisplayed()))
    }

    @Test
    fun whenOpenProfileNotLoggedIn_seeWelcomeMessageWithoutNickname() {
        launchFragmentInContainer<ProfileNotLoggedIn>()
        onView(withSubstring("You are not logged in")).check(matches(isDisplayed()))
    }

    @Test
    fun whenOpenMap_seeMap() {
        launchFragmentInContainer<MapFragment>()
        onView(withId(R.id.map)).check(matches(isDisplayed()))
    }

    @Test
    fun whenOpenBirdCard_seeMapAndTime() {
        val bird = BirdModel(
            id = UUID.randomUUID().toString(),
            location = GeoPoint(0.0, 90.0),
            image = "bird.png"
        )
        val args = bundleOf(ARG_BIRD to bird)

        launchFragmentInContainer<BirdFragment>(args)
        onView(withSubstring("Seen at")).check(matches(isDisplayed()))
        onView(withId(R.id.mapView)).check(matches(isDisplayed()))
    }

    @Test
    fun whenCancelPickLocationDialog_itDismiss() {
        // Assumes that "MyDialogFragment" extends the DialogFragment class.
        with(launchFragment<PickLocationFragment>()) {
            onFragment { fragment ->
                assertThat(fragment.dialog).isNotNull()
                assertThat(fragment.requireDialog().isShowing).isTrue()
                fragment.dismiss()
                fragment.parentFragmentManager.executePendingTransactions()
                assertThat(fragment.dialog).isNull()
            }
        }

        // Assumes that the dialog had a button
        // containing the text "Cancel".
        onView(withText("Cancel")).check(doesNotExist())
    }
}
