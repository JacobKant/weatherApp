package ru.jacobkant.weatherapp.presentation

import io.reactivex.rxkotlin.addTo
import ru.jacobkant.weatherapp.data.CityRow
import ru.jacobkant.weatherapp.data.EventBus
import ru.jacobkant.weatherapp.model.WeatherInteractor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SelectCityViewState(
    val cityList: List<CityRow> = listOf(),
    val inputText: String = ""
)

data class SelectCity(val cityRow: CityRow) : Event()
data class QueryChanged(val query: String) : Event()

class SelectCityViewModel @Inject constructor(
    private val interactor: WeatherInteractor,
    private val bus: EventBus
) : MviViewModel<SelectCityViewState>() {

    override val initialState: SelectCityViewState
        get() = SelectCityViewState()

    init {
        events.ofType(QueryChanged::class.java)
            .debounce(300, TimeUnit.MILLISECONDS)
            .switchMapSingle {
                interactor.findCityByName(it.query)
            }
            .subscribe {
                state.onNext(
                    currentState.copy(
                        cityList = it
                    )
                )
            }
            .addTo(requestsDisposables)
    }

    override fun onEvent(event: Event) {
        when (event) {
            is SelectCity -> {
                bus.sendEvent(ru.jacobkant.weatherapp.data.SelectCity(event.cityRow.id))
            }
        }
    }

}