package ru.itmo.chori.birdsexplorer

import com.google.firebase.firestore.GeoPoint
import org.junit.Test
import org.junit.Assert.*
import ru.itmo.chori.birdsexplorer.data.BirdModel
import ru.itmo.chori.birdsexplorer.data.BirdViewModel

class BirdViewModelValidatorTest {
    @Test
    fun validName() {
        val bird = BirdModel(name = "Pigeon")
        val viewModel = BirdViewModel(bird)

        Assert.assertTrue(viewModel.isNameValid)
    }

    @Test
    fun emptyName() {
        val bird = BirdModel(name = "")
        val viewModel = BirdViewModel(bird)

        Assert.assertFalse(viewModel.isNameValid)
    }

    @Test
    fun blankName() {
        val bird = BirdModel(name = "    ")
        val viewModel = BirdViewModel(bird)

        Assert.assertFalse(viewModel.isNameValid)
    }

    @Test
    fun nullName() {
        val bird = BirdModel(name = null)
        val viewModel = BirdViewModel(bird)

        Assert.assertFalse(viewModel.isNameValid)
    }

    @Test
    fun validImage() {
        val bird = BirdModel(image = "pigeon.jpeg")
        val viewModel = BirdViewModel(bird)

        Assert.assertTrue(viewModel.isImageValid)
    }

    @Test
    fun emptyImage() {
        val bird = BirdModel(image = "")
        val viewModel = BirdViewModel(bird)

        Assert.assertFalse(viewModel.isImageValid)
    }

    @Test
    fun blankImage() {
        val bird = BirdModel(image = "   ")
        val viewModel = BirdViewModel(bird)

        Assert.assertFalse(viewModel.isImageValid)
    }

    @Test
    fun nullImage() {
        val bird = BirdModel(image = null)
        val viewModel = BirdViewModel(bird)

        Assert.assertFalse(viewModel.isImageValid)
    }

    @Test
    fun validLocation() {
        val bird = BirdModel(location = GeoPoint(10.0, 20.0))
        val viewModel = BirdViewModel(bird)

        Assert.assertTrue(viewModel.isLocationValid)
    }

    @Test
    fun emptyLocation() {
        val bird = BirdModel(location = null)
        val viewModel = BirdViewModel(bird)

        Assert.assertFalse(viewModel.isLocationValid)
    }

// // Works only with JUnit 5. GitHub CI can't run it
//
//    companion object {
//        @JvmStatic
//        fun isValid(): CartesianProductTest.Sets = CartesianProductTest.Sets()
//            .add("Pigeon", "     ", "", null)
//            .add("pigeon.png", " ", "", null)
//            .add(GeoPoint(-20.0, 10.0), null)
//    }
//
//    @CartesianProductTest
//    fun isValid(name: String?, image: String?, location: GeoPoint?) {
//        val bird = BirdModel(name = name, image = image, location = location)
//        val birdModel = BirdViewModel(bird)
//
//        val expected = !name.isNullOrBlank() && !image.isNullOrBlank() && location != null
//        Assert.assertEquals(expected, birdModel.isValid)
//    }
}