package ru.jacobkant.weatherapp.presentation

import android.os.Parcelable
import io.reactivex.rxkotlin.addTo
import ru.jacobkant.weatherapp.data.EventBus
import ru.jacobkant.weatherapp.data.SelectCity
import ru.jacobkant.weatherapp.model.TemperatureUnit
import ru.jacobkant.weatherapp.model.Weather
import ru.jacobkant.weatherapp.model.WeatherInteractor
import javax.inject.Inject

data class WeatherViewState(
    val cityName: String = "",
    val tempText: String = "",
    val iconUrl: String = "",
    val selectedTempUnit: TemperatureUnit = TemperatureUnit.C,
    val tempDescription: String = "",
    val facts: List<Pair<String, String>> = listOf(),
    val isWeatherLoading: Boolean = false,
    val errorText: String? = null
)

data class OnCreate(val savedInstanceState: Parcelable?) : Event()
data class SelectTemperatureUnit(val tempUnit: TemperatureUnit) : Event()
data class ClickChangeCity(val cityId: Long) : Event()
object ClickMyLocation : Event()

class WeatherViewModel @Inject constructor(
    private val weatherInteractor: WeatherInteractor,
    private val bus: EventBus
) : MviViewModel<WeatherViewState>() {

    init {
        bus.getEvent(SelectCity::class.java)
            .subscribe {
                fetchByCity(it.cityId)
            }.addTo(disposables)
    }

    override val initialState: WeatherViewState
        get() = WeatherViewState()

    override fun onEvent(event: Event) {
        when (event) {
            is OnCreate -> {}
            is ClickMyLocation -> {
                fetchCurrentWeatherByLocation()
            }
            is ClickChangeCity -> {
                fetchByCity(event.cityId)
            }
            is SelectTemperatureUnit -> {
                if (currentState.selectedTempUnit != event.tempUnit) {
                    weatherInteractor.changeTemperatureUnit(event.tempUnit)
                        .subscribe(
                            {
                                state.onNext(
                                    mapToWeatherViewState(it)
                                )
                            },
                            {
                            }
                        )
                        .addTo(disposables)
                }
            }
        }
    }

    private fun fetchByCity(cityId: Long) {
        state.onNext(
            currentState.copy(
                isWeatherLoading = true
            )
        )
        weatherInteractor.fetchWeatherByCityId(cityId)
            .map {
                mapToWeatherViewState(it)
            }
            .toObservable()
            .subscribe({
                state.onNext(it)
            }, {
                state.onNext(
                    currentState.copy(
                        isWeatherLoading = false,
                        errorText = "Ошибка загрузки, попробуйте позже"
                    )
                )
            }).addTo(disposables)
    }

    private fun fetchCurrentWeatherByLocation() {
        state.onNext(
            currentState.copy(
                isWeatherLoading = true
            )
        )
        weatherInteractor.fetchWeatherByLocation()
            .map {
                mapToWeatherViewState(it)
            }
            .toObservable()
            .subscribe({
                state.onNext(it)
            }, {
                state.onNext(
                    currentState.copy(
                        isWeatherLoading = false,
                        errorText = "Ошибка загрузки, попробуйте позже"
                    )
                )
            }).addTo(disposables)
    }

    private fun mapToWeatherViewState(it: Weather): WeatherViewState {
        return currentState.copy(
            cityName = it.placeName,
            tempDescription = it.weatherDescription,
            tempText = "${it.temperature.toInt()}º",
            selectedTempUnit = it.currentTemperatureUnit,
            iconUrl = "https://openweathermap.org/img/wn/${it.iconCode}@2x.png",
            facts = listOf(
                "Ветер" to "${it.windSpeed} м/с, западный",
                "Давление" to "${it.pressure} мм рт. ст.",
                "Влажность" to "${it.humidity}%",
                "Облачность" to "${it.cloudnes}%"
            ),
            errorText = null,
            isWeatherLoading = false
        )
    }


}