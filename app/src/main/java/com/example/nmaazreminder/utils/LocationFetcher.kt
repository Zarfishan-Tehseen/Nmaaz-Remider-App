package com.example.nmaazreminder.utils

import androidx.annotation.RequiresPermission
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationFetcher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Fetches the current high-accuracy GPS coordinates of the device.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresPermission(anyOf = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"])
    suspend fun getCurrentCoordinates(): Location? = suspendCancellableCoroutine { continuation ->
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location: Location? ->
            if (continuation.isActive) continuation.resume(location)
        }.addOnFailureListener {
            if (continuation.isActive) continuation.resume(null)
        }
    }

    /**
     * Converts raw coordinates into a readable City Name using Android's Geocoder class.
     */
    fun getCityName(latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                // Return locality (city), subAdminArea, or fallback to country
                addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].countryName ?: "Unknown Location"
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown Location"
        }
    }
}