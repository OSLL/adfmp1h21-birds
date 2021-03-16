package ru.itmo.chori.birdsexplorer.data

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class BirdModel(
    val name: String = "",
    val image: String = "",
    val seen_at: Timestamp = Timestamp.now(),
    val location: GeoPoint = GeoPoint(0.0, 0.0)
) : AbstractSafeParcelable() {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Timestamp::class.java.classLoader)!!,
        GeoPoint(parcel.readDouble(), parcel.readDouble())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeParcelable(seen_at, flags)
        parcel.writeParcelable(location, flags)
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

private fun Parcel.writeParcelable(geoPoint: GeoPoint, flags: Int) {
    writeDouble(geoPoint.latitude)
    writeDouble(geoPoint.longitude)
}
