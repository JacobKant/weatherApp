package ru.jacobkant.weatherapp.model

import io.reactivex.Single
import ru.jacobkant.weatherapp.data.CityRow


interface WeatherRepository {
    var currentTemperatureUnit: TemperatureUnit
    val appMode: AppMode
    val lastCityId: Long

    fun getWeatherByCoordinate(lat: Double, lon: Double): Single<Weather>
    fun getWeatherByCityId(cityId: Long): Single<Weather>
    fun findCityByName(namePart: String): Single<List<CityRow>>
}