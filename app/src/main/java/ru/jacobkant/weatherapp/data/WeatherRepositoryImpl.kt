package ru.jacobkant.weatherapp.data

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.jacobkant.weatherapp.model.AppMode
import ru.jacobkant.weatherapp.model.TemperatureUnit
import ru.jacobkant.weatherapp.model.Weather
import ru.jacobkant.weatherapp.model.WeatherRepository
import ru.jacobkant.weatherapp.openWeatherMapApi.OpenWeatherMapApi
import ru.jacobkant.weatherapp.openWeatherMapApi.model.CurrentWeatherResponse
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: OpenWeatherMapApi,
    private val cityDao: CityDao,
    private val settingsPrefs: SettingsPreference
) : WeatherRepository {

    @ExperimentalStdlibApi
    override fun findCityByName(namePart: String): Single<List<CityRow>> =
        cityDao.findByName(namePart)
            .subscribeOn(Schedulers.io())

    override fun getWeatherByCityId(cityId: Long): Single<Weather> {
        appMode = AppMode.CityManual
        return api.getCurrentWeather(mapOf("id" to "$cityId"))
            .map(this::mapToWeather)
    }

    override fun getWeatherByCoordinate(lat: Double, lon: Double): Single<Weather> {
        appMode = AppMode.CityByLocation
        return api.getCurrentWeather(mapOf("lat" to "$lat", "lon" to "$lon"))
            .map(this::mapToWeather)
    }

    override var currentTemperatureUnit: TemperatureUnit
        get() = settingsPrefs.currentTemperatureUnit
        set(value) {
            settingsPrefs.currentTemperatureUnit = value
        }

    override var appMode: AppMode
        get() = settingsPrefs.appMode
        private set(value) {
            settingsPrefs.appMode = value
        }

    override var lastCityId: Long
        get() = settingsPrefs.lastCityId
        private set(value) {
            settingsPrefs.lastCityId = value
        }

    private fun mapToWeather(it: CurrentWeatherResponse): Weather {
        lastCityId = it.id!!
        return Weather(
            placeName = it.name ?: "",
            temperature = it.main!!.temp!!,
            coordinates = Pair(it.coord!!.lat!!, it.coord!!.lon!!),
            iconCode = it.weather?.firstOrNull()?.icon ?: "",
            weatherDescription = it.weather?.firstOrNull()?.description ?: "",
            humidity = it.main?.humidity?.toInt() ?: 0,
            pressure = it.main?.pressure?.toInt() ?: 0,
            windSpeed = it.wind?.speed?.toInt() ?: 0,
            cloudnes = it.clouds?.all?.toInt() ?: 0,
            // all responses use C temp Unit (from okhttp interceptor)
            currentTemperatureUnit = TemperatureUnit.C
        )
    }

}