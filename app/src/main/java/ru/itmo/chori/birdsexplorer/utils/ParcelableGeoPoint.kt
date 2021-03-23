package ru.itmo.chori.birdsexplorer.utils

import android.os.Parcel

import android.os.Parcelable

import com.google.firebase.firestore.GeoPoint

/**
 * Parcelable wrapper for firebase GeoPoint
 * @author https://stackoverflow.com/a/14475476/12411158
 */
class ParcelableGeoPoint : Parcelable {
    private var point: GeoPoint

    constructor(lat: Double, lon: Double) {
        point = GeoPoint(lat, lon)
    }

    constructor(parcel: Parcel) {
        val data = DoubleArray(2)
        parcel.readDoubleArray(data)
        point = GeoPoint(data[0], data[1])
    }

    override fun describeContents(): Int {
        return 0
    }

    fun unwrap(): GeoPoint {
        return point
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDoubleArray(doubleArrayOf(point.latitude, point.longitude))
    }

    companion object CREATOR : Parcelable.Creator<ParcelableGeoPoint> {
        override fun createFromParcel(parcel: Parcel): ParcelableGeoPoint {
            return ParcelableGeoPoint(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableGeoPoint?> {
            return arrayOfNulls(size)
        }
    }
}
