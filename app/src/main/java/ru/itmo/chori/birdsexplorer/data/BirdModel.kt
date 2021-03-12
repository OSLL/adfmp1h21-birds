package ru.itmo.chori.birdsexplorer.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class BirdModel(
    val name: String = "",
    val image: String = "",
    val seen_at: Timestamp = Timestamp.now(),
    val location: GeoPoint = GeoPoint(0.0, 0.0)
)
