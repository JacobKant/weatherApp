package ru.jacobkant.weatherapp.model

import io.reactivex.Single
import ru.jacobkant.weatherapp.data.CityRow


interface WeatherRepository {
    fun getWeatherByCoordinate(lat: Double, lon: Double): Single<Weather>
    fun getWeatherByCityId(cityId: Long): Single<Weather>
    fun getCurrentTemperatureUnit(): TemperatureUnit
    fun setCurrentTemperatureUnit(t: TemperatureUnit)
    fun findCityByName(namePart: String): Single<List<CityRow>>
}