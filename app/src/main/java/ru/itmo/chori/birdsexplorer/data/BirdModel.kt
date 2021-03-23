package ru.itmo.chori.birdsexplorer.data

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class BirdModel(
    @DocumentId val id: String? = null,
    val name: String? = null,
    val image: String? = null,
    val seen_at: Timestamp? = Timestamp.now(),
    val location: GeoPoint? = null,
    val geohash: String? = null,
    val author: String? = null
) : AbstractSafeParcelable() {
    constructor(parcel: Parcel) : this(
        id = parcel.readString(),
        name = parcel.readString(),
        image = parcel.readString(),
        seen_at = parcel.readParcelable(Timestamp::class.java.classLoader),
        location = GeoPoint(parcel.readDouble(), parcel.readDouble()),
        geohash = parcel.readString(),
        author = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val handler = SafeParcelWriter.beginObjectHeader(parcel)
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeParcelable(seen_at, flags)
        parcel.writeParcelable(location)
        parcel.writeString(geohash)
        parcel.writeString(author)
        SafeParcelWriter.finishObjectHeader(parcel, handler)
    }

    companion object CREATOR : Parcelable.Creator<BirdModel> {
        override fun createFromParcel(parcel: Parcel): BirdModel {
            return BirdModel(parcel)
        }

        override fun newArray(size: Int): Array<BirdModel?> {
            return arrayOfNulls(size)
        }
    }
}

private fun Parcel.writeParcelable(geoPoint: GeoPoint?) {
    writeDouble(geoPoint?.latitude ?: 0.0)
    writeDouble(geoPoint?.longitude ?: 0.0)
}
