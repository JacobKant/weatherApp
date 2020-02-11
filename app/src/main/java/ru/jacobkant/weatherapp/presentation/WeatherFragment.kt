package ru.jacobkant.weatherapp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.frag_weather.*
import ru.jacobkant.weatherapp.App
import ru.jacobkant.weatherapp.R
import ru.jacobkant.weatherapp.di.getViewModel
import ru.jacobkant.weatherapp.model.TemperatureUnit

class WeatherFragment : Fragment() {

    private val viewModel: WeatherViewModel by getViewModel(App.ComponentHolder.appComponent)

    private val compositeDisposable = CompositeDisposable()

    private lateinit var rxPermission: RxPermissions
    private var savedInstanceState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        App.ComponentHolder.appComponent.inject(this)
        rxPermission = RxPermissions(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_weather, container, false)
    }

    private val tempChangeListener: (RadioGroup, Int) -> Unit = { _, checkedId ->
        viewModel.events.onNext(
            SelectTemperatureUnit(
                when (checkedId) {
                    R.id.frag_weather_select_C -> TemperatureUnit.C
                    R.id.frag_weather_select_F -> TemperatureUnit.F
                    else -> throw IllegalStateException()
                }
            )
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.events.onNext(
            OnResume(savedInstanceState)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        frag_weather_city_change.setOnClickListener {
            val selectCityDialogFragment = SelectCityDialogFragment()
            selectCityDialogFragment.setStyle(
                DialogFragment.STYLE_NO_TITLE,
                R.style.AppTheme_Dialog_Alert
            )
            selectCityDialogFragment.show(requireActivity().supportFragmentManager, null)
        }
        frag_weather_city_by_location.setOnClickListener {
            onClickMyLocation()
        }

        frag_weather_select.setOnCheckedChangeListener(tempChangeListener)
        viewModel.viewState.subscribe {
            frag_weather_city_name.text = if (it.cityName.isEmpty()) getString(R.string.city_placeholder) else it.cityName
            frag_weather_temp.text = it.tempText
            frag_weather_description.text = it.tempDescription
            frag_weather_progress_container.isVisible = it.isWeatherLoading
            frag_weather_temperature_container.isVisible = !it.isWeatherLoading

            frag_weather_select.setOnCheckedChangeListener(null)
            frag_weather_select.check(
                when (it.selectedTempUnit) {
                    TemperatureUnit.C -> R.id.frag_weather_select_C
                    TemperatureUnit.F -> R.id.frag_weather_select_F
                }
            )
            frag_weather_select.setOnCheckedChangeListener(tempChangeListener)

            Glide.with(frag_weather_icon)
                .load(it.iconUrl)
                .into(frag_weather_icon)
            it.facts.forEachIndexed { index, (label, value) ->
                val factContainer = frag_weather_factsContainer.getChildAt(index) as ViewGroup
                val labelTv = factContainer.getChildAt(0) as TextView
                val valueTv = factContainer.getChildAt(1) as TextView
                labelTv.text = label
                valueTv.text = value
            }
        }.addTo(compositeDisposable)
    }

    private fun onClickMyLocation() {
        viewModel.events.onNext(ClickMyLocation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }

}


