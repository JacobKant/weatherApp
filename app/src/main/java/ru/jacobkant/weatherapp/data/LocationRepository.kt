package ru.jacobkant.weatherapp.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import io.reactivex.Maybe
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(private val context: Context) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun getCurrentLocation(): Maybe<Location> {
        return Maybe.create<Location> {
            try {
                val checkLocationPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )

                if (checkLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    it.onComplete()
                    return@create
                }

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    it.onComplete()
                    return@create
                }

                locationManager.requestSingleUpdate(
                    Criteria().apply {
                        accuracy = Criteria.ACCURACY_COARSE
                        isCostAllowed = false
                        isSpeedRequired = true
                    },
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
            } catch (err: Throwable) {
                it.onError(err)
            }
        }
    }

}