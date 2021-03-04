package com.myapps.simpleweatherapp.utils

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.io.IOException
import java.util.*


//fun getRoundedNum(num: Double): Float {
//
//    Timber.i("num : $num")
//    val df = DecimalFormat("#.####")
////    val df = DecimalFormat("#.####")
//    return df.format(num).toFloat()
//}


fun textContainsArabic(text: String): Boolean {
    text.map { char ->
        if (Character.UnicodeBlock.of(char) === Character.UnicodeBlock.ARABIC)
            return true
    }
    return false
}

fun getLocationName(geoCoder: Geocoder, lat: Double?, lng: Double?): String? {
    return try {
        lat?.let { latitude ->
            lng?.let { longitude ->
                val addressList: MutableList<Address> =
                    geoCoder.getFromLocation(latitude, longitude, 1)
                if (!addressList.isNullOrEmpty()) {
                    val obj = addressList[0]
                    Timber.i(obj.getAddressLine(0))
                    Timber.i("obj.countryName: ${obj.countryName}")
                    Timber.i("obj.countryCode: ${obj.countryCode}")
                    Timber.i("obj.adminArea: ${obj.adminArea}")
                    Timber.i("obj.postalCode: ${obj.postalCode}")
                    Timber.i("obj.subAdminArea: ${obj.subAdminArea}")
                    Timber.i("obj.locality: ${obj.locality}");
                    Timber.i("obj.subThoroughfare: ${obj.subThoroughfare}")
                    if (Locale.getDefault() == Locale("ar"))
                        "${if (obj.subAdminArea.isNullOrEmpty()) "" else "${obj.subAdminArea}،"} ${obj.adminArea}، ${obj.countryName}"
                    else
                        "${if (obj.subAdminArea.isNullOrEmpty()) "" else "${obj.subAdminArea},"} ${obj.adminArea}, ${obj.countryName}"
                } else
                    null
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun splitLocationDetails(s: String): String {
    return if (s.contains(','))
        s.split(',')[0]
    else
        s.split('،')[0]
}

class LocationUpdatesUseCase(private val client: FusedLocationProviderClient) {

    @SuppressLint("MissingPermission")
    fun fetchUpdates(): Flow<LatLng> = callbackFlow {

        val callBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.locations[0]
                val userLocation = LatLng(
                    location.latitude,
                    location.longitude
                )
                try {
                    offer(userLocation)
                } catch (t: Throwable) {
                    offer(null)
                }

            }
        }
        client.requestLocationUpdates(locationRequest, callBack, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callBack) }
    }

    companion object {
        private val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}