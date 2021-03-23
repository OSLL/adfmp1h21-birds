package ru.itmo.chori.birdsexplorer.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.GeoPoint


class BirdViewModel(bird: BirdModel? = null) : ViewModel() {
    val name: MutableLiveData<String?>
    val image: MutableLiveData<String?>
    val location: MutableLiveData<GeoPoint?>

    init {
        if (bird != null) {
            name = MutableLiveData(bird.name)
            image = MutableLiveData(bird.image)
            location = MutableLiveData(bird.location)
        } else {
            name = MutableLiveData(null)
            image = MutableLiveData(null)
            location = MutableLiveData(null)
        }
    }
}

class BirdViewModelFactory(private val bird: BirdModel? = null) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BirdViewModel(bird) as T
    }
}
