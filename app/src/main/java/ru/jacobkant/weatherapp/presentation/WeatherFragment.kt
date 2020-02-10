package ru.jacobkant.weatherapp.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.frag_weather.*
import ru.jacobkant.weatherapp.*
import ru.jacobkant.weatherapp.di.getViewModel
import ru.jacobkant.weatherapp.model.TemperatureUnit

class WeatherFragment : Fragment() {

    private val viewModel: WeatherViewModel by getViewModel(App.ComponentHolder.appComponent)

    private val compositeDisposable = CompositeDisposable()

    private lateinit var rxPermission: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.ComponentHolder.appComponent.inject(this)
        rxPermission = RxPermissions(this)
        viewModel.events.onNext(
            OnCreate(
                savedInstanceState
            )
        )
        if (savedInstanceState == null)
            onClickMyLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        frag_weather_city_change.setOnClickListener {
            SelectCityDialogFragment()
                .showNow(childFragmentManager, null)
        }
        frag_weather_city_by_location.setOnClickListener {
            onClickMyLocation()
        }
        frag_weather_select.setOnCheckedChangeListener { group, checkedId ->
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
        viewModel.viewState.subscribe {
            frag_weather_city_name.text = it.cityName
            frag_weather_temp.text = it.tempText
            frag_weather_description.text = it.tempDescription
            frag_weather_progress_container.isVisible = it.isWeatherLoading
            frag_weather_temperature_container.isVisible = !it.isWeatherLoading
            frag_weather_select.check(
                when (it.selectedTempUnit) {
                    TemperatureUnit.C -> R.id.frag_weather_select_C
                    TemperatureUnit.F -> R.id.frag_weather_select_F
                }
            )
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
        rxPermission.request(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).subscribe { isGrated ->
            if (isGrated) {
                val locationManager =
                    requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    viewModel.events.onNext(ClickMyLocation)
                } else {
                    showEnableLocationDialog()
                }
            } else Toast.makeText(
                requireContext(),
                "Нет разрешения на получение геопозиции",
                Toast.LENGTH_LONG
            ).show()
        }.addTo(compositeDisposable)
    }

    private fun showEnableLocationDialog() {
        AlertDialog.Builder(requireContext(), R.style.AppTheme_Dialog_Alert)
            .setTitle("Включите GPS")
            .setCancelable(true)
            .setPositiveButton("В настройки") { dialog, _ ->
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingsIntent)
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}


