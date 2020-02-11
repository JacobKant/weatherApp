package ru.jacobkant.weatherapp.presentation

import android.content.Context
import android.os.Parcelable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.addTo
import ru.jacobkant.weatherapp.R
import ru.jacobkant.weatherapp.data.EventBus
import ru.jacobkant.weatherapp.data.SelectCity
import ru.jacobkant.weatherapp.model.TemperatureUnit
import ru.jacobkant.weatherapp.model.Weather
import ru.jacobkant.weatherapp.model.WeatherInteractor
import ru.jacobkant.weatherapp.openWeatherMapApi.OpenWeatherMapApi
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

data class OnResume(val savedInstanceState: Parcelable?) : Event()
data class OnPause(val savedInstanceState: Parcelable?) : Event()
data class SelectTemperatureUnit(val tempUnit: TemperatureUnit) : Event()
data class ClickChangeCity(val cityId: Long) : Event()
object ClickMyLocation : Event()

class WeatherViewModel @Inject constructor(
    private val context: Context,
    private val weatherInteractor: WeatherInteractor,
    bus: EventBus
) : MviViewModel<WeatherViewState>() {

    init {
        bus.getEvent(SelectCity::class.java)
            .subscribe {
                weatherInteractor.fetchWeatherByCityId(it.cityId)
                    .map(this::mapToWeatherViewState)
                    .request()
            }.addTo(requestsDisposables)
    }

    override val initialState: WeatherViewState
        get() = WeatherViewState()

    override fun onEvent(event: Event) {
        when (event) {
            is OnResume -> {
                weatherInteractor.getInitialWeather()
                    .map(this::mapToWeatherViewState)
                    .request()
            }
            is ClickMyLocation -> {
                weatherInteractor.fetchWeatherByLocation()
                    .map(this::mapToWeatherViewState)
                    .request()
            }
            is ClickChangeCity -> {
                weatherInteractor.fetchWeatherByCityId(event.cityId)
                    .map(this::mapToWeatherViewState)
                    .request()
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
                        .addTo(requestsDisposables)
                }
            }
        }
    }

    private fun Single<WeatherViewState>.request() {
        this.toMaybe().request()
    }

    private fun Maybe<WeatherViewState>.request() {
        this
            .doOnSubscribe {
                state.onNext(
                    currentState.copy(
                        isWeatherLoading = true
                    )
                )
            }
            .toObservable()
            .subscribe(
                {
                    state.onNext(it)
                },
                {
                    state.onNext(
                        currentState.copy(
                            isWeatherLoading = false,
                            errorText = context.getString(R.string.load_weather_common_error)
                        )
                    )
                },
                {
                    state.onNext(
                        currentState.copy(
                            isWeatherLoading = false
                        )
                    )
                }).addTo(requestsDisposables)
    }

    private fun mapToWeatherViewState(it: Weather): WeatherViewState {
        return currentState.copy(
            cityName = it.placeName,
            tempDescription = it.weatherDescription,
            tempText = "${it.temperature.toInt()}º",
            selectedTempUnit = it.currentTemperatureUnit,
            iconUrl = OpenWeatherMapApi.getIconUrl(it.iconCode),
            facts = listOf(
                context.getString(R.string.frag_weather_wind_label) to context.getString(
                    R.string.frag_weather_wind_value_template,
                    it.windSpeed.toString()
                ),
                context.getString(R.string.frag_weather_pressure_label) to context.getString(
                    R.string.frag_weather_pressure_value_template,
                    it.pressure.toString()
                ),
                context.getString(R.string.frag_weather_humidity_label) to context.getString(
                    R.string.frag_weather_humidity_value_template,
                    it.humidity.toString()
                ),
                context.getString(R.string.frag_weather_cloudnes_label) to context.getString(
                    R.string.frag_weather_cloudnes_value_template,
                    it.cloudnes.toString()
                )
            ),
            errorText = null,
            isWeatherLoading = false
        )
    }


}