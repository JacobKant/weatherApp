package ru.jacobkant.weatherapp.model

import io.reactivex.Maybe
import io.reactivex.Single
import ru.jacobkant.weatherapp.data.CityRow

interface WeatherInteractor {
    fun changeTemperatureUnit(unit: TemperatureUnit): Maybe<Weather>
    fun fetchWeatherByLocation(): Maybe<Weather>
    fun fetchWeatherByCityId(cityId: Long): Single<Weather>
    fun findCityByName(namePart: String): Single<List<CityRow>>
    fun getInitialWeather(): Maybe<Weather>
}