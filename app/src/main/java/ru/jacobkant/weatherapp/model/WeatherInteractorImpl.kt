package ru.jacobkant.weatherapp.model

import android.Manifest
import android.util.Log
import io.reactivex.Maybe
import io.reactivex.Single
import ru.jacobkant.weatherapp.data.*
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class WeatherInteractorImpl @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val permissionHelper: PermissionHelper,
    private val router: AppRouter
) : WeatherInteractor {

    private var lastWeather: Weather? = null

    override fun findCityByName(namePart: String): Single<List<CityRow>> {
        return weatherRepository.findCityByName("%$namePart%")
    }

    override fun fetchWeatherByLocation(): Maybe<Weather> {
        return permissionHelper.requestPermission(
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        )
            .flatMapMaybe { locationPermissionIsGranted ->
                if (locationPermissionIsGranted) {
                    permissionHelper.requestEnableGps()
                        .flatMapMaybe { gpsEnabled ->
                            if (gpsEnabled) {
                                locationRepository.getCurrentLocation()
                                    .flatMapSingle {
                                        weatherRepository.getWeatherByCoordinate(
                                            it.latitude,
                                            it.longitude
                                        )
                                    }
                                    .map {
                                        val weather = transformWeatherWithCurrentTemperatureUnit(it)
                                        lastWeather = weather
                                        weather
                                    }.toMaybe()
                            } else {
                                Maybe.empty<Weather>()
                            }
                        }
                } else {
                    router.pushCommand(ShowMessageCommand("Разрешите приложению использовать геопозицию в настройках"))
                    fetchWeatherByCityId(weatherRepository.lastCityId).toMaybe()
                }
            }
    }

    override fun fetchWeatherByCityId(cityId: Long): Single<Weather> {
        return weatherRepository.getWeatherByCityId(cityId)
            .map {
                val weather = transformWeatherWithCurrentTemperatureUnit(it)
                lastWeather = weather
                weather
            }
    }

    override fun changeTemperatureUnit(unit: TemperatureUnit): Maybe<Weather> {
        val fromUnit = weatherRepository.currentTemperatureUnit
        weatherRepository.currentTemperatureUnit = unit

        return Maybe.create<Weather> { maybe ->
            if (unit == fromUnit) {
                maybe.onComplete()
            } else {
                val let = lastWeather?.let {
                    val newWeather = it.copy(
                        temperature = convertTemperature(
                            fromUnit = fromUnit,
                            fromTemp = it.temperature,
                            toUnit = unit
                        ),
                        currentTemperatureUnit = unit
                    )
                    lastWeather = newWeather
                    maybe.onSuccess(newWeather)
                    newWeather
                }
                if (let == null) maybe.onComplete()
            }
        }
    }

    override fun getInitialWeather(): Maybe<Weather> {
        return when (weatherRepository.appMode) {
            AppMode.CityByLocation -> fetchWeatherByLocation()
            AppMode.CityManual -> fetchWeatherByCityId(weatherRepository.lastCityId).toMaybe()
        }
    }

    private fun transformWeatherWithCurrentTemperatureUnit(it: Weather): Weather {
        return it.copy(
            currentTemperatureUnit = weatherRepository.currentTemperatureUnit,
            temperature = convertTemperature(
                fromUnit = it.currentTemperatureUnit,
                fromTemp = it.temperature,
                toUnit = weatherRepository.currentTemperatureUnit
            )
        )
    }

    private fun convertTemperature(
        fromUnit: TemperatureUnit,
        fromTemp: Double,
        toUnit: TemperatureUnit
    ): Double {
        return when {
            fromUnit == toUnit -> fromTemp
            toUnit == TemperatureUnit.C -> (5.0 / 9.0) * (fromTemp - 32)
            toUnit == TemperatureUnit.F -> (fromTemp * (9.0 / 5.0)) + 32
            else -> fromTemp
        }
    }


}