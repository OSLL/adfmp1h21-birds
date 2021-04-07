package ru.itmo.chori.birdsexplorer.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class BirdModel(
    @DocumentId val id: String? = null,
    val name: String? = null,
    val image: String? = null,
    val seen_at: @RawValue Timestamp? = Timestamp.now(),
    val location: @RawValue GeoPoint? = null,
    val geohash: String? = null,
    val author: String? = null
) : Parcelable
