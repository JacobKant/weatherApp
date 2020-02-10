package ru.jacobkant.weatherapp.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import io.reactivex.Maybe
import javax.inject.Inject


class LocationRepository @Inject constructor(private val context: Context) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun getCurrentLocation(): Maybe<Location> {
        return Maybe.create<Location> {
            try {
                val provider = locationManager.getBestProvider(
                    Criteria().apply {
                        accuracy = Criteria.ACCURACY_FINE
                        isCostAllowed = false
                    },
                    true
                )
                val checkLocationPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )

                if (provider == null && checkLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    it.onComplete()
                    return@create
                }

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    openLocationSettings()
                    it.onComplete()
                    return@create
                }

                val lastKnownLocation = locationManager.getLastKnownLocation(provider!!)
                if (lastKnownLocation != null) {
                    it.onSuccess(lastKnownLocation)
                    return@create
                } else {
                    locationManager.requestSingleUpdate(
                        provider,
                        object : LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                location?.let { it1 -> it.onSuccess(it1) }
                            }

                            override fun onStatusChanged(
                                provider: String?,
                                status: Int,
                                extras: Bundle?
                            ) {
                            }

                            override fun onProviderEnabled(provider: String?) {}
                            override fun onProviderDisabled(provider: String?) {}
                        },
                        Looper.getMainLooper()
                    )
                }
            } catch (err: Throwable) {
                it.onError(err)
            }
        }
    }

    private fun openLocationSettings() {
        val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(settingsIntent)
    }
}