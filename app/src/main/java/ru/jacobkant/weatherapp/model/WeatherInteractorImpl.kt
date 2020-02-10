package ru.jacobkant.weatherapp.model

import io.reactivex.Maybe
import io.reactivex.Single
import ru.jacobkant.weatherapp.data.CityRow
import ru.jacobkant.weatherapp.data.LocationRepository
import javax.inject.Inject

class WeatherInteractorImpl @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository
) : WeatherInteractor {

    private var lastWeather: Weather? = null

    override fun findCityByName(namePart: String): Single<List<CityRow>> {
        return weatherRepository.findCityByName("%$namePart%")
    }

    override fun fetchWeatherByLocation(): Single<Weather> {
        return locationRepository.getCurrentLocation()
            .flatMapSingle {
                weatherRepository.getWeatherByCoordinate(it.latitude, it.longitude)
            }
            .map {
                val weather = transformWeatherWithCurrentTemperatureUnit(it)
                lastWeather = weather
                weather
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
        val fromUnit = weatherRepository.getCurrentTemperatureUnit()
        weatherRepository.setCurrentTemperatureUnit(unit)

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

    private fun transformWeatherWithCurrentTemperatureUnit(it: Weather): Weather {
        return it.copy(
            currentTemperatureUnit = weatherRepository.getCurrentTemperatureUnit(),
            temperature = convertTemperature(
                fromUnit = it.currentTemperatureUnit,
                fromTemp = it.temperature,
                toUnit = weatherRepository.getCurrentTemperatureUnit()
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